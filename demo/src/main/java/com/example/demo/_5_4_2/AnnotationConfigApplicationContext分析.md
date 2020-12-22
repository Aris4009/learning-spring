# AnnotationConfigApplicationContext的初始化流程解析

## 1. 通过类的注册来初始化

> 1.1. 创建实例
> 
> AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
> 
> 1.2. 注册组件
> 
> context.register(AppConfig.class, Service.class, ServiceAspect.class);
> 
> > 1.2.1. 记录上下文启动步骤指标
> > 
> > StartupStep registerComponentClass = this.getApplicationStartup().start("spring.context.component-classes.register")  
> >  .tag("classes", () -> Arrays.toString(componentClasses));
> > 
> > 1.2.2. 注册组件
> > 
> > 这个reader为AnnotatedBeanDefinitionReader
> > 
> > this.reader.register(componentClasses);
> > 
> > 1.2.3. 结束记录上下文的状态和其他的指标
> > 
> > registerComponentClass.end();
> 
> 1.3. 上下文刷新
> 
> context.refresh();

### 1.1. 创建实例（略）

### 1.2. 注册组件

#### 1.2.1. 记录上下文启动步骤指标（略）

#### 1.2.2. 注册组件

`AnnotatedBeanDefinitionReader reader`

```java
//注册一个或多个组件。组件的注册是幂等的，这意味着如果多次注册相同的组件，将没有附加的影响。
public void register(Class<?>... componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            registerBean(componentClass);
        }
}

//接着调用注册bean的方法
public void registerBean(Class<?> beanClass) {
        doRegisterBean(beanClass, null, null, null, null);
}
```

`doRegisterBean(Class<T> beanClass, @Nullable String name,  
      @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,  
      @Nullable BeanDefinitionCustomizer[] customizers)`方法

```java
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
            @Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
            @Nullable BeanDefinitionCustomizer[] customizers) {

        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
            return;
        }

        abd.setInstanceSupplier(supplier);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        if (qualifiers != null) {
            for (Class<? extends Annotation> qualifier : qualifiers) {
                if (Primary.class == qualifier) {
                    abd.setPrimary(true);
                }
                else if (Lazy.class == qualifier) {
                    abd.setLazyInit(true);
                }
                else {
                    abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                }
            }
        }
        if (customizers != null) {
            for (BeanDefinitionCustomizer customizer : customizers) {
                customizer.customize(abd);
            }
        }

        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
    }
```

`AnnotatedGenericBeanDefinition`：描述了一个bean的实例，它包含了属性值、构造参数值和具体实现支持的其他信息。 

* 1. 通过beanClass，创建`AnnotatedGenericBeanDefinition`实例

> `setBeanClass(beanClass);`将beanClass绑定到自身`beanClass`的属性上。
> 
> AnnotatedGenericBeanDefinition extends GenericBeanDefinition
> 
> GenericBeanDefinition extends AbstractBeanDefinition
> 
> AbstractBeanDefinition的属性，包含了`beanClass`

> `this.metadata = AnnotationMetadata.introspect(beanClass);`
> 
> AnnotationMetadata是定义了访问指定类的注解的抽象接口，并不要求类已经被加载。
> 
> introspect是一个静态工厂方法，使用标准的反射机制来创建`AnnotationMetadata`类。
> 
> ```java
> static AnnotationMetadata introspect(Class<?> type) {
>         return StandardAnnotationMetadata.from(type);
> }
> ```

* 2. `this.conditionEvaluator`：`ConditionEvaluator`，内部类，用于评估Conditional注解

> `this.conditionEvaluator.shouldSkip(abd.getMetadata())`：根据
> 
> **@Conditional** 注解来决定是否跳过该项目。

* 3. 给`AnnotatedGenericBeanDefinition`设置supplier实例

* 4. 通过`AnnotationScopeMetadataResolver`解析bean的scope，并给`AnnotatedGenericBeanDefinition`设置scope。

