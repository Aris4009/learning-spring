# 1. Spring Web MVC

Spring Web MVC是构建在Servlet API之上的原始web框架，它从一开始就包含在Spring框架中。正式名称"Spring Web MVC"来源于它的源代码模块（`spring-webmvc`），但是它通常被成为”Spring MVC“。



与Spring Web MVC并行，Spring框架5.0以后引入了一个反应式Web框架，其名称为"Spring WebFlux"，它的名称也来源于源代码模块（`spring-webflux`）。本节涵盖了Spring Web MVC。下一节涵盖了Spring WebFlux。



有关基线信息以及与Servlet容器和Java EE版本范围的兼容性，请参阅Spring Framework Wiki。



## 1.1. DispatcherServlet

 与许多其他web框架一样，Spring MVC是围绕前端控制器模式设计的，其中一个中央`Servlet`， `DispatcherServlet`为请求处理提供了共享算法，而实际工作是由可配置的委托组件执行的。该模型非常灵活，并支持多种工作流程。



`DispatcherServlet`，和任何`Servlet`一样，需要通过使用Java配置或在`web.xml`中根据Servlet规范声明和映射。反过来，`DispatcherServlet`使用Spring配置发现请求映射，视图解析，异常处理等所需的委托组件。



下面的例子是使用Java配置并初始化`DispatcherServlet`，它有Servlet容器自动检测。

```java
public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {

        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class);

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/app/*");
    }
}
```

> 除了直接使用ServletContext API，也可以扩展`AbstractAnnotationConfigDispatcherServletInitializer`并覆盖指定方法。



下面的例子是`web.xml`配置，用来注册和初始化`DispatcherServlet`：

```xml
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/app-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value></param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app</servlet-name>
        <url-pattern>/app/*</url-pattern>
    </servlet-mapping>

</web-app>
```

> Spring Boot遵循不同的初始化顺序。Spring Boot并没有陷入Servlet容器的生命周期，而是使用Spring配置来引导自身和嵌入式Servlet容器。在Spring配置中检测`Filter`和`Servlet`，并在Servlet容器中注册。更多细节，参考Spring Boot Documentation。



### 1.1.1. Context Hierarchy

`DispatcherServlet`需要一个`WebApplicationContext`（普通`ApplicationContext`的扩展）为其自身的配置。`WebApplicationContext`具有指向`ServletContext`和与其关联的`Servlet`链接。它也绑定了`ServletContext`，以便应用程序可以在`RequestContextUtils`上使用静态方法来查找`WebApplicationContext`（如果需要访问他们）。



多余大多数应用，仅有一个`WebApplicationContext`就简单足够了。它也可能具有上下文层次结构，其中一个根`WebAppliationContext`在多个`DispatcherServlet`(或其他`Servlet`)实例之间共享，每个实例都有其自己的子`WebApplicationContext`配置。



根`WebApplicationContext`通常包含基础的beans，例如需要在多个`Servlet`实例之前共享的数据存储和业务服务。这些bean是有效集成的，并且可以在`Servlet`特定子`WebApplicationContext`中重写（即重新声明），该子`WebApplicationContext`通常包含指定`Servlet`本地的bean。下图展示了这个关系：

