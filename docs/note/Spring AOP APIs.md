# 6. Spring AOP APIs

前面的章节描述了通过使用@AspectJ和基于schema的切面定义为Spring提供AOP支持。在这个章节中，将要塔伦底层的Spring AOP APIs。对于普通的应用程序，建议通过使用Spring AOP的AspectJ切点来描述之前的章节。

## 6.1. Spring中的切点API

本章描述了Spring如何处理重要的切点概念

### 6.1.1. 概念

Spring的切点模型是切点可独立于通知类型来达到重用目的。可以在不同的切点上使用相同的切点。

`org.springframework.aop.Pointcut`接口是中心接口，用来将通知定向到特定的类和方法上。完整的接口如下：

```java
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();

}
```

`Pointcut`接口分为两部分，目的是允许重用类和方法匹配部分，细粒度控制组合操作（例如与另一个方法匹配器执行`union`操作）。

`ClassFilter`接口用来将切点限制为指定的一组目标类。如果`matches()`方法总是返回true，所有目标类将被匹配。下面列出了这个接口的定义：

```java
public interface ClassFilter {

    boolean matches(Class clazz);
}
```

`MethodMatcher`接口通常更重要。这个接口的完整定义如下：

```java
public interface MethodMatcher {

    boolean matches(Method m, Class targetClass);

    boolean isRuntime();

    boolean matches(Method m, Class targetClass, Object[] args);
}
```

`matches(Medhod,Class)`方法用来测试切点是否与目标类上的指定方法匹配。当AOP代理被创建时，可以执行此评估以避免对每个方法调用进行测试。如果两个参数的`matches`方法对于指定的方法返回`true`，并且`isRuntime()`方法返回`true`，在每个方法调用时，三个参数的matches会被调用。这让切点可以在目标通知开始之前立即查看传递的方法调用的参数。

大多数`MethodMatcher`实现是静态的，意味着他们的`isRuntime()`方法返回`false`。在这个例子中，三个参数的`matches`方法永远不会被调用。

*如果可能，尝试让切点变为静态的，当AOP代理被创建时，允许AOP框架缓存切点的评估结果。*

### 6.1.2. 切点操作

Spring支持在切点上进行操作（联合和交叉）。

联合意味着方法匹配任意一个切点。交叉意味着方法匹配所有切点。联合通常来说更有用。可以通过使用`org.springframework.aop.support.Pointcuts`类中的静态方法或通过使用`OmposablePointcut`类中的方法来组合切点。但是，使用AspectJ切点表达式通常来说是更简单的途径。

### 6.1.3. AspectJ切点表达式

从Spring2.0以后，`org.springframework.aop.aspectj.AspectJExpressionPointcut`是Spring使用的最重要的切点类型。这是一个使用AspectJ支持的库来解析AspectJ切点表达式字符串的切点。

### 6.1.4. 方便的切点实现

Spring提供一些方便的切点实现。可以直接使用他们；其他的则打算在特定于应用程序的切入点中子类化。

**静态切点**

静态切点是基于方法和目标类的，并且不能挈带方法的参数。静态切点满足大多数的用途，并且最好。当方法第一次被调用时，Spring仅评估一次静态切点。在那以后，对于每个方法的调用，不再需要评估切点。

本节的其余部分描述了Spring附带的一些静态切点实现。

**正则表达式切点**

指定静态切点的一种明显方法是正则表达式。除了Spring框架之外，还有几个AOP框架使之成为可能。

`org.springframework.aop.support.JdkRegexpMethodPointcut`是一个通用的正则表达式切点，用于在JDK中支持正则表达式。

使用`JdkRegexpMethodPointcut`类，可以提供一个匹配字符串列表。如果他们中的任何一个配匹配，切点评估为`true`。（因此，最终的切点模式等效于联合）。

下面是如何使用`JdkRegexpMethodPointcut`的例子：

```java
<bean id="settersAndAbsquatulatePointcut"
        class="org.springframework.aop.support.JdkRegexpMethodPointcut">
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```

Spring提供了一个方便的类名叫`RegexpMethodPointcutAdvisor`，让用户可以引用一个`Advice`（记住，这个`Advice`可以是一个拦截器、before advice,throw advice、或其他）。在幕后，Spring使用`JdkRegexpMethodPointcut`。使用`RegexpMethodPointcutAdvisor`简化了连接，因为一个bean同时封装了切入点和通知，如下面的示例所示:

```xml
<bean id="settersAndAbsquatulateAdvisor"
        class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
    <property name="advice">
        <ref bean="beanNameOfAopAllianceInterceptor"/>
    </property>
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```

可以对任何通知类型使用`RegexpMethodPointcutAdvisor`。

**属性驱动切点**

静态切点的一个重要类型是元数据驱动的切点。这将使用元数据属性的值（通常是源码级别的元数据）。

**动态切点**

动态切点比静态切点更昂贵。他们考虑了方法参数以及静态信息。这意味着他们必须对每个方法的调用进行评估并且由于参数不同，因此无法缓存结果。

主要的例子是`control flow`切点。

**Control Flow Pointcuts 控制流切点**

Spring控制流切点与AspectJ`cflow`切点概念上相似，尽管比AspectJ功能弱。（目前没有办法指定切点在与另一个切点匹配的连接点下运行）。控制流切点匹配当前调用栈。例如，如果连接点是通过在`com.mycompany.web`包中的方法或通过`SomeCaller`类调用的，则可能会触发。通过使用`org.springframework.aop.support.ControlFlowPointcut`类来指定控制流切点。

| 控制流切点与其他动态切点相比，在运行时评估成本高得多。在Java1.4中，它是其他动态切点花费成本的5倍。 |
| ----------------------------------------------------- |

### 6.1.5. 切点超类

Spring提供了有用的切点超类来帮助实现自定义的切点。

因为静态切点最有用，应该尽可能子类化`StaticMethodMatcherPointcut`。这要求仅实现一个抽象方法（尽管可以覆盖其他方法来自定义行为）。下面的例子展示了如何子类化`StaticMethodMatcherPointcut`：