* 5. 获取beanName

> 如果name不为空，就取name，如果name为空，就通过`AnnotationBeanNameGenerator`的`buildDefaultBeanName`方法生成。
> 
> 生成逻辑是，先使用`ClassUtils.getShortName(beanClassName)`获取不带包名的类名，然后通过`java.beans.Introspector`工具类，转换名字，具体的转换规则为：如果第一个字母是大写，那就转换为小写；如果第一个和第二个字母均为大写，则原样返回。如：FooBah变为fooBah;X变为x;URL保留为URL

* 6. 通过AnnotationConfigUtils.processCommonDefinitionAnnotations方法，处理通用bean定义的注解，如：lazyInit，primary，dependsOn，role，description。并将这些属性设置给AnnotatedBeanDefinition。

* 7. 判断限定符（修饰符）是否为null，如果不为null，则给`AnnotatedBeanDefinition`设置这些属性，如：primary，lazyInit，addQulifier。

* 8. 设置多个自定义的工厂bean定义的回调函数，例如，设置一个lazy-init或primary flag。

* 9. BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName)：创建BeanDefinition的持有者，将AnnotatedGenericBeanDefinition、beanName赋值给持有者。

* 10. 使用AnnotationConfigUtils.applyScopedProxyMode，判断是否根据scope，创建持有者的代理。
      
      ```java
      static BeanDefinitionHolder applyScopedProxyMode(
                  ScopeMetadata metadata, BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
      
              ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
              if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
                  return definition;
              }
              boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
              return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
          }
      ```

如果不使用代理，就直接返回definition。

判断代理的模式，使用`ScopedProxyCreator`来创建代理。

**ScopedProxyCreator.createScopedProxy(definition,registry,proxyTargetClass)**

```java
final class ScopedProxyCreator {

    private ScopedProxyCreator() {
    }


    public static BeanDefinitionHolder createScopedProxy(
            BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry, boolean proxyTargetClass) {

        return ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
    }

    public static String getTargetBeanName(String originalBeanName) {
        return ScopedProxyUtils.getTargetBeanName(originalBeanName);
    }

}
```

**ScopedProxyUtils.createScopedProxy(definitionHolder,registry,proxyTargetClass)**

```java
public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
            BeanDefinitionRegistry registry, boolean proxyTargetClass) {

        String originalBeanName = definition.getBeanName();
        BeanDefinition targetDefinition = definition.getBeanDefinition();
        String targetBeanName = getTargetBeanName(originalBeanName);

        // Create a scoped proxy definition for the original bean name,
        // "hiding" the target bean in an internal target definition.
        RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
        proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
        proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
        proxyDefinition.setSource(definition.getSource());
        proxyDefinition.setRole(targetDefinition.getRole());

        proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
        if (proxyTargetClass) {
            targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
            // ScopedProxyFactoryBean's "proxyTargetClass" default is TRUE, so we don't need to set it explicitly here.
        }
        else {
            proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
        }

        // Copy autowire settings from original bean definition.
        proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
        proxyDefinition.setPrimary(targetDefinition.isPrimary());
        if (targetDefinition instanceof AbstractBeanDefinition) {
            proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
        }

        // The target bean should be ignored in favor of the scoped proxy.
        targetDefinition.setAutowireCandidate(false);
        targetDefinition.setPrimary(false);

        // Register the target bean as separate bean in the factory.
        registry.registerBeanDefinition(targetBeanName, targetDefinition);

        // Return the scoped proxy definition as primary bean definition
        // (potentially an inner bean).
        return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
    }
```

使用RootBeanDefinition来创建proxyDefinition，此为代理的beanDefinition。

为proxyDefinition设置：

- 被代理bean的持有者；

- 将目标beanDefinition、目标的beanName赋值给proxyDefinition.decoratedDefinition

- 设置source

- 设置role

- 设置targetBeanName

- 判断是否需要创建代理类

