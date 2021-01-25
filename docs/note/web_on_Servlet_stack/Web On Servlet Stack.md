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



对于大多数应用，仅有一个`WebApplicationContext`就简单足够了。它也可能具有上下文层次结构，其中一个根`WebAppliationContext`在多个`DispatcherServlet`(或其他`Servlet`)实例之间共享，每个实例都有其自己的子`WebApplicationContext`配置。



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



在大多数情况下，`MVC Config`是最佳起点。它使用Java或XML声明所需的bean，并提供更高级别的配置回调API对其进行自定义。

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



### 1.1.5. Processing

`DispatcherServlet`处理请求的方式如下：

* 搜索`WebApplicationContext`并将其作为属性绑定到请求中，以便在进程中的controller和其他元素可以使用它。默认使用`WEB_APPLICATION_CONTEXT_ATTRIBUTE`作为key进行绑定。

* 语言环境解析器绑定到请求以便当处理请求时（绘制视图，准备数据等等），进程汇总的元素可以解析本地语言环境。如果不需要语言环境解析，就不需要语言环境解析器。

* 主题解析器绑定到请求以便让视图决定使用的主题。如果不需要主题，可以忽略它。

* 如果指定了multipart文件解析器，则将检查请求中的multiparts。如果没有找到multiparts，请求被包装到`MultipartHttpServletRequest`中，通过进程中的其他元素做进一步处理。

* 搜索合适的处理器。如果处理器被找到，连接处理器（预处理器，后处理器和controllers）的执行链以准备要渲染的模型。另外，带有注解的controller，可以呈现相应（在`HandlerAdapter`），而不是返回视图。

* 如果模型被返回，视图会被绘制。如果没有模型返回（可能由于预处理器和后处理器因为安全原因拦截了请求），没有视图会被绘制，因为请求可能已被满足。



声明在`WebApplicationContext`中的`HandlerExceptionResolver`用来在请求处理期间解析抛出即系异常。这些异常解析器允许自定义逻辑到特定的异常中。



Spring `DispatcherServlet`也支持Servlet API所指定的`last-modification-data`返回。决定指定请求的最后修改日期非常简单：`DispatcherServlet`查找合适的映射处理器并且测试处理器是否实现了`LastModified`接口。如果找到，接口`LastModified`接口中的`long getLastModified(request)`方法的值返回给客户端。



在`web.xml`中的Servlet声明中，可以以通过增加Servlet初始化的参数（`init-param`元素）的形式自定义独立的`DispatcherServlet`实例。下面的表格列出了支持的参数：

| Parameter                      | Explanation                                                                                                                                                                                                                                                                                        |
| ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contextClass                   | 实现了`ConfigurableWebApplicationContext`的类，通过Servlet实例化和本地配置。默认情况下，使用`XmlWebApplicationContext`。                                                                                                                                                                                                     |
| contextConfigLocation          | 传递给上下文实例（通过`contextClass`指定的类的实例）的字符串，用来表名上下文可以在哪被发现。这个字符串包含了多个潜在的字符串（使用逗号分割），用来支持多个上下文。在包含bean的多个上下文中如果定义了两次，最后一个位置优先。                                                                                                                                                                            |
| namespace                      | `WebApplicationContext`命名空间。默认是`[servlet-name]-servlet`                                                                                                                                                                                                                                            |
| throwExceptionIfNoHandlerFound | 当没有为请求找到处理器是，是否抛出一个`NoHandlerFoundException`。异常可以通过`HandlerExceptionResolver`捕获（例如，通过使用一个带有`@ExceptionHanlder`的控制器方法）并且并将其作为其他任何异常进行处理。<br/><br/>默认情况下，这个配置被设置为`false`，在这种情况下，`DispatcherServlet`将响应状态设置为404(NOT_FOUND)，而不会引发异常。<br/><br/>注意，如果默认servlet处理器已经配置，未解析的请求总是转发到默认的servlet上并且永远不会触发404。 |



### 1.1.6. 拦截