![](https://raw.githubusercontent.com/Aris4009/attachment/main/20210122173008.png)

下面的例子配置了一个具有层次结构的`WebApplicationContext`:

```java
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { App1Config.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/app1/*" };
    }
}
```

> 如果不需要应用程序上下文层级，应用程序会通过`getRootConfigClasses()`返回所有配置，并且从`getServletConfigClasses()`返回`null`。



下面的例子是与上述例子等效的`web.xml`配置：

```xml
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/root-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app1</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/app1-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app1</servlet-name>
        <url-pattern>/app1/*</url-pattern>
    </servlet-mapping>

</web-app>
```

> 如果不需要应用程序上下文层级，应用程序可能仅配置一个`root`上下文并且将`contextConfigLocation`参数设置为空。



### 1.1.2. 特殊的bean类型

`DispatcherServlet`委托特殊的bean来处理请求和呈现适当的响应。”特殊bean“意味着他们是实现框架约定的Spring管理的对象实例。这些通常带有内置约定，但是可以自定义他们的属性并扩展或替换他们。



下面的表格列出了`DispatcherServlet`检测到的特殊的bean：

| Bean type                                | Explanation                                                                                                                                                                                           |
| ---------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `HandlerMapping`                         | 将请求与拦截器列表一起映射到处理程序，以进行预处理和后处理。映射基于某些条件，其细节因`HandlerMapping`实现而异。<br/>两个`HandlerMapping`的主要实现是`RequestMappingHanlderMapping`（支持`@RequestMapping`注解方法）和`SimpleUrlHandlerMapping`（维护对处理程序的URI路径模式的显式注册）。 |
| `HandlerAdapter`                         | 帮助`DispatcherServlet`调用映射到请求的处理程序，无论处理程序实际上是如何调用的。例如，调用一个注解的控制器需要解析注解。`HandlerAdapter`的主要目的是使`DispatcherServlet`免受此类细节的影响。                                                                            |
| `HandlerExceptionResolver`               | 解析异常的策略，可能将异常映射到处理程序、HTML错误视图或其他目标。                                                                                                                                                                   |
| `ViewResolver`                           | 将处理程序返回的基于逻辑字符串的视图名称解析为实际视图，并将其呈现给响应。参考`视图解析`和`视图技术`。                                                                                                                                                 |
| `LocaleResolver`，`LocaleContextResolver` | 解析客户端正在使用的地区，以及可能的时区，为了能够提供国际化视图。                                                                                                                                                                     |
| `ThemeResolver`                          | 为web应用程序解析可用的主题，例如，提供个性化布局。                                                                                                                                                                           |
| `MultipartResolver`                      | 借助一些multipart解析库来解析multi-part请求的抽象（例如，浏览器表单上传文件）。                                                                                                                                                     |
| `FlashMapManager`                        | 存储和获取输入后和输出，`FlashMap`可以用来被传递从一个请求到另一个请求的属性，通常跨重定向。                                                                                                                                                   |



### 1.1.3. Web MVC配置

应用程序可以在需要处理请求时，声明上述列出的基础bean。`DispatcherServlet`检查每个特殊bean的`WebApplicationContext`。如果没有匹配的bean类型，它将使用`DispatcherServlet.properties`中列出的默认类型。



在大多数情况下，`MVC Config`是最佳七点。它使用Java或XML声明所需的bean，并提供更高级别的配置回调API对其进行自定义。

> Spring Boot依靠MVC Java配置来配置Spring MVC，并提供了许多额外的方便方法。



### 1.1.4. Servlet配置

在Servlet 3.0以上的环境，可以选择以编程方式配置Servlet容器，作为代替`web.xml`文件的方式，或可以结合使用。下面的例子注册了一个`DispatcherServlet`：

```java
import org.springframework.web.WebApplicationInitializer;

public class MyWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {
        XmlWebApplicationContext appContext = new XmlWebApplicationContext();
        appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");

        ServletRegistration.Dynamic registration = container.addServlet("dispatcher", new DispatcherServlet(appContext));
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
```



`WebApplicationInitializer`是一个Spring MVC提供的接口，用来确保实现会被检测并自动用于初始化任何Servlet 3容器。基于它的一个抽象实现是`AbstractDispatcherServletInitializer`，通过覆盖方法，指定servlet映射和`DispatcherServlet`配置的位置，使注册`DispatcherServlet`简化。



建议使用基于java的Spring配置的应用程序这样做，如下面的示例所示:

```java
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MyWebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```



如果使用基于XML的Spring配置，应该从`AbstractDispatcherServletInitializer`直接扩展：

```java
public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        return null;
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        XmlWebApplicationContext cxt = new XmlWebApplicationContext();
        cxt.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
        return cxt;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```



`AbstractDispatcherServletInitializer`也提供了一个简便方法来增加`Filter`实例并且让他们自动映射到`DispatcherServlet`：

```java
public class MyWebAppInitializer extends AbstractDispatcherServletInitializer {

    // ...

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {
            new HiddenHttpMethodFilter(), new CharacterEncodingFilter() };
    }
}
```

每个过滤器都会根据其具体类型添加一个默认名称，并自动映射到`DispatcherServlet`。



`AbstractDispatcherServletInitializer`的`isAsyncSupported`受保护方法提供了一个标志位，以在`DispatcherServlet`及其映射的所有过滤器上启用异步支持。默认情况下，该标志位为`true`。



最后，如果您需要进一步自定义`DispatcherServlet`本身，则可以覆盖`createDispatcherServlet`方法。