- 从原始bean定义中拷贝自动装配设置

- <span style="color:red">将目标beanDefinition的autowrieCandidate、primary属性设置为false</span>

- 通过`GenericApplicationContext`持有的`DefaultListableBeanFactory`来注册目标beanDefinition。

- 返回一个新的beanDefinitionHolder，这个holder包含了代理proxyDefinition，originalBeanName。
* 11. 默认通过`DefaultListableBeanFactory`来注册beanDefinition。
  
  **org.springframework.beans.factory.support.DefaultListableBeanFactory**

```java
  @Override
      public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
              throws BeanDefinitionStoreException {

          Assert.hasText(beanName, "Bean name must not be empty");
          Assert.notNull(beanDefinition, "BeanDefinition must not be null");

          if (beanDefinition instanceof AbstractBeanDefinition) {
              try {
                  ((AbstractBeanDefinition) beanDefinition).validate();
              }
              catch (BeanDefinitionValidationException ex) {
                  throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
                          "Validation of bean definition failed", ex);
              }
          }

          BeanDefinition existingDefinition = this.beanDefinitionMap.get(beanName);
          if (existingDefinition != null) {
              if (!isAllowBeanDefinitionOverriding()) {
                  throw new BeanDefinitionOverrideException(beanName, beanDefinition, existingDefinition);
              }
              else if (existingDefinition.getRole() < beanDefinition.getRole()) {
                  // e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
                  if (logger.isInfoEnabled()) {
                      logger.info("Overriding user-defined bean definition for bean '" + beanName +
                              "' with a framework-generated bean definition: replacing [" +
                              existingDefinition + "] with [" + beanDefinition + "]");
                  }
              }
              else if (!beanDefinition.equals(existingDefinition)) {
                  if (logger.isDebugEnabled()) {
                      logger.debug("Overriding bean definition for bean '" + beanName +
                              "' with a different definition: replacing [" + existingDefinition +
                              "] with [" + beanDefinition + "]");
                  }
              }
              else {
                  if (logger.isTraceEnabled()) {
                      logger.trace("Overriding bean definition for bean '" + beanName +
                              "' with an equivalent definition: replacing [" + existingDefinition +
                              "] with [" + beanDefinition + "]");
                  }
              }
              this.beanDefinitionMap.put(beanName, beanDefinition);
          }
          else {
              if (hasBeanCreationStarted()) {
                  // Cannot modify startup-time collection elements anymore (for stable iteration)
                  synchronized (this.beanDefinitionMap) {
                      this.beanDefinitionMap.put(beanName, beanDefinition);
                      List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
                      updatedDefinitions.addAll(this.beanDefinitionNames);
                      updatedDefinitions.add(beanName);
                      this.beanDefinitionNames = updatedDefinitions;
                      removeManualSingletonName(beanName);
                  }
              }
              else {
                  // Still in startup registration phase
                  this.beanDefinitionMap.put(beanName, beanDefinition);
                  this.beanDefinitionNames.add(beanName);
                  removeManualSingletonName(beanName);
              }
              this.frozenBeanDefinitionNames = null;
          }

          if (existingDefinition != null || containsSingleton(beanName)) {
              resetBeanDefinition(beanName);
          }
          else if (isConfigurationFrozen()) {
              clearByTypeCache();
          }
      }
```

`DefaultListableBeanFactory.registerBeanDefinition`

* 首先判断beanName、beanDefinition不能为空

* 检查beanDefinition是否为AbstractBeanDefinition的实例。