```java
class TestStaticPointcut extends StaticMethodMatcherPointcut {

    public boolean matches(Method m, Class targetClass) {
        // return true if custom criteria match
    }
}
```

### 6.1.6. 自定义切点

因为在Spring AOP中的切点是Java类而不是语言功能（如AspectJ），可以声明自定义切点，无论是静态的还是动态的。Spring中的自定义切点可以是任意复杂的。然而，建议尽可能使用AspectJ切点表达式语言。

| Spring后续的版本可能通过JAD来支持意义切点-例如，更改目标对象中的实例变量的所有方法。 |
| ----------------------------------------------- |

## 6.2. Spring中的Advice API

现在解释Spring AOP如何处理通知。

### 6.2.1. Advice生命周期

每个advice是一个Spring bean。一个advice实例可以被所有的被通知对象共享，或对于每个被通知对象都是唯一的。这对应的是per-class或per-instance通知。

最常用的通知是per-class通知。例如事务advisors。他们不依赖于代理对象的状态或添加新状态。他们仅仅作用域方法和参数上。

对于引入，per-instance通知是合适的，以支持混合。在这种情况下，通知将状态添加到代理对象。

可以混合使用共享的和per-instance通知在同一个AOP代理中。

### 6.2.2. Spring中的通知类型

Spring提供了一系列的通知类型，并且可扩展来支持任意的通知类型。本节描述了基本概念和标准通知类型。

**拦截环绕通知**

Spring中最基础的通知类型是拦截环绕通知。

对于使用方法拦截的环绕通知，Spring符合AOP `Alliance`接口。实现了`MethodInterceptor`接口和实现了环绕通知的类也应该实现如下接口：

```java
public interface MethodInterceptor extends Interceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

方法`invoke()中的``MethodInvocation`参数公开了被调用的方法，目标连接点，AOP代理和该方法的参数。方法`invoke()`应该返回调用结果：连接点的返回值。

下面的例子展示了`MethodInterceptor`的实现：

```java
public class DebugInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Before: invocation=[" + invocation + "]");
        Object rval = invocation.proceed();
        System.out.println("Invocation returned");
        return rval;
    }
}
```

注意，调用`MethodInvocation`的`proceed()`方法。这将沿着拦截器联向下到达连接点。绝大多数拦截器调用这个方法并返回它的返回值。但是，`MethodINterceptor`与任何环绕通知一样，可以返回一个不同的值或抛出异常，而不是调用proceed方法。

| `MethodInterceptor`实现提供了与其他符合AOP Alliance要求的AOP实现的互操作性。本节其余部分讨论的其他通知类型将实现常见的AOP概念，但以特定于Spring的方式。尽管使用具体的通知类型有一个优势，但是如果可能想在另一个AOP框架中运行切面，则在通知周围使用`MethodInterceptor`。注意，切点当前无法在框架之间互操作，并且AOP Alliance当前未定义切点接口。 |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |

**前置通知**

一个更简单的通知类型是前置通知。它不需要`MethodInvocation`对象，因为它尽在进入方法之前被调用。

前置通知的主要优点是，不需要调用`proceed()`方法，因此，不会因疏忽而未能沿拦截器链继续前进。

下面列出了`MethodBeforeAdvice`接口：

```java
public interface MethodBeforeAdvice extends BeforeAdvice {

    void before(Method m, Object[] args, Object target) throws Throwable;
}
```

(尽管通常的对象适用于字段拦截，但是Spring不太可能实现，但Spring的API设计允许先于字段通知)。

注意，返回类型是`void`。前置通知可以在连接点运行前插入客户行为，但是不能改变返回值。如果前置通知抛出异常，它将停止进一步执行连接器链。异常会传播回拦截链。如果是未检查或在调用方法的签名上，则将其直接传递给客户端。否则，它将由AOP代理包装在未经检查的异常中。

下面的例子展示了Spring中的前置通知，计算所有方法调用：

```java
public class CountingBeforeAdvice implements MethodBeforeAdvice {

    private int count;

    public void before(Method m, Object[] args, Object target) throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```

| 前置通知可以用于任何切点。 |
| ------------- |

**异常通知**

异常通知是在连接点抛出异常返回后被调用的。Spring提供类型化的异常通知。注意，这意味着`org.springframework.aop.ThrowsAdvice`接口不包含任何方法。它是一个标签接口，用于标识指定对象实现了一个或多个类型化的异常通知方法。这些方法应采用一下形式：

```java
afterThrowing([Method, args, target], subclassOfThrowable)
```

只有最后一个参数是必须的。这个方法可以具有一个或4个参数，这依赖于通知方法是否对该方法和参数感兴趣。下面另个列表展示了异常通知的例子：

下面的通知在抛出`RomoteException`时被调用（包括子类）：

```java
public class RemoteThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }
}
```

与前面的通知不同，下面的例子声明了4个参数，因此它可以访问调用方法，方法参数，目标对象。如果抛出了`ServletException`，这个通知会被调用：

```java
public class ServletThrowsAdviceWithArguments implements ThrowsAdvice {

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```

最后的例子说明了可以在单独的类中使用这两种方法来处理`RemoteException`和`ServletException`。任意数量的异常通知方法可以组合到一个单独的类中：

```java
public static class CombinedThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```

| 如果异常通知自身抛出异常，它会覆盖原始异常（也就是说，它改变了抛出的异常给用户）。这个覆盖的异常通常是RuntimeException，兼容任何方法签名。但是，如果异常通知抛出一个可检查的异常，它必须匹配目标方法声明的异常，因此在某种程度上耦合到了指定目标方法签名。不要抛出与目标方法签名不兼容的未声明的已检查异常！ |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------- |

*异常通知可以对任何切点使用。*

**后置返回通知**

Spring中的后置返回通知必须实现`org.springframework.aoop.AfterReturningAdvice`接口：

```java
public interface AfterReturningAdvice extends Advice {