所有`HandlerMapping`的实现支持处理拦截器，当想要为某些请求应用特定功能时，非常有用-例如，检查主体。拦截器必须实现来自`org.springframework.web.servlet`包中的`HandlerInterceptor`，它包含三个方法，对于所有类型的预处理、后处理提供了足够的弹性。

* `preHandler(...)`：在实际处理之前运行

* `postHandler(...)`：在处理后运行

* `afterCompletion(...)`：在请求完成后运行



方法`preHandler`返回一个布尔值。可以使用这个方法来打破或继续执行链的处理。当方法返回`true`，处理执行链会继续工作。当返回`false`时，`DispatcherServlet`假设拦截器本身已经处理了请求（例如，绘制适当的视图）并且不会继续执行其他拦截器和执行链中的实际处理器。



参考[Interceptors](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config-interceptors)，来查看如何配置拦截器。可以在独立的`HandlerMapping`的实现上通过使用setters直接注册他们。



注意，对于`@ResponseBody`和`ResponseEntity`方法，postHandle没有那么有用，响应是针对这些方法在`HandlerAdapter`中和`postHandle`之前写入和提交的。这意味着响应的任何改变都太迟了，例如增加额外的header。对于这种情况，可以实现`ResponseBodyAdvice`并且将其声明为 [Controller Advice](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-controller-advice) bean或在`RequestMappingHandlerdapter`上直接配置。



### 1.1.7. 异常

如果在请求映射期间或请求处理时发生异常（例如`@Controller`），`DispatcherServlet`委托`HandlerExceptionResolver` bean的一个链来解析异常并提供可选的处理，通常为错误的响应。



下面的表格列出了可用的`HandlerExceptionResolver`实现：

| HandlerExceptionResolver          | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| SimpleMappingExceptionResolver    | 异常类名称和错误视图名称之间的映射。用来在浏览器应用程序中绘制错误页面。                                                  |
| DefaultHandlerExceptionResolver   | 解决Spring MVC引发的异常，并且将他们映射为HTTP状态码。另请参见`ResponseEntityExceptionHandler`和`REST API 异常`. |
| ResponseStatusExceptionResolver   | 解析带有`@ResponseStatus`注解的异常，并且根据注解中的值，将他们映射到HTTP状态码。                                   |
| ExceptionHandlerExceptionResolver | 通过调用在`@Controller`或`@ControllerAdvice`类中的`@ExceptionHandler`方法解析异常。                   |



**解析链**

在Spring的配置中，通过声明多个`HandlerExceptionResolver` bean并在需要时设置他们的`order`属性，可以形成异常解析链。order属性越高，异常解析器的位置就越晚。



`HandlerExceptionREsolver`规定的约定，可以返回：

* 一个指向错误视图的`ModelAndView`。

* 如果通过解析器处理了异常，返回一个空的`ModelAndView`。

* 如果该异常仍未解决，则为`null`，以拱后续解析器尝试；如果该异常扔在末尾，则允许其冒泡到Servlet容器中。

[MVC Config](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-config)自动为默认Spring MVC异常声明内置的解析器，例如对于`@ResponseStatus`注解的异常和`@ExceptionHandler`支持的方法。可以自定义列表或代替他们。



**容器错误页面**

如果任何`HandlerExceptionResolver`都无法解析异常，因此，该异常可以传播，或者如果响应状态设置为错误状态（即4xx，5xx），则Servlet容器可以在HTML中呈现默认错误页面。为了自定义容器中的默认错误页面，可以在`web.xml`中声明错误页面的映射：

```xml
<error-page>
    <location>/error</location>
</error-page>
```



给定前面的示例，当异常冒泡或响应具有错误状态时，Servlet容器在容器内向配置的URL（例如`/error`）进行error调度。然后有`DispatcherServlet`处理它，可能将其映射为`@Controller`，可以实现该错误以使用模型返回错误视图名称或呈现JSON响应：

```java
@RestController
public class ErrorController {

    @RequestMapping(path = "/error")
    public Map<String, Object> handle(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", request.getAttribute("javax.servlet.error.status_code"));
        map.put("reason", request.getAttribute("javax.servlet.error.message"));
        return map;
    }
}
```