* private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
  
  从缓存中检查beanName来判断是否已经存在beanDefinition
  
  如果存在：
  
  1. 判断是否通过具有相同名称的其他定义来覆盖bean定义，默认为true。
  
  2. 判断已存在的beanDefinition的角色是否小于当前的beanDefinition角色。角色定义在接口BeanDefinition中，分别为：int ROLE_APPLICATION = 0;int ROLE_SUPPORT = 1;int ROLE_INFRASTRUCTURE = 2;
  
  3. 判断已存在的beanDefinition是否和当前beanDefinition相等
  
  4. 当已存在的beanDefinition替换为当前beanDefinition，更新beanDefinitionMap缓存。
  
  如果不存在：
  
  1. 检查bean创建阶段是否已经开始。例如，是否任意的bean已经被标记为created。如果被标记为created阶段，则：
     
     在缓存beanDefinitionMap中，this.beanDefinitionMap.put(beanName, beanDefinition);
     
     新建一个用来保存beanDefinitionNames的列表List<String> updatedDefinitions。列表的大小为DefaultListableBeanFactyro的beanDefinitionNames列表长度+1，然后将这个列表添加至updatedDefinitions。
     
     添加当前的beanName，并将updatedDefinitions重新赋值给this.beanDefinitionNames。
     
     删除通过手动注册的单例。private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);
2. 如果创建阶段没有开始：
   
   执行this.beanDefinitionMap.put(beanName, beanDefinition);
   
   执行this.beanDefinitionNames.add(beanName);
   
   删除通过手动注册的单例。

3. 设置this.frozenBeanDefinitionNames = null;
* 判断当前beanDefinition是否为null，并检查缓存单例的private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);是否存在该bean。如果不存在当前的beanDefinition或者单例缓存中包含了这个bean，那么执行    resetBeanDefinition(beanName)，该方法重新设置了beanDefinition的所有缓存。

* 判断 this.configurationFrozen属性，如果为true，则执行：

```java
  private void clearByTypeCache() {
          this.allBeanNamesByType.clear();
          this.singletonBeanNamesByType.clear();
  }
```

#### 1.2.3. 结束记录上下文的状态和其他的指标（略）

registerComponentClass.end();

### 1.3. AbstractApplicationContext上下文刷新

**org.springframework.context.support.AbstractApplicationContext**

```java
@Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

            // Prepare this context for refreshing.
//为上下文刷新做的准备工作
            prepareRefresh();

            // Tell the subclass to refresh the internal bean factory.
            //此时，获取到了创建好的DefaultListableBeanFactory()
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // Prepare the bean factory for use in this context.
            //为了在这个上下文中使用，准备这个DefaultListableBeanFactory()
            prepareBeanFactory(beanFactory);

            try {
                // Allows post-processing of the bean factory in context subclasses.
                //允许在上下文子类中对bean factory进行post-processing,此为模板方法
                postProcessBeanFactory(beanFactory);
                
                //记录post-process阶段指标
                StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");
                // Invoke factory processors registered as beans in the context.
                //调用在上下文中注册为bean的工厂处理器。
                invokeBeanFactoryPostProcessors(beanFactory);

                // Register bean processors that intercept bean creation.
                //注册beanPostProcessor，用来拦截bean创建
                registerBeanPostProcessors(beanFactory);
                //记录结束
                beanPostProcess.end();

                // Initialize message source for this context.
                //初始化当前上下文的消息来源
                initMessageSource();

                // Initialize event multicaster for this context.
                // 初始化事件多播器
                initApplicationEventMulticaster();

                // Initialize other special beans in specific context subclasses.
                //在特定上下文子类中初始化其他特殊bean，此为模板方法
                onRefresh();

                // Check for listener beans and register them.
                // 注册监听器
                registerListeners();

                // Instantiate all remaining (non-lazy-init) singletons.
                // 实例化所有剩余的（非延迟初始化）单例。
                // 如：conversionService
                finishBeanFactoryInitialization(beanFactory);

                // Last step: publish corresponding event.
                // 最后一步，发布响应事件
                finishRefresh();
            }

            catch (BeansException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
                }

                // Destroy already created singletons to avoid dangling resources.
                destroyBeans();

                // Reset 'active' flag.
                cancelRefresh(ex);

                // Propagate exception to caller.
                throw ex;
            }

            finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
                resetCommonCaches();
                contextRefresh.end();
            }
        }
    }
```