    void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable;
}
```

后置返回通知可以访问返回值（不能修改返回值）、调用的方法，方法的参数和目标对象。

下面的后置返回通知统计了所有成功调用方法并且没有抛出异常：

```java
public class CountingAfterReturningAdvice implements AfterReturningAdvice {

    private int count;

    public void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```

该通知不会改变执行路径。如果抛出异常，则会将其抛出到链接链而不是返回值。

| 后置返回通知可以用于任意切点。 |
| --------------- |

**引入通知**

Spring将引入通知作为一种特殊的拦截通知。

引入需要实现`IntroductionAdvisor`和`IntroductionInterceptor`接口：

```java
public interface IntroductionInterceptor extends MethodInterceptor {

    boolean implementsInterface(Class intf);
}
```

从AOP Alliance `MethodInterceptor`接口集成的`invoke()`方法必须实现引入。也就是说，如果被调用的方法在引入接口上，则该引入负责拦截处理方法调用，不能调用`proceed()`。

引入通知不能用于任意切点，它仅能应用于类，而不是方法级别。只能通过`IntroductionAdvisor`使用引入通知，它具有一下方法：

```java
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

    ClassFilter getClassFilter();

    void validateInterfaces() throws IllegalArgumentException;
}

public interface IntroductionInfo {

    Class<?>[] getInterfaces();
}
```

这些接口没有`MethodMatcher`，因此，没有与通知相关的切点。只有类过滤是合乎逻辑的。

方法`getInterfaces()`返回此advisor引入的接口。

内部使用`validateInterfaces()`方法来查看所有配置的`IntroductionInterceptor`是否可以实现引入的接口。

思考Spring测试套件中的例子并且假设想要为一个或多个对象引入一下接口:

```java
public interface Lockable {
    void lock();
    void unlock();
    boolean locked();
}
```

希望将通知对象强制转换为`Lockable`，无论他们的类型和调用所和解锁方法如何。如果调用`lock()`方法，想要所有的setter方法抛出一个`LockedException`。因此，可以增加一个提供此能力的切面让对象在无需了解对象的情况下不可变：这是一个很好的AOP的例子。

首先，需要一个`IntroductionInterceptor`来完成繁重的工作。在这种情况下，扩展`org.springframework.aop.support.DelegatingIntroductionInterceptor`是方便的类。可以直接实现`IntroductionInterceptor`，但是使用`DelegatingIntroductionInterceptor`在多数情况下更好。

`DelegatingIntroductionInterceptor`旨在委派引入的接口的实际实现，从而隐藏了使用拦截的方式。可以使用构造函数将委派设置为任何对象。默认的委派（当无参构造器被使用时）是`this`。因此，在下一个例子中，委派是`DelegatingIntroductionInterceptor`的子类`LockMixin`。指定一个委派（默认是自身），`DelegatingIntroductionInterceptor`实例查找由该委派实现的所有接口（`IntroductionInterceptor`除外）并且针对其中任何一个引入。例如`LockMixin`的子类可以调用`suppressInterface(Class intf)`方法来禁止

不应公开的接口。但是，无论`IntroductionInterceptor`准备支持多少个接口，`IntroductionAdvisor`用于控制实际公开哪些接口。引入的接口隐藏了目标对同一接口的任意实现。

注意`locked`实例变量的使用。这有效地将状态附加到目标对象中保存的状态。

下面的例子展示了`LockMixin`类：

```java
public class LockMixin extends DelegatingIntroductionInterceptor implements Lockable {

    private boolean locked;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean locked() {
        return this.locked;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (locked() && invocation.getMethod().getName().indexOf("set") == 0) {
            throw new LockedException();
        }
        return super.invoke(invocation);
    }

}
```

通常，不需要覆盖`invoke()`方法。`DelegatingIntroductionInterceptor`实现通常足以满足（如果引入了方法，则调用委托方法，否则进入到连接点）。在当前情况下，需要添加一个检查：如果处于锁定模式，则无法调用任何setter方法。

所需的引入仅需要持有一个单独的`LockMixin`实例并且指定所引入的接口（在这种情况下，仅仅是`Lockable`）。一个更复杂的例子是可能引用了引入拦截器（它将被定义为原型）。在这种情况下，没有与`LockMixin`相关的配置，因此使用`new`来创建它，一下显示了`LockMixinAdvisor`类：

```java
public class LockMixinAdvisor extends DefaultIntroductionAdvisor {