> Servlet API没有提供在Java中创建错误页面映射的方式。但是，可以同事使用`WebApplicationInitializer`和最小的`web.xml`来实现。



### 1.1.8. 视图解析

Spring MVC定义了`ViewResolver`和`View`接口，以便让客户无需绑定特殊的视图技术就可以在浏览器中绘制模型。`ViewResolver`提供视图名称和实际视图之间的映射。`View`处理数据在移交给特定视图技术之前的准备工作。



下面的表格提供了有关`ViewResolver`层结构的更多细节：

| ViewResolver                   | Description                                                                                                                                                                         |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| AbstractCachingViewResolver    | `AbstractCachingViewResolver`的子类缓存视图实例以便用来解析。缓存提高了某些视图技术的性能。可以通过设置`cache`属性为`false`来关闭缓存。此外，如果需要刷新运行时的某些视图（例如，当FreeMarker模板本修改），可以使用`removeFormCache(String viewName,Local loc)`方法。 |
| UrlBasedViewResolver           | `ViewResolver`接口的简单实现，会影响逻辑视图名称到URL的直接解析，而无需显示映射定义。如果逻辑名称以直接的方式与视图资源名称匹配，而不需要任意映射，则这是合适的。                                                                                           |
| InternalResourceViewResolver   | `UrlBasedViewResolver`的便捷子类，支持`INternalResourceView`（实际上是Servlets和JSPs），`JstlView`子类，`TilesView`子类。可以使用`setViewClass(..)`为该解析器生成的所有视图指定视图类。                                         |
| FreeMarkerViewResolver         | `UrlBasedViewResolver`的便捷子类，用来支持`FreeMarkerView`和他们的自定义子类。                                                                                                                          |
| ContentNegotiatingViewResolver | `ViewResolver`接口的实现，用来解析在请求文件名或`Accept` header上的视图。                                                                                                                                 |
| BeanNameViewResolver           | `ViewResolver`接口的实现，在当前应用程序上下文中拦截视图名作为bean名称。这是一个非常灵活的变体，它允许根据不同的视图名称来混合和匹配不同的视图类型。每个这样的`View`可以定义为一个bean，例如在XML或配置类中。                                                              |



**处理**

如果需要，可以通过声明更多的解析器bean，将视图解析器变为链式的，通过设置`order`属性来指定顺序。记住，越高的order属性，视图解析器在链中的定位就越晚。



`ViewResolver`的约定指定了它可以返回null来暗示没有找到视图。但是，在JSPs和`InternalResourceViewResolver`情况下，弄清楚JSP是否存在的唯一方法是通过`RequestDispatcher`进行调度。因此，必须总是将`InternalResoureViewResolver`配置为在视图解析器的总体顺序中的最后一个。



配置视图解析就像将`ViewResolver` bean添加到Spring配置中一样简单。MVC Config为View解析器和添加无逻辑的View Controller提供了专用的配置，这对于无需控制器逻辑的HTML模板呈现非常有用。



**重定向**

特殊的`redirect:`：视图名中的前缀可以执行重定向。`UrlBasedViewResolver`（和它的子类）将其识别为需要重定向的指令。视图名称的其余部分是重定向的URL。



最终效果与控制器返回`RedirectView`的效果相同，但是现在控制器本身可以根据逻辑视图名称进行操作。一个逻辑视图名称（例如`redirect:/myapp/some/resource`）相当于当前Servlet上下文进行重定向，而名称如`redirect://https://myhost.com/some/arbitrary/path`则重定向到绝对URL。



注意，如果controller方法是带有`@ResponseStatus`注解的，注解值优先于`RedirectView`设置的响应状态。



**转发**

可以使用特殊的`forward:`：视图名称的前缀最终是通过`UrlBasedViewResolver`和它的子类解析的。这创建了一个`InternalResourceView`，用来执行`RequestDispatcher.forward()`。因此，这个前缀对于`InternalResourceViewResolver`和`InternalResourceView`（对于JSP）没有用，但是，如果使用另一种视图技术但仍然希望强制有Servlet/JSP引擎处理资源的转发，则可能会有所帮助。注意，也可以改为链接多个视图解析器代替。