* 使用内置锁this.startupShutdownMonitor，进行方法同步

* 开始记录刷新步骤指标

* 为上下文准备刷新`prepareRefresh()`：准备上下文刷新，设置它的启动时间和激活标志位以及执行property sources的初始化。

```java
protected void prepareRefresh() {
        // Switch to active.
//设置启动时间
        this.startupDate = System.currentTimeMillis();
//此标志位用来判断上下文是否已经关闭
        this.closed.set(false);
//此标志用来判断当前上下文是否被激活
        this.active.set(true);

        if (logger.isDebugEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Refreshing " + this);
            }
            else {
//返回一个友好的上下文名字，返回的是上下文的displayName属性，在实例化上下文时
//使用ObjectUtils.identityToString(this)：返回对象整体身份的String表示形式。
//此属性永远不是null，如果为空，返回""；如果不为空，格式为：obj.getClass().getName() + "@" + getIdentityHexString(obj);
//例如：org.springframework.context.annotation.AnnotationConfigApplicationContext@79698539
                logger.debug("Refreshing " + getDisplayName());
            }
        }

        // Initialize any placeholder property sources in the context environment.
        //初始化上下文环境中的任何带有占位符的属性源，对于子类，默认情况下什么也不做。
        initPropertySources();

        // Validate that all properties marked as required are resolvable:
        // see ConfigurablePropertyResolver#setRequiredProperties
        //验证所有被标记为required的属性是可解析的
        //getEnvironment()，以配置的形式返回应用程序上下文环境，以便进一步自定义，如果this.environment为null，创建一个StandardEnvironment，他是ConfigurableEnvironment的实现。
        getEnvironment().validateRequiredProperties();

        // Store pre-refresh ApplicationListeners...
        //存储准备刷新的应用程序监听器
        if (this.earlyApplicationListeners == null) {
            this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
        }
        else {
            // Reset local application listeners to pre-refresh state.
            this.applicationListeners.clear();
            this.applicationListeners.addAll(this.earlyApplicationListeners);
        }

        // Allow for the collection of early ApplicationEvents,
        // to be published once the multicaster is available...
        this.earlyApplicationEvents = new LinkedHashSet<>();
    }
```

* ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();告诉子类刷新内部的bean factory。

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        //获取DefaultListableBeanFactory()
        refreshBeanFactory();
        //返回这个DefaultListableBeanFactory()
        return getBeanFactory();
}
```

**GenericApplicationContext.refreshBeanFactory()**

```java
//---------------------------------------------------------------------
    // Implementations of AbstractApplicationContext's template methods
    //---------------------------------------------------------------------

    /**
     * Do nothing: We hold a single internal BeanFactory and rely on callers
     * to register beans through our public methods (or the BeanFactory's).
     * @see #registerBeanDefinition
     */
    @Override
    //这里采用了模板方法的设计模式，如果调用两次refresh，会抛出这里的异常
    //private final AtomicBoolean refreshed = new AtomicBoolean();Java的元子类；false-希望的值，true-更新的值
    protected final void refreshBeanFactory() throws IllegalStateException {
        if (!this.refreshed.compareAndSet(false, true)) {
            throw new IllegalStateException(
                    "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
        }
    //getId()->AbstractApplicationContext private String id = ObjectUtils.identityToString(this)
    //上下文的唯一id
    //private final DefaultListableBeanFactory beanFactory;
    //在初始化时，创建了一个DefaultListableBeanFactory()
        this.beanFactory.setSerializationId(getId());
    }
```

**AbstractApplicationContext.prepareBeanFactory(ConfigurableLisableBeanFactory beanFactory)**

```java
//配置工厂的标准上下文特征，例如上下文的classLoader和post-processors
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // Tell the internal bean factory to use the context's class loader etc.
        //Set the class loader to use for loading bean classes. Default is the thread context class loader.
        //Note that this class loader will only apply to bean definitions that do not carry a resolved bean class yet. This is the case as of Spring 2.0 by default: Bean definitions only carry bean class names, to be resolved once the factory processes the bean definition.
        //为工厂设置classLoader，用来加载bean class。默认是当前线程上下文的classLoader。
        //注意，这个classLoader只能应用于bean定义，不能用来解析bean
        beanFactory.setBeanClassLoader(getClassLoader());
        //private static final boolean shouldIgnoreSpel = SpringProperties.getFlag("spring.spel.ignore");
        //此属性是一个标志位，用来控制是否忽略Spring的EL表达式，默认是false
        if (!shouldIgnoreSpel) {
            beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        }

        //1. 策略接口PropertyEditorRegistrar->void registerCustomEditors(PropertyEditorRegistry registry);，用于向属性编辑器注册表注册自定义属性编辑器的策略的接口。
        //当需要在几种不同情况下使用同一组属性编辑器时，这特别有用：编写相应的注册器，并在每种情况下重复使用该注册器。
        //2. 接口PropertyEditorRegistry，为对应的策略。为JavaBean PropertyEditors封装了方法
        //3. ResourceEditorRegistrar实现了PropertyEditorRegistrar接口，包含了多种Resource属性编辑
        /**
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader, this.propertyResolver);
        doRegisterEditor(registry, Resource.class, baseEditor);
        doRegisterEditor(registry, ContextResource.class, baseEditor);
        doRegisterEditor(registry, InputStream.class, new InputStreamEditor(baseEditor));
        doRegisterEditor(registry, InputSource.class, new InputSourceEditor(baseEditor));
        doRegisterEditor(registry, File.class, new FileEditor(baseEditor));
        doRegisterEditor(registry, Path.class, new PathEditor(baseEditor));
        doRegisterEditor(registry, Reader.class, new ReaderEditor(baseEditor));
        doRegisterEditor(registry, URL.class, new URLEditor(baseEditor));

        ClassLoader classLoader = this.resourceLoader.getClassLoader();
        doRegisterEditor(registry, URI.class, new URIEditor(classLoader));
        doRegisterEditor(registry, Class.class, new ClassEditor(classLoader));
        doRegisterEditor(registry, Class[].class, new ClassArrayEditor(classLoader));

        if (this.resourceLoader instanceof ResourcePatternResolver) {
            doRegisterEditor(registry, Resource[].class,
                    new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader, this.propertyResolver));
        }
    }
        **/
        //4. 注册用来解析属性的属性编辑器
        //private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);  
        beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

        // Configure the bean factory with context callbacks.
        //配置bean工厂的上下文回调
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        //DefaultListableBeanFactory extend AbstractAutowireCapableBeanFactory
        //private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();
        //在依赖检查和自动装配中忽略依赖接口，默认情况下，仅BeanFactory接口被忽略。
        //在构造DefaultListableBeanFactory时，超类AbstractAutowireCapableBeanFactory的构造器默认忽略一下接口：
        //ignoreDependencyInterface(BeanNameAware.class);
		//ignoreDependencyInterface(BeanFactoryAware.class);
		//ignoreDependencyInterface(BeanClassLoaderAware.class);
        beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
        beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
        beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
        beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationStartup.class);

        // BeanFactory interface not registered as resolvable type in a plain factory.
        // MessageSource registered (and found for autowiring) as a bean.
        //BeanFactory没有在普通工厂中注册为可解析类型，MessageSource注册为一个bean
        beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
        beanFactory.registerResolvableDependency(ResourceLoader.class, this);
        beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
        beanFactory.registerResolvableDependency(ApplicationContext.class, this);

        // Register early post-processor for detecting inner beans as ApplicationListeners.
        //尽早注册post-processor以便将内部bean当做ApplicationListeners
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

        // Detect a LoadTimeWeaver and prepare for weaving, if found.
        // 检测LoadTimeWeaver并准备织入
        if (!IN_NATIVE_IMAGE && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // Set a temporary ClassLoader for type matching.
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }

        // Register default environment beans.
        // 注册默认环境bean
        //environment
        if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
        }
        //systemProperties
        if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
        }
        //systemEnvironment
        if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
        }
        //applicationStartup
        if (!beanFactory.containsLocalBean(APPLICATION_STARTUP_BEAN_NAME)) {
            beanFactory.registerSingleton(APPLICATION_STARTUP_BEAN_NAME, getApplicationStartup());
        }
    }