    public LockMixinAdvisor() {
        super(new LockMixin(), Lockable.class);
    }
}
```

可以非常简单的应用此advisor，因为它不需要配置。（但是，如果没有`IntroductionAdvisor`，就无法使用`IntroductionInterceptor`）。像通常的引入一样，advisor必须是每个实例的，因为它是有状态的。需要不同的`LockMixinAdvisor`接口实例，因此`LockMixin`是对于每个被通知对象的。 Advisor包含被通知对象的部分状态。

可以使用`Advised.addAdvisor()`方法以编程的方式应用advisor或（建议以这种方式）通过XML配置的方式。下文讨论的所有代理创建选择，包括“自动代理创建器”，都可以正确处理引入和有状态的混合。

## 6.3. Spring中的Advisor API

在Spring中，Advisor是一个切面，它包含了仅一个相关联的切点表达式的通知对象。

除了引入的特殊情况，任何advisor可以用于任何通知。`org.springframework.aop.support.DefaultPointcutAdvisor`是最常用的advisor类。它可以与`MethodInterceptor`，`BeforeAdvice`或`ThrowAdvice`一直使用。

在Spring中，相同的AOP代理可以混合使用advisor和advice类型。例如，可以使用拦截环绕通知，异常通知和前置通知在一个代理配置中。Spring自动创建所需的拦截链。

## 6.4. 使用`ProxyFactoryBean`来创建AOP代理

如果使用Spring IoC容器（`ApplicationContext`或`BeanFactory`），那么需要使用Spring AOP的`FactoryBean`实现之一。（记住，factory bean引入了一个间接层，让它创建不同类型的对象。）

> Spring AOP还支持在后台使用factory beans。

Spring中创建AOP代理的基本方式是使用`org.springframework.aop.framework.ProxyFactoryBean`。这样就可以完全控制切点、应用的任何通知以及它们的顺序。但是，如果不需要这样的控制，有一些更简单的选项是更好的。

### 6.4.1.  基础

`ProxyFactoryBean`，与其他Spring中的`FactoryBean`实现一样，引入一个间接层。如果定义了一个名为`foo`的`ProxyFatoryBean`，`ProxyFactoryBean`实例本身对`foo`引用的对象不可见，而看到的是由`ProxyFactoryBean`中的`getObject()`方法的实现创建的对象。

使用`ProxyFactoryBean`或其他IoC-aware类来创建AOP代理的一个重要好处是通知和切点可以通过IoC来管理。这是一个强大的功能，支持其他AOP框架难以实现的某些方法。例如，通知自身可以引用应用程序对象（除了在任意AOP框架中可用的目标对象），得益于DI提供的所有可插拔性。

### 6.4.2. JavaBean 属性

与Spring附带的大多数`FactoryBean`实现一样，`ProxyFactoryBean`类本身就是JavaBean。它的属性被用来：

* 指定要代理的目标

* 指定是否使用CGLIB

一些关键属性继承自`org.springframework.aop.framework.ProxyConfig`（Spring中所有AOP代理工厂的超类）。这些关键属性包括：

* `proxyTargetClass`:`true`表示目标类被代理而不是目标类的接口。如果这个属性被设置为`true`，CGLIB代理会被创建。

* `optimize`: 控制是否将主动优化应用于通过CGLIB创建的代理。除非完全理解相关的AOP代理如何处理优化，否则不应该轻率地使用这种设置。目前仅用于CGLIB代理。对JDK代理没有效果。

* `frozen`:如果代理配置是`frozen`，就不允许改变配置。这对于进行轻微优化以及不希望调用者在创建代理后希望调用者（通过`Advised`接口）操纵代理的情况下都是有用的。这个属性的默认值是`false`，因此是允许改变的（例如增加附加的通知）。

* `exposeProxy`:决定在`ThreadLocal`中是否应该暴露当前代理以便通过目标进行访问。如果目标需要获取代理并且`exposeProxy`属性被设置为`true`，目标可以使用`AopContext.currentProxy()`方法。

`ProxyFactoryBean`中特有的其他属性包括：

* `proxyInterfaces`:一个接口名称的`String`数组。如果没有提供，会使用CGLIB代理。

* `interceptorNames`:一个字符串数组，包含`Advisor`,interceptor或其他通知名称。排序是非常重要的，根据先到先得的原则。也就是说，列表中的第一个拦截器会首先拦截方法调用。

        名称是当前工厂的bean名称，包括从祖先工厂中的bean名称。不能在此提及bean引用，因为这样做会导致`ProxyFactoryBean`忽略通知的单例设置。

        可以在拦截器名称后加上星号(*)。这样做会导致所有advisor bean的应用程序名称都以要应用的星号之前的部分开头。可以找到使用此功能的例子，在`Using Golbal Advisors`中。

* singleton:工厂是否应该返回一个单例对象，无论调用`getObject()`多少次。一些`FactoryBean`实现提供这样的方法。默认的值是`true`。如果想要使用有状态的通知-例如，对于有状态的混合-使用原型通知以及单例值设置为`false`。

### 6.4.3. JDK代理和CGLIB代理

本部分是有关`ProxyFactoryBean`如何选择为特定目标对象（将被代理的对象）创建基于JDK的代理或CGLIB的代理的权威性文档。

> 在Spring的1.2.x和2.0版本之间，`ProxyFactoryBean`的行为与创建基于JDK或CGLIB的代理有关。`ProxyFactoryBean`现在在自动检测接口方面表现出与`TransactionProxyFactoryBean`类有类似的语义。

     

如果要代理的目标对象的类没有实现任何接口（以下简称为目标类），则将创建基于CGLIB的代理。这是最简单的情况，因为JDK代理是基于接口的，没有接口意味着无法进行JDK代理。可以插入目标bean并且设置`interceptorNames`属性来指定拦截器列表。注意，即使`ProxyFactoryBean`的`proxyTargetClass`设置为`false`，也会创建基于CGLIB的代理。（这样做是没有意义的，最好将其从bean定义中删除，因为它是多余的，并且在最糟糕的情况下会造成混淆。）



如果目标类实现了一个或多个接口，则创建的代理类型取决于`ProxyFactoryBean`的配置。



如果`ProxyFactoryBean`的`proxyTargetClass`属性设置为`true`，则会创建CGLIB代理。这是有道理的，并且符合最少知道的原则。即使将`ProxyFactoryBean`的`proxyInterfaces`属性设置为一个或多个完全限定的接口名称，`proxyTargetClass`属性设置为`true`，事实上也会导致基于CGLIB的代理生效。



如果`ProxyFactoryBean`的`proxyInterfaces`属性设置了一个或多个全限定接口名称，会创建基于CGLIB的代理。创建的代理实现了`proxyInterfaces`属性中指定的所有接口。如果目标类恰好实现了比`proxyInterfaces`属性指定的接口更多个接口，那很好，但是返回的代理对象不实现那些其他接口。



如果`ProxyFactoryBean`没有设置`proxyInterfaces`属性，但是目标类实现了一个或多个接口，`ProxyFactoryBean`自动检测目标类实际实现的接口，基于JDK代理会被创建。实际代理的接口是目标类实现的所有接口。实际上，这与将目标类的每个接口的列表提供给`proxyInterfaces`属性是相同的。但是，它的工作量大大减少，并且不容易出现错误。



### 6.4.4. 代理接口

思考下面`ProxyFactoryBean`的例子。这个例子涉及到：

* 被代理的目标bean。在这个例子中，是`personTarget`的bean定义。

* 一个`Advisor`和一个`Interceptor`来提供通知。

* 一个AOP代理bean定义，指定目标对象（目标对象是`personTarget`bean），代理接口以及要应用的通知。

```xml
<bean id="personTarget" class="com.mycompany.PersonImpl">
    <property name="name" value="Tony"/>
    <property name="age" value="51"/>
</bean>

<bean id="myAdvisor" class="com.mycompany.MyAdvisor">
    <property name="someProperty" value="Custom string property value"/>
</bean>

<bean id="debugInterceptor" class="org.springframework.aop.interceptor.DebugInterceptor">
</bean>

<bean id="person"
    class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="proxyInterfaces" value="com.mycompany.Person"/>