**内容协商**

`ContextNegotiatingViewResolver`本身不解析视图，相反，它委托其他解析器，并选择类似于客户端请求的表示的视图。可以从`Accept`头或查询参数（例如，"path?format=pdf"）确定表示形式。



它通过比较请求的媒体类型（也就是`Content-Type`）选择合适的`View`来处理请求。在列表中的第一个兼容`Content-Type`的`View`将表示形式返回给客户端。如果`ViewResolver`链不能提供兼容视图，请查阅通过`DefaultViews`属性指定的视图列表。后一个选项适用于可以呈现当前资源的合适表示形式的单例视图，而与逻辑视图名无关。`Accept`头可以包含通配符（例如`text/*`），这种情况下的`View`会兼容匹配`Context-Type`为`text/xml`的请求。




### 1.1.9. 语言环境

Spring架构的大多数部分都支持国际化，Spring web MVC框架也不例外。`DispatcherServlet`让用户通过使用客户端语言环境自动解析消息。这是通过`LocaleResolver`对象完成的。



当请求进来时，`DispatcherServlet`查找本地化解析器，如果找到，它尝试使用该解析器来设置本地化。通过使用`RequestContext.getLocale()`方法，可以始终检索由语言环境解析器解析的语言环境。



除了自动语言环境解析，也可以将拦截器附加到handler mapping中，来改变特定情况的语言环境。



语言环境解析器和拦截器被定义在`org.springframework.web.servlet.i18n`包中，并且以正常方式在应用程序上下文中配置。Spring包含一下选择的语言环境解析器：

* Time Zone

* Header Resolver

* Cookie Resolver

* Session Resolver

* Locale Interceptor



**Time Zone**

除了获取客户端语言环境，了解时区通常也很有用。`LocalContextResolver`接口提供了对`LocaleResolver`的扩展，以便让解析器提供更丰富的`LocalContext`，包括时区信息。



用户的`TimeZone`通过使用`RequestContext.getTimeZone()`方法获得。通过Spring的`ConversionService`注册的任何日期/时间转换器和格式器对象都会自动使用时区信息。



**Header Resolver**

本地解析器可以通过客户端（例如，web浏览器）发来的请求，检查请求中的`access-language`头。通常，头字段包含客户端操作系统的语言环境。注意，该解析器不支持时区信息。



**Cookie Resolver**

该解析器检查可能存在与客户端的`Cookie`，来查看指定的`Locale`或`TimeZone`。如果是这样，它将使用指定的详细信息。通过使用语言环境解析器，可以指定cookie的名称以及最长使用期限。下面的例子定义了一个`CookieLocaleResolver`：

```xml
<bean id="localeResolver" class="org.springframework.web.servlet.i18n.CookieLocaleResolver">

    <property name="cookieName" value="clientlanguage"/>

    <!-- in seconds. If set to -1, the cookie is not persisted (deleted when browser shuts down) -->
    <property name="cookieMaxAge" value="100000"/>

</bean>
```

下面的表格描述了`CookieLocaleResolver`：

| Property     | Default                   | Description                                               |
| ------------ | ------------------------- | --------------------------------------------------------- |
| cookieName   | classname + LOCALE        | cookie的名称                                                 |
| cookieMaxAge | Servlet container default | cookie在客户端保留的最长时间。如果指定了`-1`,cookie将永远不保留。它仅在客户端关闭浏览器之前可用。 |
| cookiePath   | /                         | 限制cookie对于站点某些部分的可见性。当`cookiePath`被指定，它仅对该路径及其下方的路径可见。    |



**Session Resolver**

`SessionLocaleResolver`可以从相关的客户端请求的session中获取`Locale`和`TimeZone`。对比`CookieLocaleResolver`，该策略将本地选择的语言环境设置存储在Servlet容器的`HttpSession`中。结果，这些设置对于每个会话都是临时的，因此，在每个会话结束时会丢失。