```

**DefaultResourceLoader**

<mark>AbstractApplicationContext extend DefaultResourceLoader</mark>

```java
    public ClassLoader getClassLoader() {
        return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
    }
```



**AbstractApplicationContext.finishRefresh()**

完成当前上下文刷新，调用LifecycleProcessor's onRefresh()方法，并且发布`ContextRefreshedEvent`

```java
protected void finishRefresh() {
		// Clear context-level resource caches (such as ASM metadata from scanning).
        //清除上下文级别的resource缓存
		clearResourceCaches();

		// Initialize lifecycle processor for this context.
        // 模板方法，初始化lifecycle processor
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
        // 首先传播刷新到lifecycle processor
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
        // 发布最终事件
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
		if (!IN_NATIVE_IMAGE) {
			LiveBeansView.registerApplicationContext(this);
		}
	}
```



## 2. context.getBean 获取bean实例



**DefaultListableBeanFactory**



```java
    @Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBean(Class<T> requiredType, @Nullable Object... args) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
		if (resolved == null) {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
		return (T) resolved;
	}
    
    @Nullable
	private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
		NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
		if (namedBean != null) {
			return namedBean.getBeanInstance();
		}
		BeanFactory parent = getParentBeanFactory();
		if (parent instanceof DefaultListableBeanFactory) {
			return ((DefaultListableBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
		}
		else if (parent != null) {
			ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
			if (args != null) {
				return parentProvider.getObject(args);
			}
			else {
				return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
			}
		}
		return null;
	}
    
    @Nullable
	private <T> NamedBeanHolder<T> resolveNamedBean(
			ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) throws BeansException {

		Assert.notNull(requiredType, "Required type must not be null");
		String[] candidateNames = getBeanNamesForType(requiredType);

		if (candidateNames.length > 1) {
			List<String> autowireCandidates = new ArrayList<>(candidateNames.length);
			for (String beanName : candidateNames) {
				if (!containsBeanDefinition(beanName) || getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (!autowireCandidates.isEmpty()) {
				candidateNames = StringUtils.toStringArray(autowireCandidates);
			}
		}

		if (candidateNames.length == 1) {
			String beanName = candidateNames[0];
			return new NamedBeanHolder<>(beanName, (T) getBean(beanName, requiredType.toClass(), args));
		}
		else if (candidateNames.length > 1) {
			Map<String, Object> candidates = CollectionUtils.newLinkedHashMap(candidateNames.length);
			for (String beanName : candidateNames) {
				if (containsSingleton(beanName) && args == null) {
					Object beanInstance = getBean(beanName);
					candidates.put(beanName, (beanInstance instanceof NullBean ? null : beanInstance));
				}
				else {
					candidates.put(beanName, getType(beanName));
				}
			}
			String candidateName = determinePrimaryCandidate(candidates, requiredType.toClass());
			if (candidateName == null) {
				candidateName = determineHighestPriorityCandidate(candidates, requiredType.toClass());
			}
			if (candidateName != null) {
				Object beanInstance = candidates.get(candidateName);
				if (beanInstance == null || beanInstance instanceof Class) {
					beanInstance = getBean(candidateName, requiredType.toClass(), args);
				}
				return new NamedBeanHolder<>(candidateName, (T) beanInstance);
			}
			if (!nonUniqueAsNull) {
				throw new NoUniqueBeanDefinitionException(requiredType, candidates.keySet());
			}
		}

		return null;
	}
```