    <property name="target" ref="personTarget"/>
    <property name="interceptorNames">
        <list>
            <value>myAdvisor</value>
            <value>debugInterceptor</value>
        </list>
    </property>
</bean>
```

注意，属性`interceptorNames`接收字符串类型的列表，在当前工厂中，它持有拦截器或advisors的bean名称。advisors的顺序很重要。

> 可能会惊讶为什么列表不持有bean的引用。原因是如果`ProxyFactoryBean`的单例属性被设置为`false`，它必须能够返回独立的代理实例。如果任何advisors本身是原型，需要返回独立的实例，因此它必须能够从工厂中获取原型的实例。持有引用是不够的。



这个`person`的bean 定义可以代替`Person`的实现：

```java
Person person = (Person) factory.getBean("person");
```



其他在同一个IoC上下文中的bean可以表达对此强类型的依赖性，与普通的Java对象一样：

```xml
<bean id="personUser" class="com.mycompany.PersonUser">
    <property name="person"><ref bean="person"/></property>
</bean>
```



例子中的`PersonUser`类暴露了一个`Person`属性。对它来说，可以透明的使用AOP代理代替”真实的“`Person`的实现。但是，它的类是动态代理类。可以将其转换为`Advised`接口。



可以使用匿名内部bean来隐藏目标和代理之间的区别。只有`ProxyFactoryBean`是不同的。通知仅出于完整性考虑。下面的例子展示了如何使用一个匿名内部bean:

```xml
<bean id="myAdvisor" class="com.mycompany.MyAdvisor">
    <property name="someProperty" value="Custom string property value"/>
</bean>

<bean id="debugInterceptor" class="org.springframework.aop.interceptor.DebugInterceptor"/>

<bean id="person" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="proxyInterfaces" value="com.mycompany.Person"/>
    <!-- Use inner bean, not local reference to target -->
    <property name="target">
        <bean class="com.mycompany.PersonImpl">
            <property name="name" value="Tony"/>
            <property name="age" value="51"/>
        </bean>
    </property>
    <property name="interceptorNames">
        <list>
            <value>myAdvisor</value>
            <value>debugInterceptor</value>
        </list>
    </property>
</bean>
```

使用匿名内部bean的有点是仅有一个`Person`类型的对象。如果要防止应用程序上下文的用户获取对为通知对象的引用，或者需要避免使用Spring IoC自动装配的任何歧义，这将非常有用。可以说，还有一个有点事`ProxyFactoryBean`定义是独立的。但是，有时候能够从工厂获得胃镜通知的目标实际可能是一个优势。



### 6.4.5. 代理类

如果要代理一个类而不是一个或多个接口，该怎么办？



假设在之前的例子中，没有`Person`接口。需要通知一个名为`Person`的对象，它没有实现任何业务接口。在这种情况下，可以配置Spring使用CGLIB代理而不是动态代理。为了达到这个目的，可以设置`ProxyFactoryBean`的`proxyTargetClass`属性为`true`。然而，最好还是通过接口编程，而不是类，在使用遗留代码时，通知未实现接口的类的功能可能会很有用。（通常，Spring不是规定性的。尽管可以很容易地应用良好实践，但可以避免强制采取特定的方法）。



只要你想，就可以在任何情况下使用CGLIB，即使被代理的对象实现了接口。



CGLIB代理通过在运行时生成目标类的子类来进行工作。Spring配置此生成的子类，并将方法调用委托给原始目标。子类用于实现装饰器模式，并织入到通知中。



CGLIB代理通常对用户是透明的。但是，这里有一些问题需要思考：

* `Final`方法不能被通知，因为他们不能被覆盖。

* 不需要在classpath中添加CGLIB。Spring3.2以后，CGLIB被重新打包并包含在spring-core的JAR中。也就是说，基于CGLIB的AOP是开箱即用的，JDK动态代理也是如此。



CGLIB代理和动态代理几乎性能差别。在这种情况下，性能不应作为决定性的考虑因素。



### 6.4.6. 使用"Global"全局 Advisors

通过在拦截器名称后添加星号，所有具有与该星号之前的部分匹配的bean名称的advisors都将添加到advisor链中。如果需要添加全局的advisors，这可能会派上用场。下面的例子定义了两个全局的advisors：

```xml
<bean id="proxy" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target" ref="service"/>
    <property name="interceptorNames">
        <list>
            <value>global*</value>
        </list>
    </property>
</bean>

<bean id="global_debug" class="org.springframework.aop.interceptor.DebugInterceptor"/>
<bean id="global_performance" class="org.springframework.aop.interceptor.PerformanceMonitorInterceptor"/>
```



## 6.5. 简洁的代理定义

特别是在定义事务代理时，可能会遇到许多相似的代理定义。使用父子bean定义和子bean定义，以及内部bean定义可以使代理更加简洁明了。



首先，为代理创建父模板，bean定义如下：

```xml
<bean id="txProxyTemplate" abstract="true"
        class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean">
    <property name="transactionManager" ref="transactionManager"/>
    <property name="transactionAttributes">
        <props>
            <prop key="*">PROPAGATION_REQUIRED</prop>
        </props>
    </property>
</bean>
```

它本身不会实例化，因此实际上可能是不完整的。然后，每个需要创建的代理是一个子bean定义，它将代理的目标包装为一个内部bean定义，因为无论如何该目标都不会单独使用。下面的例子展示了这样的子bean：

```xml
<bean id="myService" parent="txProxyTemplate">
    <property name="target">
        <bean class="org.springframework.samples.MyServiceImpl">
        </bean>
    </property>
</bean>
```



可以覆盖来自父模板中的属性。下面的例子，覆盖了事务的传播设置：

```xml
<bean id="mySpecialService" parent="txProxyTemplate">
    <property name="target">
        <bean class="org.springframework.samples.MySpecialServiceImpl">
        </bean>
    </property>
    <property name="transactionAttributes">
        <props>
            <prop key="get*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="find*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="load*">PROPAGATION_REQUIRED,readOnly</prop>
            <prop key="store*">PROPAGATION_REQUIRED</prop>
        </props>
    </property>
</bean>
```

注意父bean的例子，通过将`abstract`属性设置为`true`来将父bean定义显示标记为抽象的，如前所述，因此可能永远不会实例化它。应用程序上下文（但不是简单的bean工厂）默认情况下会预先实例化所有单例。因此，它非常重要（至少对单例bean而言），如果有一个（父）bean定义，打算仅用作模板，并且此定义指定了一个类，则必须确保将`abstract`属性设置为`true`。否则，应用程序上下文实际上会对其进行实例化。



## 6.6. 使用`ProxyFactory`通过编程的方式创建AOP代理

在Spring中，通过编程方式创建AOP代理非常简单。可以不依赖于Spring IoC来使用Spring AOP。



由目标对象实现的接口将被自动代理。下面列出了创建目标对象的代理，包含一个interceptor和一个advisor：

```java
ProxyFactory factory = new ProxyFactory(myBusinessInterfaceImpl);
factory.addAdvice(myMethodInterceptor);
factory.addAdvisor(myAdvisor);
MyBusinessInterface tb = (MyBusinessInterface) factory.getProxy();
```

第一步，构造一个`org.springframework.aop.framework.ProxyFactory`类型的对象。可以使用目标对象创建它，像前面的例子，或指定需要代理的接口。



可以添加通知（用拦截器作为一种特殊的通知），advisors，或两者，并在`ProxyFactory`的声明周期内对其进行操作。如果增加一个`IntroductionInterceptionAroundAdvisor`，可以使代理实现其他接口。



`ProxyFactory`上也有方便的方法（继承自`AdvisedSupport`），可以添加其他通知类型，例如before和throws通知。`AdvisedSupport`是`ProxyFactory`和`ProxyFactoryBean`两者的超类。

> 在多数应用程序中，将AOP代理创建与IoC框架集成在一起是最佳实践。建议使用AOP从Java代码外部化配置。



## 6.7. 操纵通知对象

虽然创建了AOP代理，但可以通过使用`org.springframework.aop.framework.Advised`接口来操纵他们。任何AOP代理对象可以被转换为这个接口，无论它实现了哪个其他接口。这个接口包含了如下方法：

```java
Advisor[] getAdvisors();

void addAdvice(Advice advice) throws AopConfigException;

void addAdvice(int pos, Advice advice) throws AopConfigException;

void addAdvisor(Advisor advisor) throws AopConfigException;

void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

int indexOf(Advisor advisor);

boolean removeAdvisor(Advisor advisor) throws AopConfigException;

void removeAdvisor(int index) throws AopConfigException;

boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

boolean isFrozen();
```

方法`getAdvisors()`返回每个advisor返回一个`Advisor`，interceptor或其他被添加到工厂的通知类型。如果需要一个`Advisor`，该索引处返回的advisor是添加的对象。如果需要一个interceptor或其他通知类型，Spring将它包装在带有总是返回`true`的切点的advisor中。因此，如果增加了一个`MethodInterceprot`，该索引返回的advisor是一个`DefaultPointcutAdvisor`，它返回`MethodInterceprot`和一个与所有类和方法匹配的切点。



方法`addAdvisor()`可以被用来添加任何`Advisor`，通常，advisor持有的切点和通知是通用的`DefaultPointcutAdvisor`，可以将它与任何通知或切点一起使用（但不能用于引入）。



默认情况下，它能够添加，删除advisors或interceptors，即使代理已经创建。唯一的限制是不可能添加或删除引入advisor,因为工厂中已经存在的代理不会显示接口改变。（可以从工厂获取新的代理来避免此问题）。



下面的例子展示了将AOP代理转换为`Advised`接口，检查并操作它的通知：

```java
Advised advised = (Advised) myObject;
Advisor[] advisors = advised.getAdvisors();
int oldAdvisorCount = advisors.length;
System.out.println(oldAdvisorCount + " advisors");

// Add an advice like an interceptor without a pointcut
// Will match all proxied methods
// Can use for interceptors, before, after returning or throws advice
advised.addAdvice(new DebugInterceptor());

// Add selective advice using a pointcut
advised.addAdvisor(new DefaultPointcutAdvisor(mySpecialPointcut, myAdvice));

assertEquals("Added two advisors", oldAdvisorCount + 2, advised.getAdvisors().length);
```



> 尽管可能存在合法的使用案例，但是是否建议修改生产中的业务对象的通知值得怀疑。但是，在开发阶段它非常有用（例如，测试）。有时候发现以拦截器或其他通知的形式添加测试代码，并进入要测试的方法调用中非常有用。（例如，该通知可以进入为该方法创建的事务中，也许可以在将事务标记为回滚之前运行SQL以检查数据库是否已正确更新）。



根据创建代理的方式，通常可以设置`frozen`标志。在那个例子中，`Advised``isFrozen()`方法返回`true`，并且通过添加或删除来修改通知的任何尝试都会导致AopConfigException`。冻结通知对象状态的功能在某些情况下很有用（例如，防止调用代码删除安全拦截器）。



## 6.6. 使用自动代理功能auto-proxy

到目前为止，已经考虑了通过使用`ProxyFactoryBean`或相似的工厂bean来显示创建AOP代理。



Spring也允许使用"auto-proxy"bean定义，它能自动代理选择的bean定义。它基于Spring的bean post processor基础架构，可以在容器加载时修改任何bean定义。



在这个模型中，可以在XML的bean定义文件设置一些特别的bean定义来配置自动代理功能。这使得可以声明合法的自动代理目标而不需要使用`ProxyFactoryBean`。



有两种方式可以达到这个目的：

* 通过使用自动代理创建器，在当前上下文中引用指定的bean。

* 自动代理创建的一种特殊情况是，值得单独思考：由源码级元数据属性驱动的自动代理创建。

### 6.8.1. 自动代理bean定义

这部分涵盖了通过`org.springframework.aop.framework.autoproxy`包提供的自动代理创建器。



**`BeanNameAutoProxyCreator`**

`BeanNameAutoProxyCretor`类是一个`BeanPostProcessor`，它自动为名称、文字值或通配符匹配的bean创建AOP代理。下面的例子展示了如何创建一个`BeanNameAutoProxyCreator`bean：

```xml
<bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
    <property name="beanNames" value="jdk*,onlyJdk"/>
    <property name="interceptorNames">
        <list>
            <value>myInterceptor</value>
        </list>
    </property>
</bean>
```

与`ProxyFactoryBean`一样，有一个`interceptorNames`属性而不是拦截器列表，以允许原型advisor具有正确的行为。名称`interceprots`可以是任何advisors或通知类型。



通常，与自动代理一样，使用`BeanNameAutoProxyCreator`的要点是将相同配置一致地用于多个对象，并且使用最小的配置量。将声明式事务应用于多个对象是一种流行的选择。



在前面例子中的那些匹配名称的bean定义，例如`jdkMyBean`和`onlyJdk`，和目标类一样是普通的bean定义。AOP代理会通过`BeanNameAutoProxyCreator`自动创建。相同的通知会应用于所有匹配的beans。注意，如果使用了advisors（而不是前面例子中的interceprot），切点可以应用于不同地应用于不同的bean。



**`DefaultAdvisorAutoProxyCreator`**

一个更通用和极其强大的自动代理创建器是`DefaultAdvisorAutoProxyCreator`。在当前上下文中，它自动应用于合法的advisors，不需要在advisor的bean定义中指定包含bean的名称。它与`BeanNameAutoProxyCreator`一样，具有一直的配置和避免重复的优点。



使用此机制涉及：

* 指定`DefaultAdvisorAutoProxyCreator`bean 定义。

* 在相同或相关的上下文中，指定任意数量的advisors。注意，这些必须是advisors，不能是interceptors或其他通知。这是必须的，因为必须有一个评估的切点，才能检查是否符合候选bean定义。

`DefaultAdvisorAutoProxyCreator`自动评估包含在每个advisor中的切点，来查看通知是否应该应用于每个业务对象（例如`businessObject1`和`businessObject2`）。



这意味着任意数量的advisors可以被自动地应用于每个业务对象。如果在任何advisors中没有切点与业务对象中的任何方法匹配，则该对象不会被代理。当为新的业务对象添加bean定义时，如有必要，他们会被自动代理。



通常，自动代理的有点是使调用者或依赖着无法获得不通知的对象。在`ApplicationContext`上调用`getBean(businessObject1)`会返回一个代理，而不是目标业务对象（前面显示到“inner bean”也提供了这一好处）。



下面的例子创建了一个`DefaultAdvisorAutoProxyCreator`bean，并且其他元素将在本节中讨论：

```xml
<bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>

<bean class="org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor">
    <property name="transactionInterceptor" ref="transactionInterceptor"/>
</bean>

<bean id="customAdvisor" class="com.mycompany.MyAdvisor"/>

<bean id="businessObject1" class="com.mycompany.BusinessObject1">
    <!-- Properties omitted -->
</bean>

<bean id="businessObject2" class="com.mycompany.BusinessObject2"/>
```

如果想要将相同的通知应用于许多业务对象，则`DefaultAdvisorAutoProxyCreator`非常有用。一旦基础结构定义到位后，可以添加新的业务对象，而无需包括特定的代理配置。也可以轻松的添加其他切面（例如，跟踪或性能监视切面），而对配置的更改更少。



`DefaultAdvisorAutoProxyCreator`提供过滤支持（通过使用名字约定，以便只有某些advisors被评估，从而允许在同一个工厂中使用多个配置不同的AdvisorAutoProxyCreators）和排序。Advisors可以实现`org.springframework.core.Ordered`接口来确保正确的顺序。之前例子中使用的`TransactionAttributeSourceAdvisor`可以配置顺序。默认设置为无序。



## 6.9. 使用`TargetSource`实现

Spring提供`TargetSource`的概念，以`org.springframework.aop.TargetSource`接口表示。这个接口负责返回实现连接点的目标对象。每当AOP代理处理方法调用时，都会想`TargetSource`实现请求目标实例。



使用Spring AOP的开发人员通常不需要直接与`TargetSource`实现一起工作，但这提供了支持池化，可插拔和其他复杂目标的强大方法。例如，通过一个池来管理实例，`TargetSrouce`可以为每次调用返回不同的目标实例。



如果不指定`TargetSource`，默认实现用于包装本地对象。每次都返回相同的目标（与期望的一样）。



本节的其余部分描述了Spring随附的标准目标源以及如何使用他们。



> 当使用自定义的目标源时，目标通常需要是一个原型而不是单例bean定义。这允许Spring当需要时创建一个新的目标实例。



### 6.9.1. 热插拔目标源

`org.springframework.aop.target.HotSwappableTargetSource`的存在是为了允许AOP代理的目标切换，同时允许调用者保留对其的引用。



更改目标来源的目标会立即生效。`HotSwappableTargetSource`是线程安全的。



可以通过HotSwappableTargetSrouce上的`swap()`方法来改变目标：

```java
HotSwappableTargetSource swapper = (HotSwappableTargetSource) beanFactory.getBean("swapper");
Object oldTarget = swapper.swap(newTarget);
```

下面的例子展示了需要的XML定义：

```xml
<bean id="initialTarget" class="mycompany.OldTarget"/>

<bean id="swapper" class="org.springframework.aop.target.HotSwappableTargetSource">
    <constructor-arg ref="initialTarget"/>
</bean>

<bean id="swappable" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="targetSource" ref="swapper"/>
</bean>
```

前面的`swap()`调用更改了可交换的bean的目标。拥有该bean的引用的客户端不知道更改，但立即开始达到新目标。



尽管这个例子没有添加任何通知（不必添加通知来使用`TargetSource`），但可以将任何`TargetSource`与通知结合使用。



**池化的目标源**

使用池目标源提供了与无状态会话EJB相似的编程模型，在无状态会话EJB中，维护了相同实例的池，方法调用将释放池中的对象。



Spring池与SLSB池的关键区别在于，Spring池可应用于任何POJO。通常，与Spring一样，可以以非入侵的方式应用此服务。



Spring提供对Commons Pool 2.2的支持，该池提供了相当有效的池的实现。需要将`commons-pool`Jar放入应用程序的classpath中以便使用这个功能。也可以子类化`org.springframework.aop.target.AbstractPoolingTargetSource`来支持任何的其他池API。

> 还支持Commons Pool 1.5+，但从Spring Framework4.2开始不推荐使用。



下面的例子展示了示例配置：

```xml
<bean id="businessObjectTarget" class="com.mycompany.MyBusinessObject"
        scope="prototype">
    ... properties omitted
</bean>

<bean id="poolTargetSource" class="org.springframework.aop.target.CommonsPool2TargetSource">
    <property name="targetBeanName" value="businessObjectTarget"/>
    <property name="maxSize" value="25"/>
</bean>

<bean id="businessObject" class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="targetSource" ref="poolTargetSource"/>
    <property name="interceptorNames" value="myInterceptor"/>
</bean>
```



注意，目标对象（前面例子中的`businessObjectTarget`）必须是一个原型。这让`PoolingTargetSource`的实现可以创建一个目标的新实例，以根据需要扩展池。参考`AbtractPoolingTargetSrouce`和具体子类来获取更多相关信息。`maxSize`是最基本的，并且始终保证存在。



在这个例子中，`myInterceptor`是拦截器的名称，它需要定义在相同的IoC上下文中。但是，不需要指定拦截器使用池。如果仅希望池化而没有其他通知，则根本不需要设置`interceprotNames`属性。



可以将Spring配置为能够将任何池化对象转换为`org.springframework.aop.target.PoolingConfig`接口，该接口通过介绍来公开有关池的配置和当前大小的信息。需要定义一个类似于以下内容的advisor：

```xml
<bean id="poolConfigAdvisor" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject" ref="poolTargetSource"/>
    <property name="targetMethod" value="getPoolingConfigMixin"/>
</bean>
```

通过在`AbstractPoolingTargetSource`类上调用方便的方法来获得此advisor，因此可以使用`MethodInvokingFactoryBean`。这个advisor的名字必须在公开池对象的`ProxyFactoryBean`中的拦截器名称列表中。



转换定义如下：

```java
PoolingConfig conf = (PoolingConfig) beanFactory.getBean("businessObject");
System.out.println("Max pool size is " + conf.getMaxSize());
```

> 通常不需要合并无状态服务的对象。不认为它应该是默认选择，因为大多数无状态对象自然是线程安全的，并且如果缓存了资源，实例池会成问题。



通过使用自动代理，可以实现更简单的池化，可以设置任何自动代理创建者使用的`TargetSource`实现。



### 6.9.3. 原型目标源

设置原型目标源与设置池化目标源类似。在这种情况下，每次调用方法都会创建一个新的目标实例。尽管在当前JVM中创建一个新的对象成本不是很高，但是连接新对象（满足其IoC依赖性）的成本可能更高。因此，没有充分理由不应该使用此方法。



为此，可以修改前面显示的`poolTargetSource`定义，如下所示（为了清除起见，也更改了名称）：

```xml
<bean id="prototypeTargetSource" class="org.springframework.aop.target.PrototypeTargetSource">
    <property name="targetBeanName" ref="businessObjectTarget"/>
</bean>
```

唯一的属性是目标bean的名称。在`TargetSource`实现中使用继承来确保命名一致。与池化目标源一样，目标bean必须是原型bean定义。



### 6.9.4. `ThreadLocal`目标源

如果需要每次进入请求时创建对象（也就是每个线程），`ThreadLocal`目标源非常有用。`ThreadLocal`的概念提供了JDK范围的功能，可以透明的将资源与线程一起存储。设置`ThreadLocalTargetSource`几乎与其他类型的目标源所说明的相同：

```xml
<bean id="threadlocalTargetSource" class="org.springframework.aop.target.ThreadLocalTargetSource">
    <property name="targetBeanName" value="businessObjectTarget"/>
</bean>
```

> 在多线程和多类加载器环境中错误使用`ThreadLocal`实例时，会遇到严重问题（可能导致内存泄露）。应该使用考虑在其他一些类中包装threadlocal，并且绝对不要直接使用`ThreadLocal`本身（包装类中除外）。另外，应该始终记住正确设置和取消设置线程本地资源（在后者仅涉及对`ThreadLocal.set(null)`的调用）。在任何情况下都应该进行取消设置，因为不取消设置可能会导致问题出现。Spring的`ThreadLocal`支持做到这一点，并且应该始终考虑使用`ThreadLocal`实例，而无需其他适当的处理代码。



## 6.10. 定义一个新的通知类型

Spring AOP是可扩展的。虽然目前在内部使用拦截器实现策略，但是除了在around,before,throws和after之后进行拦截之外，还可以支持任意通知类型。



`org.springframework.aop.framework.adapter`包是一个SPI包，以便支持新的自定义通知类型而不需要改变核心框架。唯一的限制是自定义的`Advice`类型必须实现`org.aopalliance.aop.Advice`标记接口。



参考`org.springframework.aop.framework.adapter`来获取更多信息。