注意，与外部会话管理机制没有直接关系（例如，Spring Session项目）。`SessionLocaleResolvert`评估并修改相应的`HttpSession`属性仅针对当前的`HttpServletRequest`。



**LocaleInterceptor**

可以通过增加`LocaleChangeInterceptor`到一个`HandlerMapping`定义，来开启改变本地语言环境。它检测请求中的参数，并相应的更改语言环境，在调度程序的应用程序上下文中的`LocaleResolver`上调用`setLocale`方法。下面的例子展示了调用所有包含一个名为`siteLanguage`参数的`*.view`资源会改变语言环境。因此，例如，对于一个URL请求，`https://www.sf.net/homeview?siteLanguage=n1`，将站点语言变为荷兰语。下面的例子展示 如何拦截语言环境：

```xml
<bean id="localeChangeInterceptor"
        class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor">
    <property name="paramName" value="siteLanguage"/>
</bean>

<bean id="localeResolver"
        class="org.springframework.web.servlet.i18n.CookieLocaleResolver"/>

<bean id="urlMapping"
        class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    <property name="interceptors">
        <list>
            <ref bean="localeChangeInterceptor"/>
        </list>
    </property>
    <property name="mappings">
        <value>/**/*.view=someController</value>
    </property>
</bean>
```



### 1.1.10. 主题（略...）



### 1.1.11. Multipart Resolver

`MultipartResolver`来自`org.springframework.web.multipart`包，它是用来解析包含文件上传的multipart请求的一个策略。有一个基于Commons FileUpload的实现和另一个基于Servlet 3.0 multipart请求解析的实现。



为了可以处理multipart，需要在`DispatcherServlet`配置中声明一个名为`multipartResolver`的`MultipartResolver` bean。`DispatcherServlet`检测并将它应用于进入的请求。当收到一个带有`multipart/form-data`的POST请求时，解析器解析内容，并将当前的`HttpServletRequest`包装为`MultipartHttpServletRequest`，以提供对已解析部件的访问，同时将它们作为请求参数公开。



**Apache Commons  `FileUpload`**

为了使用Apache Commons `FileUpload`，可以配置一个名为`multipartResolver`，类型为`CommonsMultipartResolver`的bean。也需要在classpath上包含`commons-fileupload`的依赖。



**Servlet 3.0**

Servlet 3.0 multipart解析需要通过Servlet容器配置开启，为此需要：

* 在Java中，在Servlet注册器上设置一个`MultipartConfigElement`。

* 在`web.xml`中，为servlet声明增加一个`"<multipart-config>"`部分。



下面的例子展示了如何在Servlet注册器上设置一个`MultipartConfigElement`：

```java
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // ...

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {

        // Optionally also set maxFileSize, maxRequestSize, fileSizeThreshold
        registration.setMultipartConfig(new MultipartConfigElement("/tmp"));
    }

}
```

一旦Servlet 3.0配置就绪，就可以添加一个名为multipartResolver的StandardServletMultipartResolver类型的bean。



### 1.1.12. 日志

Spring MVC中的debug级别的日志被设计为紧凑，最少且人性化的。它侧重于一遍又一遍的高价值信息，而其他信息仅在调试特定问题时才有用。



trace级别的日志通常与debug遵循的原则一样但是可以被用来debug任何问题。此外，一些日志消息在trace和debug上可能显示不同级别的详细信息。



良好的日志记录来自使用日志的经验。



**敏感数据**

DEBUG和TRACE日志可能会记录敏感信息。这就是默认情况下屏蔽请求参数和header，并且通过`DispatcherServlet`上的`enableLoggingRequestDetails`属性显示启用他们的完整记录的原因。

下面的例子展示了如何通过Java配置来设置：

```java
public class MyInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return ... ;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return ... ;
    }

    @Override
    protected String[] getServletMappings() {
        return ... ;
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("enableLoggingRequestDetails", "true");
    }

}
```










