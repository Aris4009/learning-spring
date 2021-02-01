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
| contextConfigLocation          | 传递给上下文实例（通过`contextClass`指定的类的实例）的字符串，用来表明上下文可以在哪被发现。这个字符串包含了多个潜在的字符串（使用逗号分割），用来支持多个上下文。在包含bean的多个上下文中如果定义了两次，最后一个位置优先。                                                                                                                                                                            |
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

## 1.2. 过滤器

`spring-web`模块提供了一些有用的过滤器：

* Form Data

* Forwarded Headers

* Shallow ETag

* CORS

### 1.2.1. Form Data

浏览器仅可以通过HTTP GET或HTTP POST提交表单数据，但是非浏览器客户端也可以使用HTTP PUT，PATCH，和DELETE。Servlet API要求`ServletRequest.getParameter*()`方法来支持仅对于HTTP POST的表单字段访问。

模块`spring-web`提供`FormContentFilter`用来拦截内容类型为`application/x-www-form-urlencoded`的HTTP PUT，PATCH，和DELETE请求，从请求体中读取表单数据，包装`ServletRequest`以使表单数据可以通过`ServletRequest.getParameter*()`系列方法使用。

### 1.2.2. Forwarded Headers

当请求通过代理进入（例如负载均衡），host，port和scheme可能发生改变，并且，从客户端视角，创建连接并指向正确的host，port和scheme变成了挑战。

`RFC 7239`定义了`Forwarded` HTTP Header，代理可以使用它来提供关于原始请求的信息。这也有其他非标准的header,包括`X-Forwarded-Host`，`X-Forwarded-Port`，`X-Forwarded-Proto`，`X-Forwarded-Ssl`和`X-Forwarded-Prefix`。

`ForwardedHeaderFilter`是一个Servlet过滤器，可以修改请求以便：a)改变基于`Forwarded`头的host,port和scheme；b)删除这些头以消除进一步的影响。该过滤器依赖于包装请求，因此必须先于其他过滤器（例如`RequestContextFilter`）进行排序，该过滤器应该与修改后的请求一起使用而不是原始请求。

对于转发头，这里有安全性的考虑，因为应用程序不知道请求头是通过代理添加的，还是有恶意客户端添加的。这就是为什么应该将位于信任边界的代理配置为删除来自外部的不受信任的转发头。还可以使用`removeOnluy=true`配置`ForwardedHeaderFilter`，在这种个情况下，它将删除但不适用头。

为了支持异步请求和错误调度，该过滤器应该和`DispatcherType.ASYNC`和`DispatcherType.ERROR`映射。如果使用Spring框架的`AbstractAnnotationConfigDispatcherServletInitializer`，所有过滤器自动为所有调度类型自动注册。但是，如果通过`web.xml`或在Spring Boot，通过`FilterRegistrationBean`注册，请确保包含DispatcherType.ASYNC和DispatcherType.ERROR，除了DispatcherType.REQUEST以外。

### 1.2.3. Shallow ETag

`ShallowEtagHeaderFilter`过滤器通过缓存写入响应的内容，并计算出MD5哈希值来创建`shallow` ETag。客户端下一次发送时，它会执行相同的操作，但是还会将计算值与If-None-Match请求标头进行比较，如果两者相等，则返回304（NOT_MODIFIED）。

这种策略可以节省网络带宽，但不能节省CPU，因为必须为每个请求计算完整响应。如前所述，控制器级别的其他策略可以避免计算。请参阅HTTP缓存。

该过滤器具有`writeWeakETag`参数，该参数将过滤器配置为写入弱ETag，类似于以下内容：W /“ 02a2d595e6ed9a0b24f027f2b63b134d6”（在RFC 7232第2.3节中定义）。

为了支持异步请求，此过滤器必须与DispatcherType.ASYNC映射，以便过滤器可以延迟并成功生成ETag到最后一个异步调度的结尾。如果使用Spring Framework的AbstractAnnotationConfigDispatcherServletInitializer（请参阅Servlet配置），则会为所有调度类型自动注册所有过滤器，但是，如果通过web.xml或在Spring Boot中通过FilterRegistrationBean注册过滤器，请确保包括DispatcherType.ASYNC。

### 1.2.4. 跨域

Spring MVC通过控制器上的注解为CORS配置提供了细粒度的支持。但是，当与Spring Security一起使用时，我们建议您依赖内置的`CorsFilter`，该`CorsFilter`顺序必须在Spring Security的过滤器链之前。

## 1.3. 注解的Controllers

Spring MVC提供一个基于注解的编程模型，`@Controller`和`@RestController`组件使用注解来表示请求映射，请求输入，异常处理等等。注解的控制器有弹性的方法签名并且不需要扩展基类或实现任何特定接口。下面的例子展示了通过注解定义的一个控制器：

```java
@Controller
public class HelloController {

    @GetMapping("/hello")
    public String handle(Model model) {
        model.addAttribute("message", "Hello World!");
        return "index";
    }
}
```

在前面的例子中，方法接收一个`Model`并以字符串形式返回视图，但是存在其他选项，本章稍后对其进行说明。

### 1.3.1. 声明

可以在Servlet的`WebApplicationContext`中通过使用标准Spring bean定义来定义controller beans。样板`@Controller`允许自动检测，与Spring对在classpath中检测`@Component`类并为其自动注册bean定义的常规支持保持一致。它还充当了带注解类的样板，表明其作为Web组件的作用。

为了对注入`@Controller` beans开启自动检测，可以在Java配置中添加组件扫描，像下面的例子一样：

```java
@Configuration
@ComponentScan("org.example.web")
public class WebConfig {

    // ...
}
```

下面的例子是XML配置，与上面的例子等效：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.example.web"/>

    <!-- ... -->

</beans>
```

`@RestController`是一个组合注解，它本身是带有`@Controller`和`@ResponseBoyd`的元注解，用来表示每个方法继承顶级`@ResponseBody`注解，因此，直接将其写入响应体，而不是视图解析和使用HTML模板进行渲染。

**AOP Proxies**

在有些情况下，可能需要通过运行时AOP代理装饰一个controller。一个例子是如果选择将`@Transactional`注解直接用在controller上。当在这种情况下时，专门针对controller，建议使用基于类的代理。这通常对controller来说是默认选择。但是，如果controller必须实现的接口不是Spring上下文回调的接口（例如`InitializingBean`，`*Aware`，和其他），可能需要明确配置基于类的代理。例如，对于`<tx:annotation-driven/>`，需改改变为`<tx:annotation-driven proxy-target-class="true"/>`，并且对于`@EnableTransactionManagement`,需要变为`@EnableTransactionManagement(proxyTargetClass = true)`。

### 1.3.2. Request Mapping

可以使用`@RequestMapping`注解来将请求映射到controllers方法。可以通过多种属性来匹配-URL，HTTP方法，请求参数，headers和media types。可以在类级别上使用它来表示共享的映射或在方法级别来缩小到指定的端点映射。

这里有一些`@RequestMapping`的特殊缩写：

* `@GetMapping`

* `@PostMapping`

* `@PutMapping`

* `@DeleteMapping`

* `@PatchMapping`

这些缩写被当做自定义注解来提供，因为可以说，大多数控制器方法应该映射到特定的HTTP方法，而不是使用`@RequestMapping`，后者默认情况下与所有HTTP方法匹配。同时，在类级别仍需要`@RequestMapping`来表示共享映射。

下面的例子有类型和方法级别的映射：

```java
@RestController
@RequestMapping("/persons")
class PersonController {

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable Long id) {
        // ...
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody Person person) {
        // ...
    }
}
```

**URI模式**

`@RequestMapping`方法可以使用URI模式来映射。有两种选择：

* `PathPattern`-根据URI路径的预解析模式，该路径也预解析`PathContainer`。该解决方案转为web使用设计，可以有效处理编码和参数路径，并有效匹配。

* `AntPathMatcher`-根据字符串路径匹配字符串模式。这是一种原始的解决方案，这在Spring配置中还用于在类路径，文件系统和其他位置上选择资源。它效率较低，并且字符串路径输入对于有效处理URI的编码和其他问题是一个挑战。

对于web应用程序，`PathPattern`是建议的解决方案并且在Spring WebFlux中它是唯一选择。在5.3版本之前，`AntPathMatcher`在Spring MVC中是唯一的选择并且仍然是默认选择。但是，在`MVC config`中可以开启`PathPattern`。

`PathPattern`像`AntPathMatcher`一样支持模式语法。此外，它也支持捕获模式，例如：`{*spring}`，可以用于匹配路径末尾的0个或更多路径段。`PathPattern`还限制了使用`**`来匹配更多路径段，以便仅在模式末尾才允许使用。当为指定请求选择最佳匹配模式时，这消除了许多歧义。对于全部的匹配模式语法，请参考[PathPattern](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/web/util/pattern/PathPattern.html) 和 [AntPathMatcher](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/util/AntPathMatcher.html)。

一些模式样例：

* `"/resources/ima?e.png"`-在路径段匹配一个字符

* `"/resources/*.png"`-在路径段匹配0个或多个字符

* `"/resources/**"`-匹配多个路径段

* `"/projects/{project}/versions"`-匹配一个路径段并将它作为变量捕获

* `"/projects/{project:[a-z]+}/version"`-匹配并捕获带有正则表达式的变量

可以通过`@PathVariable`来访问捕获到的URI变量，例如：

```java
@GetMapping("/owners/{ownerId}/pets/{petId}")
public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
    // ...
}
```

可以在类和方法级别上声明URI变量：

```java
@Controller
@RequestMapping("/owners/{ownerId}")
public class OwnerController {

    @GetMapping("/pets/{petId}")
    public Pet findPet(@PathVariable Long ownerId, @PathVariable Long petId) {
        // ...
    }
}
```

URI变量会自动转换为合适的类型或引发`TypeMismatchException`。默认支持简单类型简单类型（`int`，`long`，`Date`等等）并且可以注册任何其他数据类型来提供支持。

可以显示命名URI变量名称（例如，`@PathVariable("customId")`），但是如果名称相同并且代码使用调试信息或使用Java 8上的`-parameters`编译器标志编译的，则可以忽略该详细信息。

语法`{varName:regex}`声明了一个带有正则表达式的URI变量。例如，指定URL `"spring-web-3.0.5 .jar"`，下面的方法提取了名称，版本和文件扩展：

```java
@GetMapping("/{name:[a-z-]+}-{version:\\d\\.\\d\\.\\d}{ext:\\.[a-z]+}")
public void handle(@PathVariable String name, @PathVariable String version, @PathVariable String ext) {
    // ...
}
```

URI路径模式也有内置的`${...}`占位符，根据本地语言环境，系统，环境和其他property源，通过使用`PropertyPlaceHolderConfigurer`来解析占位符。例如，可以使用它来基于某些外部配置参数化一个基础URL。

**模式对比**

当多种模式匹配了一个URL后，会选择最佳匹配项。根据是否启用了已解析的`PathPattern`，使用以下一种方法来完成此操作：

* [`PathPattern.SPECIFICITY_COMPARATOR`](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/web/util/pattern/PathPattern.html#SPECIFICITY_COMPARATOR)

* [`AntPathMatcher.getPatternComparator(String path)`](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/util/AntPathMatcher.html#getPatternComparator-java.lang.String-)

两者都有助于对模式进行排序，并在上面放置更具体的模式。如果模式的URI变量（计数为1），单个通配符（计数为1）和双通配符（计数为2）的数量较少，则模式的含义不太明确。给定相等的分数，则选择更长的模式。给定相同分数和长度，URI变量的数量比通配符多的模式会被选择。

默认映射模式（`/**`）从评分中被排除，并且使用排在最后。同样，前缀模式（例如`/public/**`）被认为比其他没有双通配符的模式更具体。

**后缀匹配**

从5.3以后，Spring MVC默认情况下不再执行`.*`的后缀模式来匹配映射为`/person`也隐含的映射`/person.*`的controller。因此，路径扩展不再用于解释响应的请求内容类型-例如，`/person.pdf`，`/person.xml`。

当浏览器用于发送难以一致解释的`Accept`头时，以这种方式使用文件扩展名是必要的。目前，这已经不再是必须的，使用`Accept`头应该是首选。

随着时间流逝，文件扩展名的使用已经被证明有各种问题。当使用URI变量时，路径参数和URI编码进行覆盖时，可能会引起歧义。关于基于URL的授权和安全性的推理也变得更加困难。

要完全禁用5.3之前版本中的路径扩展，请设置以下内容：

* `useSuffixPatternMatching(false)`

* `favorPathExtension(false)`

除了通过`Accept`头之外，还有一种请求内容类型的方法仍然很有用，例如在浏览器中输入URL时。路径扩展的一种安全代替方法是使用查询参数策略。如果必须使用文件扩展，考虑通过`ContentNegotiationConfigurer`的`mediaTypes`来限制为显示注册的扩展列表。

**后缀匹配和RFD**

反射文件下载（RFD）攻击与XSS攻击相似，因为它依赖于响应中反应的请求输入（例如，查询参数和URI变量）。但是，RFD攻击不是将JavaScript插入到HTML，而是依靠浏览器切换来执行下载，并在以后双击时将响应视为可执行脚本。

在Spring MVC中，`@RequestBody`和`ResponseEntity`方法是有风险的，因为他们可以绘制为不同的内容类型，因为客户端可以通过URL路径扩展来请求。关闭后缀模式匹配和为内容写上使用路径扩展可以有效降低风险，但是对于组织RFD统计它不满足。

为了阻止RDF攻击，Spring MVC在呈现响应体之前添加了`Content-Disposition:inline;filename=f.txt`头，以建议提供固定且安全的下载文件。仅当URL路径包含既不被视为安全也不被明确注册以用于内容写上的文件扩展名时，才执行此操作。但是，当直接在浏览器中键入URL时，它可能会产生副作用。

许多常见路径扩展在默认情况下都被视为安全的。具有自定义`HttpMessageConverter`实现的应用程序可以显示注册文件扩展名以进行内容写上，以避免为这些扩展名添加`Content-Disposition`头。

参考 [CVE-2015-5211](https://pivotal.io/security/cve-2015-5211) 获取更多相关RFD的其他建议。

**可消费的媒体类型**

可以在基于请求的`Content-Type`上缩小请求映射：

```java
@PostMapping(path = "/pets", consumes = "application/json") 1
public void addPet(@RequestBody Pet pet) {
    // ...
}
```

<mark>1</mark>使用`consumes`属性通过content type来缩小映射。

该属性也支持否定表达式-例如`!text/plain`意味着除了`text/plain`之外的任何内容。

可以在类级别上尚明一个共享的`consumes`属性。但是，与绝大多数其他请求映射属性不同，当在类级别使用该属性时，方法级别的`consumes`属性会覆盖类级别声明，而不是扩展它。

> `MediaType`为常用的media types提供了常量，例如：`APPLICATION_JSON_VALUE` 和`APPICATION_XML_VALUE`。

**可生产的媒体类型**

可以在基于请求头的`Accept`上缩小请求映射，并且列出了内容类型是控制器方法可产生的：

```java
@GetMapping(path = "/pets/{petId}", produces = "application/json")1 
@ResponseBody
public Pet getPet(@PathVariable String petId) {
    // ...
}
```

<mark>1</mark>使用`produces`属性来缩小映射

该媒体类型可指定为字符集。也支持否定表达式，例如`!text/plain`意味着除了`text/plain`的其他任意内容类型。

可以在类级别上声明一个共享的`produces`属性。不像绝大多数请求映射属性会扩展类级别的声明，该属性当在类级别上使用时，方法级别的属性会覆盖类级别的属性。

> `MediaType`为常用的media types提供了常量，例如：`APPLICATION_JSON_VALUE` 和`APPICATION_XML_VALUE`。

**参数，headers**

可以在请求参数条件上缩小请求映射。可以测试是否存在请求参数（`myParam`）,是否存在一个请求参数（!myParam）或特定值（myParam=myValue）：

```java
@GetMapping(path = "/pets/{petId}", params = "myParam=myValue") 1
public void findPet(@PathVariable String petId) {
    // ...
}
```

<mark>1</mark>测试`myParam`是否等于`myValue`

也可以使用相同的请求头提交件：

```java
@GetMapping(path = "/pets", headers = "myHeader=myValue") 1
public void findPet(@PathVariable String petId) {
    // ...
}
```

<mark>1</mark>测试`myHeader`是否等于`myValue`

**HTTP HEAD，OPTIONS**

`@GetMapping`（和`@RequestMapping(method=HttpMethod.GET)`）透明地支持HTTP HEAD来进行请求映射。控制器方法不需要改变。应用于`javax.servlet.http.HttpServlet`的相应包装器确保`Content-Length` header设置为写入的字节数（实际上未写入响应）。

`@GetMapping`（和`@RequestMapping(method=HttpMethod.GET)`）是隐含映射并且支持HTTP HEAD的。像处理HTTP GET一样处理HTTP HEAD请求，不同的是，不是写入正文，而是计算字节数并设置`Content-Length` header。

默认情况下，通过将`Allow`设置为所有具有匹配URL模式的`@RequestMapping`方法总列出的HTTP方法列表来处理HTTP OPTIONS。

对于没有声明HTTP方法的`@RequestMapping`，`Allow`被设置为`GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS`。控制器方法应该总是声明支持的HTTP方法（例如，通过使用HTTP方法特殊的变种：`@GetMapping`，`@PostMapping`等等）。

可以将`@RequestMapping`方法显式映射到HTTP HEAD和HTTP OPTIONS，但这在通常情况下没有必要。

**自定义注解**

Spring MVC支持使用请求映射的组合注解。这些注解本身具有`@RequestMapping`的元数据并且以更狭窄更具体的母体通过组合重新声明`@RequestMapping`属性的子集（或全部）。

`@GetMapping`，`@PostMapping`，`@PutMapping`，`@DeleteMapping`和`@PatchMapping`是符合注解的例子。之所以提供他们是因为，大多数控制器方法应该映射到特定的HTTP方法，而不是使用`@RequestMapping`，后者默认情况下匹配所有HTTP方法。如果需要符合注解的例子，可以参考他们如何来声明。

Spring MVC也支持通过自定义请求匹配逻辑来自定义请求映射属性。这是一个高级选项，它需要子类化`RequestMappingHandlerMapping`和覆盖`getCustomMethodCondition`方法，可以在其中检查自定义属性并且返回自己的`RequestCondition`。

**明确注册**

可以使用编程方式注册处理方法，它可以用来动态注册或高级案例，例如统一处理程序在不同URL下的不同实例：

```java
@Configuration
public class MyConfig {

    @Autowired
    public void setHandlerMapping(RequestMappingHandlerMapping mapping, UserHandler handler) 1
            throws NoSuchMethodException {

        RequestMappingInfo info = RequestMappingInfo.paths("/user/{id}").methods(RequestMethod.GET).build(); 2

        Method method = UserHandler.class.getMethod("getUser", Long.class); 3

        mapping.registerMapping(info, handler, method); 4
    }
}
```

<mark>1 </mark>注入目标处理器和处理器的处理程序映射

<mark>2 </mark>准备请求映射元数据

<mark>3 </mark>获取处理方法

<mark>4 </mark>添加注册

### 1.3.3. 处理方法

`@RequestMapping`处理方法具有灵活的签名，可以从一系列受支持的控制器方法参数和返回值中进行选择。

**方法参数**

下面的表格描述了支持控制器方法的参数。反应式类型不支持任何参数。

支持JDK 8的`java.util.Optiolan`作为方法参数，并与具有必须属性（例如，`@RequestParam`，`@RequestHeader`等）的注解结合在一起，等效与`required=false`

| Controller method argument                                                       | Description                                                                                                                                  |
| -------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `WebRequest`, `NativeWebRequest`                                                 | 通用访问请求参数，以及请求和会话属性，而无需直接使用Servlet API。                                                                                                       |
| `javax.servlet.ServletRequest`, `javax.servlet.ServletResponse`                  | 可以选择任何指定的请求或相应类型-例如，`ServletRequest`,`HttpServletRequest`，或Spring的`MultipartRequest`，`MultipartHttpServletRequest`。                          |
| `javax.servlet.http.HttpSession`                                                 | 强制session的存在。结果，该参数永远不为`null`。注意，session访问不是线程安全的。如果允许多个请求并发访问session，请考虑使用`RequestMappingHandlerAdapter`实例的`synchronizeOnSession`标志为`true`。 |
| `javax.servlet.http.PushBuilder`                                                 | 用于程序化HTTP/2资源推送的Servlet 4.0推送构建器API。注意，根据Servlet规范，如果客户端不支持HTTP/2，则注入的`PushBuilder实例可以为null。                                                 |
| `java.security.Principal`                                                        | 当前经过身份验证的用户-可能是特定`Principal`实现类。                                                                                                             |
| `HttpMethod`                                                                     | 请求的HTTP方法                                                                                                                                    |
| `java.util.Locale`                                                               | 当前请求的语言环境，通过多数指定的可用`LocaleResolver`来决定（实际上，是配置的`LocaleResolver`或`LocaleContextResolver`）。                                                    |
| `java.util.TimeZone`+`java.time.ZoneId`                                          | 当前请求的时区，通过`LocaleCOntextResolver`来决定。                                                                                                        |
| `java.io.InputStream`，`java.io.Reader`                                           | 用于访问Servlet API公开的原始请求正文                                                                                                                     |
| `java.io.OutputStream`，`java.io.Writer`                                          | 用于访问Servlet API公开的原始响应                                                                                                                       |
| `@PathVariable`                                                                  | 用于访问URI模板变量                                                                                                                                  |
| `@MatrixVariable`                                                                | 用于访问在URI路径段中的name-value对                                                                                                                     |
| `@RequestParam`                                                                  | 用户访问Servlet请求参数，包括multipart文件。参数值被转换为声明方法的参数类型。<br/><br/>注意对于单个参数值，`@RequestParam`是可选的。                                                      |
| `@RequestHeader`                                                                 | 用于访问请求头。Header值会被转换为声明方法的参数类型。                                                                                                               |
| `@CookieValue`                                                                   | 用于访问cookies。Cookies值会被转换为声明方法中的参数类型。                                                                                                         |
| `@RequestBody`                                                                   | 用于访问HTTP请求体。内容通过使用`HttpMessageConverter`的实现来转换为声明方法的参数类型。                                                                                    |
| `@HttpEntity<B>`                                                                 | 用于访问请求头和请求体。请求体使用`HttpMessageConverter`来转换。                                                                                                  |
| `@RequestPart`                                                                   | 用于访问在`multipart/form-data`请求的部分，使用`HttpMessageConverter`来转换部分请求体。                                                                            |
| `java.util.Map`，`org.springframework.ui.Model`，`org.springframework.ui.ModelMap` | 用于访问HTML控制器中使用的模型，并作为视图渲染的一部分公开给模板。                                                                                                          |
| `RedirectAttributes`                                                             | 指定在重定向的情况下使用的属性（即追加到查询字符串中），并指定要临时存储直到重定向后的请求的Flash属性。                                                                                       |
| `@ModelAttribute`                                                                | 访问应用了数据绑定和验证的模型中存在的属性（如果不存在，进行实例化）。<br/><br/>注意使用`@ModelAttribute`是可选的（例如，设置它的属性）。                                                           |
| `Errors`，`BindingResult`                                                         | 用于访问命令对象（也就是说`@ModelAttribute`参数）的验证和数据绑定中的错误，或验证来自`@RequestBody`或`@RequestPart`参数的错误。必须在经过验证的方法参数之后立即声明一个`Errors`或`BindingResult`参数。        |
| `SessionStatus`+class-level `@SessionAttributes`                                 | 为了标记表单处理完成，将触发清除通过类级别`@SessionAttributes`注解声明的会话属性。                                                                                          |
| `UriComponentsBuilder`                                                           | 用于准备相对于当前请求的主机、端口、scheme、上下文路径和servlet映射的文字部分的URL。                                                                                           |
| `@SessionAttribute`                                                              | 用于访问任何session属性，与通过类级别`@SessionAttributes`声明存储在会话中的模型属性相反。                                                                                   |
| `@RequestAttribute`                                                              | 用于访问请求属性。                                                                                                                                    |
| 其他参数                                                                             | 如果方法参数与该表中的任何较早值都不匹配，并且为简单类型（由BeanUtils＃isSimpleProperty确定，则将其解析为@RequestParam。否则，将其解析为@ModelAttribute。                                      |

**返回值**

下面的表格描述了控制器方法支持的返回值。反应式类型支持所有的返回值。

| Controller method return value                                                                              | Description                                                                                                                                                                                                                                 |
| ----------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `@ResponseBody`                                                                                             | 返回值通过`HttpMessageConverter`的实现转换并写入响应。                                                                                                                                                                                                      |
| `HttpEntity<B>`，`ResponseEntity<B>`                                                                         | 返回值被指定为完全的响应（包括HTTP headers和body），通过`HttpMessageConverter`的实现进行转换并写入响应。                                                                                                                                                                     |
| `HttpHeaders`                                                                                               | 用于返回只有headers但没有body的响应                                                                                                                                                                                                                     |
| `String`                                                                                                    | 通过`ViewResolver`的实现解析的视图名，并且与隐式模型一起使用-通过命令对象和`@ModelAttribute`方法来决定。处理程序方法还可以通过声明`Model`参数以编程方式丰富模型。                                                                                                                                        |
| `View`                                                                                                      | 用来与隐式模型一起绘制`View`实例-通过命令对象和`@ModelAttribut`方法决定。处理方法也可以通过声明`Model`参数以编程方式丰富模型。                                                                                                                                                              |
| `java.util.Map`，`org.springframework.ui.Model`                                                              | 将属性加入到隐式模型，通过`RequestToViewNameTranslator`来隐式决定视图名                                                                                                                                                                                          |
| `@ModelAttribute`                                                                                           | 将属性加入到模型，通过`RequestToViewNameTranslator`来隐式决定视图名。<br/><br/>注意，`@ModelAttribute`是可选的。                                                                                                                                                        |
| `ModelAndView`对象                                                                                            | 要使用的视图和模型属性，以及相应状态（可选）。                                                                                                                                                                                                                     |
| `void`                                                                                                      | 如果带有`void`返回类型的方法（或`null`返回值）还具有`ServletResponse`，`OutputStream`参数或`@ResponseStatus`注解，则认为该方法已完全处理了响应。如果控制器进行了肯定的`ETag`或`lastModified`时间戳检查，也是如此。<br/><br/>如果上面的都不成立，`void`返回类型也暗示对于REST controllers来说没有响应体或对于HTML controllers来说表示默认视图名称选择。 |
| `DeferredResult<V>`                                                                                         | 从任何线程异步产生任何上述返回值-例如，某些事件或回调结果。                                                                                                                                                                                                              |
| `Callable<V>`                                                                                               | 在Spring MVC管理的线程中异步产生上述任何返回值。                                                                                                                                                                                                               |
| `ListenableFuture<V>`，`java.util.concurrent.COmpletionStage<V>`，`java.util.concurrent.COmpletableFuture<V>` | 为方便起见（例如，当基础服务返回其中之一时），代替`DeferredResult`。                                                                                                                                                                                                  |
| `ResponseBodyEmitter`，`SseEmitter`                                                                          | 异步发出要通过`HttpMessageConverter`实现将对象流写入响应的对象。也支持作为`ResponseEntity`的实体。                                                                                                                                                                        |
| `StreamingResponseBody`                                                                                     | 异步写入响应`OutputSteram`。也支持`ResponseEntity`作为响应体。                                                                                                                                                                                              |
| Reactive type-Reactor，RxJava或其他类型`ReactiveAdapterRegistry`                                                  | `DeferredResult`的替代方法，其中包含收集到的`List`的多值流。<br/><br/>对于流场景(例如，`text/event-stream`，`application/json+stream`)，使用`SseEmitter`和`ResponseBodyEmitter`，其中`ServletOutputStream`阻塞I/O在一个Spring mvc管理的线程上执行，并在每个写完成时应用反压力                             |
| 其他返回值                                                                                                       | 任何不匹配之前列表的返回值,例如`String`或`void`都被视为一个视图名称(默认视图名称选择通过`RequestToViewNameTranslator`适用),它不是一个简单的类型,由BeanUtils # isSimpleProperty提供。简单类型的值仍然无法解析。                                                                                               |

**类型转换**

一些注解控制器方法的参数，表示基于`String`的请求输入（例如，`@RequestParam`，`@RequestHeader`，`@PathVariable`，`@MatrixVariable`和`@CookieValue`）,如果参数声明为`String`以外的值，就需要类型转换。

对于这些情况，类型转换会自动应用基于配置好的转换器。默认情况下，简单类型（`int`，`long`，`Date`等）都支持。可以通过`WebDataBinder`来自定义类型转换或通过想`FormattingConversionService`注册`Formatters`。

类型转换中的一个实际问题是处理空的String源值。如果该值由于类型转换而变为`null`，则将其视为丢失。`Long`，`UUID`和其他目标类型可能就是这种情况。如果想要允许`null`被注入，要么在注解参数上使用`reqiured`标志，要么声明参数为`@Nullable`。

> 5.3以后，即使在类型转换之后，也将强制使用非null参数。如果处理方法倾向于接受null值，要么声明参数为`@Nullable`或将它标记为`reuired=false`在相应的`@RequestParam`等注解上。这是对于5.3升级中遇到的回归问题的推荐解决方案。
> 
> 另外，也可以专门处理在必须的`@PathVariabled`的情况下，将导致由此产生的`MissingPathVariableException`。转换后的null将被视为空的原始值，因此将抛出对应的`Missing ... Exception`变体。

**矩阵变量**

RFC 3986讨论了在路径段中的name-value对。在Spring MVC中，根据Tim Berners-Lee的就帖子将其称作为矩阵变量，但也可以成为URI路径参数。

矩阵变量可以出现在任何路径段中，变量通过分号分割，多个值通过逗号分割（例如`/cars;color=red,green;year=2012`）。通过重复变量名，也可以指定多个值（例如，`color-red;color-green;color=blue`）。

如果URL希望包含矩阵变量，控制器请求方法的请求映射必须使用URI变量来屏蔽该变量内容并且确保可以成功匹配请求，而与矩阵变量的顺序和存在无关：

```java
// GET /pets/42;q=11;r=22

@GetMapping("/pets/{petId}")
public void findPet(@PathVariable String petId, @MatrixVariable int q) {

    // petId == 42
    // q == 11
}
```

指定所有路径段可能包含的矩阵变量，可能有时候需要消除矩阵比那辆应位于哪个路径变量的歧义：

```java
// GET /owners/42;q=11/pets/21;q=22

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable(name="q", pathVar="ownerId") int q1,
        @MatrixVariable(name="q", pathVar="petId") int q2) {

    // q1 == 11
    // q2 == 22
}
```

矩阵变量可以被定义为可选的和指定默认值：

```java
// GET /pets/42

@GetMapping("/pets/{petId}")
public void findPet(@MatrixVariable(required=false, defaultValue="1") int q) {

    // q == 1
}
```

为了获取所有矩阵变量，可以使用`MultiValueMap`：

```java
// GET /owners/42;q=11;r=12/pets/21;q=22;s=23

@GetMapping("/owners/{ownerId}/pets/{petId}")
public void findPet(
        @MatrixVariable MultiValueMap<String, String> matrixVars,
        @MatrixVariable(pathVar="petId") MultiValueMap<String, String> petMatrixVars) {

    // matrixVars: ["q" : [11,22], "r" : 12, "s" : 23]
    // petMatrixVars: ["q" : 22, "s" : 23]
}
```

注意，需要开启使用矩阵变量。在MVC Java配置中，需要通过Path Matching设置具有`removeSemicolonContent=false`的`UrlPathHelper`。在MVC XML命名空间中，可以设置`<mvc:annotation-driven enable-matrix-variables="true"/>`。

**`@RequestParam`**

可以使用该注解来将Servlet请求参数（也就是说，查询参数或表单参数）绑定到控制器中的方法参数：

```java
@Controller
@RequestMapping("/pets")
public class EditPetForm {

    // ...

    @GetMapping
    public String setupForm(@RequestParam("petId") int petId, Model model) {1 
        Pet pet = this.clinic.loadPet(petId);
        model.addAttribute("pet", pet);
        return "petForm";
    }

    // ...

}
```

<mark>1</mark>使用`@RequestParam`绑定`petId`。

默认情况下，使用该注解的方法参数是必须的，但是也可以通过将该注解的`required`标志设置为`false`或通过`java.util.Optional`包装器声明参数来将它变为可选项。

如果目标方法参数类型不是`String`，类型转换会自定应用。

将参数类型声明为数组或列表，可以为同一参数名称解​​析多个参数值。

当该注解被声明为`Map<String,String>`或`MultiValueMap<String,String>`，在注解中不需要指定参数名称，将使用每个指定的参数名和请求参数值填充到map中。

注意，使用`@RequestParam`是可选的（例如，设置它的属性）。默认情况下，任何简单值类型的参数（由BeanUtils#isSimpleProperty决定），并且没有被任何其他参数解析器解析，就如同使用`@RequestParam`进行了注解一样。

**`@RequestHeader`**

可以使用`@RequestHeader`注解将请求头绑定到控制器方法的参数上。

思考下面的带有headers的请求：

```
Host                    localhost:8080
Accept                  text/html,application/xhtml+xml,application/xml;q=0.9
Accept-Language         fr,en-gb;q=0.7,en;q=0.3
Accept-Encoding         gzip,deflate
Accept-Charset          ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive              300
```

下面的例子获取了`Accept-Encoding`和`Keep-Alive`头的值：

```java
@GetMapping("/demo")
public void handle(@RequestHeader("Accept-Encoding") String encoding 1, @RequestHeader("Keep-Alive") long keepAlive 2) { 
    //...
}
```

<mark>1 </mark>获取`Accept-Encoding`头的值

<mark>2 </mark>获取`Keep-Alive`头的值

如果目标方法参数类型不是`String`，会自动应用类型转换。

当`@RequestHeader`注解使用在`Map<String,String>`，`MultiValueMap<String,String>`或`HttpHeaders`参数上时，map会填充所有的header值。

> 内置支持可用于将逗号分隔的字符串转换为数组或字符串集合或类型转换系统已知的其他类型。例如，用`@RequestHeader("Accept")`注解的方法参数可以是`String`类型，也可以是`String[]`或`List <String>`。

**`@CookieValue`**

可以送该注解来将HTTP cookie的值绑定到控制器方法的参数上。

考虑下面带有cookie的请求：

```
JSESSIONID=415A4AC178C59DACE0B2C9CA727CDD84
```

下面的例子展示了如何获取cookie值：

```java
@GetMapping("/demo")
public void handle(@CookieValue("JSESSIONID") String cookie) { 1
    //...
}
```

<mark>1 </mark>获取`JESSIONID`的值。

如果目标方法参数类型不是`String`，会自动应用类型转换。

**`@ModelAttribute`**

可以在方法参数上使用该注解，用于访问模型中的属性或如果不存在时实例化一个。模型属性也覆盖了名称与字段名称匹配的HTTP Servlet请求参数中的值。这成为数据绑定，免于解析和转换单个查询参数和表单字段的麻烦：

```java
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute Pet pet) { } 1
```

<mark>1 </mark>绑定一个`Pet`实例。

上面的`Pet`实例解析如下：

* 如果已通过使用模型添加，就就从模型中解析

* 通过使用`@SessionAttributes`，从HTTP session中解析

* 通过一个URI路径变量传递到一个`Converter`(参见下一个示例)。

* 从默认构造函数的调用开始。

* 从调用具有与Servlet请求参数匹配的参数的“主要构造函数”开始。参数名称是通过JavaBeans `@ConstructorProperties`或字节码中运行时保留的参数名称确定的。

虽然它通常使用Model来填充模型的属性，但另一种替代方法是依赖`Converter<String,T>`与URI路径变量约定结合使用。在下面的例子中，模型属性名`account`，匹配URI路径变量`account`，然后通过将`String` account number传递给已注册的`Converter<String,Account>`来加载`Account`。

```java
@PutMapping("/accounts/{account}")
public String save(@ModelAttribute("account") Account account) {
    // ...
}
```

在获取模型属性后，会应用数据绑定。`WebDataBinder`类匹配Servlet请求参数名（查询参数和表单字段）到目标`Object`上的字段名。匹配的字段在需要的地方应用类型转换后进行填充。有关数据绑定（和验证）的更多信息，请参见Validation。更多有关自定义数据绑定，参考DataBinder。

数据绑定可能导致错误。默认情况下，会产生`BindException`。但是，在控制器方法中为了检查这样的错误，可以`@ModelAttribute`后立即增加`BindingResult`参数：

```java
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@ModelAttribute("pet") Pet pet, BindingResult result) {1 
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```

<mark>1 </mark>在`@ModelAttribute`后增加一个`BindingResult`。

在某些情况下，可能想要访问在没有数据绑定时访问模型属性。对于这样的情况，可以在控制器中注入`Model`并直接访问它，或者设置`@ModelAttribute(binding=false)`：

```java
@ModelAttribute
public AccountForm setUpForm() {
    return new AccountForm();
}

@ModelAttribute
public Account findAccount(@PathVariable String accountId) {
    return accountRepository.findOne(accountId);
}

@PostMapping("update")
public String update(@Valid AccountForm form, BindingResult result,
        @ModelAttribute(binding=false) Account account) { 1
    // ...
}
```

<mark>1 </mark>设置`@ModelAttribute(binding=false)`。

通过增加`javax.validation.Valid`注解或Spring的`@Validated`注解在数据绑定后自动应用验证：

```java
@PostMapping("/owners/{ownerId}/pets/{petId}/edit")
public String processSubmit(@Valid @ModelAttribute("pet") Pet pet, BindingResult result) { 1
    if (result.hasErrors()) {
        return "petForm";
    }
    // ...
}
```

<mark>1 </mark>验证`Ped`实例

注意，使用`@ModelAttribute`是可选的（例如，设置它的属性）。默认情况下，任何不是简单值类型（通过BeanUtils#isSimpleProperty决定）且未被其他任何参数解析器解析的参数将被视为使用`@ModelAttribute`进行注解。

`@SessionAttributes`

该注解用来存储在HTTP Servlet session和请求之间模型属性。它是类型级别的注解，用于声明指定控制器使用的session属性。这通常列出应透明地存储在会话中以供后续访问请求的模型属性名称或模型属性类型。

下面的例子使用`@SessionAttributes`注解：

```java
@Controller
@SessionAttributes("pet")1 
public class EditPetForm {
    // ...
}
```

<mark>1 </mark>使用`@SessionAttributes`注解

在第一次请求时，当名称为`pet`的模型属性被添加到模型中，它会自动提升并保存到HTTP Servlet session中。它会一直保留在那里，直到另一个控制器方法使用`@SessionStatus`方法参数来清除存储：

```java
@Controller
@SessionAttributes("pet")1 
public class EditPetForm {

    // ...

    @PostMapping("/pets/{id}")
    public String handle(Pet pet, BindingResult errors, SessionStatus status) {
        if (errors.hasErrors) {
            // ...
        }
            status.setComplete();2 
            // ...
        }
    }
}
```

<mark>1 </mark>在Servlet session中存储`Pet`值

<mark>2 </mark>清除在Servlet session中的`Pet`值

**`@SessionAttribute`**

如果需要访问预选存在的全局管理的session属性（也就是说，控制器之外的session属性，例如，通过过滤器访问），它可能存在也可能不存在，可以在方法参数上使用`@SessionAttribute`注解：

```java
@RequestMapping("/")
public String handle(@SessionAttribute User user) { 1
    // ...
}
```

<mark>1 </mark>使用`@SessionAttribute`注解。

对于需要添加或删除session属性的使用案例，考虑注入`org.springframework.web.context.request.WebRequest`或`javax.servlet.http.HttpSession`到控制器方法中。

要在控制器工作流中将模型属性临时存储在会话中，请考虑使用`@SessionAttributes`，如@SessionAttributes中所述。

**`@RequestAttribute`**

与`@SessionAttrubite`类似，可以使用`@RequestAttribute`注解来访问先前创建的预先存在的请求参数（例如，通过Servlet `Filter`或`HandlerInterceptor`）：

```java
@GetMapping("/")
public String handle(@RequestAttribute Client client) { 1
    // ...
}
```

<mark>1 </mark>使用`@RequestAttribute`注解。

**重定向属性**

默认情况下，所有模型属性均被视为在重定向URL中作为URI模板变量公开。在其余属性中，那些属于原始类型或原始类型的集合或数组的属性会自动附加为查询参数。

如果专门为重定向准备 模型实例，则将原始类型属性作为查询参数附加可能是期望的结果。但是，在注解的控制器中，模型可以包含为渲染目的添加的其他属性（例如，下拉字段值）。为了避免次诶属性出现在URL中的可能性，`@RequestMapping`方法可以声明`RedirectAttributes`类型的参数，并使用它指定可用于`RedirectView`的确切属性。如果方法重定向，会使用`RedirectAttributes`属性。另外，将会使用模型的内容。

`RequestMappingHandlerAdapter`提供一个标志，叫做`ignoreDefaultModelOnRedirect`，它可以用来指示如果控制器方法重定向，则绝对不应该使用默认`Model`的内容。相反，控制器方法应该声明一个`RedirectAttributes`类型的属性，或者如果没有声明，则不应该将任何属性传递给`RedirectView`。MVC命名空间和MVC Java配置都将这个标志设置为`false`，保持向后兼容性。但是，对于新的应用程序，建议将它设置为`true`。

注意，展开重定向URL时，已存在请求中的URI模板变量会自动变为可用，并且不需要明确通过`Model`或`RedirectAttributes`来添加它们：

```java
@PostMapping("/files/{path}")
public String upload(...) {
    // ...
    return "redirect:files/{path}";
}
```

另外一种将数据传递给重定向目标的方法是使用flash attrubites。与其他重定向属性不同，flash属性保存在HTTP session中（因此不会出现在URL中）。

**Flash Attributes**

Flash属性为一个请求提供了一种存储要在另一个请求中使用的属性的方式。这在重定向时是最常用的-例如，Post-Redirect-Get模式。Flash属性在重定向之前（通常在会话中）被临时保存，以便在重定向之后可用请求使用，并立即删除。

Spring MVC有两个主要的抽象来支持flash属性。`FashMap`用来持有flash属性，而`FlashMapManager`用于存储，检索和管理`FlashMap`实例。

Flash属性支持始终处于打开状态无需显示启用。但是，如果不使用，它永远不会导致HTTP session创建。在每个请求上，都有一个”输入的“`FlashMap`，其属性是来自之前请求（如果有）传递过来的，而”输出的“ `FlashMap`的属性是为后续请求保存的。可以通过`RequestContextUtils`中的静态方法在Spring MVC中的任何位置访问这两个`FlashMap`实例。

注解的控制器通常不需要直接与`FlashMap`工作。相反，`@RequestMapping`方法可以接收一个类型为`RedirectAttributes`的参数并且为重定向情况下使用它来添加flash属性。Flash属性通过`RedirectAttributes`添加并自动传播到”输出的“FlashMap。类似的，在重定向之后，来自”输入的“FlashMap的属性会自动添加到提供目标URL的控制器的模型中。

> **匹配请求到flash 属性**
> 
> 在许多其他web框架中，flash属性已经存在，并且已证明有时候会遇到并发问题。这是因为根据定义，flash属性将存储到下一个请求为止。但是，”下一个”请求可能不是预期的接受者，而是另一个异步请求（例如轮询或资源请求），在这种情况下，会过早删除flash属性。
> 
> 为了减少这些问题出现的可能性，`RedirectView`会自动使用目标重定向URL的路径和查询参数“标记”`FlashMap`实例。反过来，默认`FlashMapManager`在查找“输入”`FlashMap`时会将信息与传入请求匹配。
> 
> 这不可能完全消除并发问题的可能性，但是可以通过重定向URL中已经可用的信息大大减少并发问题。因此，建议主要将Flash属性用于重定向方案。

**Multipart**

在开启`MultipartResolver`后，带有`multipart/form-data`的POST请求的内容会作为常规请求参数被解析和访问。下面的例子访问了一个常规的表单字段和一个上传文件：

```java
@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }
        return "redirect:uploadFailure";
    }
}
```

将参数类型声明为`List<MultipartFile>`允许解析同一请求参数名的多个文件。

当`@RequestParam`注解声明为`Map<String,MultipartFile>`或`MultiValueMap<String,MultipartFIle>`时，在注解中不需要指定参数名，map会为每个指定的参数名填充multipart files。

> 使用Servlet 3.0 multipart解析时，也可以声明`javax.servlet.http.Part`来代替Spring的`MultipartFile`，它可以作为方法参数或集合值类型。

也可以将multipart内容绑定到命令对象上作为数据绑定的一部分。例如，前面例子中的表单字段和文件可以定义为表单对象：

```java
class MyForm {

    private String name;

    private MultipartFile file;

    // ...
}


@Controller
public class FileUploadController {

    @PostMapping("/form")
    public String handleFormUpload(MyForm form, BindingResult errors) {
        if (!form.getFile().isEmpty()) {
            byte[] bytes = form.getFile().getBytes();
            // store the bytes somewhere
            return "redirect:uploadSuccess";
        }
        return "redirect:uploadFailure";
    }
}
```

Multipart请求也可以在RESTful服务场景下，从非浏览器的客户端被提交，下面的例子展示了一个带有JSON的文件：

```
POST /someUrl
Content-Type: multipart/mixed

--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="meta-data"
Content-Type: application/json; charset=UTF-8
Content-Transfer-Encoding: 8bit

{
    "name": "value"
}
--edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
Content-Disposition: form-data; name="file-data"; filename="file.properties"
Content-Type: text/xml
Content-Transfer-Encoding: 8bit
... File Data ...
```

可以使用`@RequestParam`访问`meta-data`并作为`String`，但是，可能想要从JSON（类似于`@RequestBody`）中将它反序列化。在使用`HttpMessageConverter`将它转换后，使用`@RequestPart`注解来访问multipart。

```java
@PostMapping("/")
public String handle(@RequestPart("meta-data") MetaData metadata,
        @RequestPart("file-data") MultipartFile file) {
    // ...
}
```

可以与`javax.validation.Valid`组合来使用`@RequestPart`或使用Spring的`@Validated`注解，两者都会导致标准bean验证被应用。默认情况下，验证错误会导致`MethodArgumentNotValidException`，会编程400（BAD_REQUEST）的响应。或者，可以通过`Errors`或`BindingResult`参数，在控制器内本地处理验证错误：

```java
@PostMapping("/")
public String handle(@Valid @RequestPart("meta-data") MetaData metadata,
        BindingResult result) {
    // ...
}
```

**`@RequestBody`**

可以使用该注解，读取请求体并通过HttpMessageConverter将内容反序列化到一个`Object`中。下面的例子使用了`@RequestBody`参数：

```java
@PostMapping("/accounts")
public void handle(@RequestBody Account account) {
    // ...
}
```

可以使用MVC Config中的Message Converter选项来配置或定制化消息转换。

可以与`javax.validation.Valid`组合来使用`@RequestPart`或使用Spring的`@Validated`注解，两者都会导致标准bean验证被应用。默认情况下，验证错误会导致`MethodArgumentNotValidException`，会编程400（BAD_REQUEST）的响应。或者，可以通过`Errors`或`BindingResult`参数，在控制器内本地处理验证错误：

```java
@PostMapping("/accounts")
public void handle(@Valid @RequestBody Account account, BindingResult result) {
    // ...
}
```

**HttpEntity**

`HttpEntity`或多或多或送与使用`@RequestBody`相同，但它基于公开请求头和请求体的容器对象：

```java
@PostMapping("/accounts")
public void handle(HttpEntity<Account> entity) {
    // ...
}
```

**`@ResponseBody`**

可以在方法上使用`@ResponseBody`来返回通过HttpMessageConverter序列化的响应体：

```java
@GetMapping("/accounts/{id}")
@ResponseBody
public Account handle() {
    // ...
}
```

`@ResponseBody`也支持类级别的注解，在这种情况下，控制器中的所有方法都继承它。这是`@RestController`的效果，它不过是带有`@Controller`和`@ResponseBody`标记的元注解。

可以将它与反应类型一起使用。

可以使用MVC Config中的Message Converters来配置或自定义消息转换。

可以将`@ResponseBody`方法与JSON序列化视图结合使用。

**ResponseEntity**

`ResponseEntity`与`@ResponseBody`很像，但是它带有状态和头，例如：

```java
@GetMapping("/something")
public ResponseEntity<String> handle() {
    String body = ... ;
    String etag = ... ;
    return ResponseEntity.ok().eTag(etag).build(body);
}
```

Spring MVC支持使用单值反应类型来异步生成ResponseEntity，和/或为主体使用单值和多值反应类型。

**Jackson JSON**

Spring为Jackson JSON库提供支持。

**JSON 视图**

Spring MVC为Jackson的序列化视图提供内置支持，可以允许仅渲染对象中所有字段的子集。为了使用带有`@ResponseBody`或`ResponseEntity`的控制器方法，可以使用Jackson的`@JsonView`注解来激活序列化类：

```java
@RestController
public class UserController {

    @GetMapping("/user")
    @JsonView(User.WithoutPasswordView.class)
    public User getUser() {
        return new User("eric", "7!jd#h23");
    }
}

public class User {

    public interface WithoutPasswordView {};
    public interface WithPasswordView extends WithoutPasswordView {};

    private String username;
    private String password;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @JsonView(WithoutPasswordView.class)
    public String getUsername() {
        return this.username;
    }

    @JsonView(WithPasswordView.class)
    public String getPassword() {
        return this.password;
    }
}
```

> `@JsonView`允许使用一组视图类，但是每个控制器方法只能指定一个。如果需要激活多个视图，则可以使用复合接口。

如果想要使用编程式方法来代替声明式注解`@JsonView`，需要使用`MappingJacksonValue`将返回值包装并使用它来提供序列化视图：

```java
@RestController
public class UserController {

    @GetMapping("/user")
    public MappingJacksonValue getUser() {
        User user = new User("eric", "7!jd#h23");
        MappingJacksonValue value = new MappingJacksonValue(user);
        value.setSerializationView(User.WithoutPasswordView.class);
        return value;
    }
}
```

对于控制器，它依赖于视图解析，可以增加序列化视图类添加到模型中：

```java
@Controller
public class UserController extends AbstractController {

    @GetMapping("/user")
    public String getUser(Model model) {
        model.addAttribute("user", new User("eric", "7!jd#h23"));
        model.addAttribute(JsonView.class.getName(), User.WithoutPasswordView.class);
        return "userView";
    }
}
```

### 1.3.4. 模型

可以使用`@ModelAttribute`注解：

* 在`@RequestMapping`方法中的方法参数上，可以访问或创建一个`Object`，并将其通过`WebDataBinder`绑定到请求。

* 作为`@Controller`或`@ControllerAdvice`类中的方法级注释，可在任何`@RequestMapping`方法调用之前帮助初始化模型。

* 在`@RequestMapping`方法上标记其返回值的是模型属性

本节讨论`@ModelAttribute`方法-前面列表中的第二项。一个控制器可以有任意数量的`@ModelAttribute`方法。所有这样的方法在相同控制器中的`@RequestMapping`方法前被调用。一个`@ModelAttribute`方法通过`@ControllerAdvice`也可以跨控制器空想。参考控制器通知获取更多细节。

`@ModelAttribute`方法有灵活的方法签名。他们想`@RequestMapping`方法一样支持许多相同的参数，除了`@ModelAttribute`本身与请求主题相关的任何东西之外。

下面的例子展示 `@ModelAttribute`方法：

```java
@ModelAttribute
public void populateModel(@RequestParam String number, Model model) {
    model.addAttribute(accountRepository.findAccount(number));
    // add more ...
}
```

下面的例子仅增加了一个属性：

```java
@ModelAttribute
public Account addAccount(@RequestParam String number) {
    return accountRepository.findAccount(number);
}
```

> 当没有明确指定名称时，默认的名称是基于`Object`的类型。可以总是通过使用重载`addAttribute`方法或通过在`@ModelAttribute`（对于一个返回值)上的`name`属性来明确指定名称。

可以使用`@ModelAttribute`作为在`@RequestMapping`方法上的方法级别注解，在这种情况下，`@RequestMapping`方法的返回值将解释为模型属性。这通常不是必须的，因为这是HTML控制器的默认行为，除非返回值是一个`String`，否则它江北解释为视图名称。`@ModelAttribute`也可以自定义模型属性名称：

```java
@GetMapping("/accounts/{id}")
@ModelAttribute("myAccount")
public Account handle() {
    // ...
    return account;
}
```

### 1.3.5. `DataBinder`

`@Controller`或`@ControllerAdvice`类可以使用`@InitBinder`方法，来初始化`WebDataBinder`实例：

* 绑定请求参数到模型对象（也就是说，表单或查询参数）。

* 转换基于字符串的请求值（例如请求参数，路径变量，请求头，cookies等等）到控制器方法参数的目标类型。

* 当呈现HTML表单时，将模型对象的值格式化为`String`。

`@InitBinder`方法可以注册指定控制器`java.beans.PropertyEditor`或Spring `Converter`和`Formatter`组件。此外可以使用MVC config在全局共享的`FormattingConversionService`注册`Converter`和`Formatter`。

`@InitBinder`方法支持与`@RequestMapping`方法相同的多种参数，除了`@ModelAttribute`参数（命令对象）。通常，他们使用`WebDataBinder`参数（用于注册）和`void`返回值声明：

```java
@Controller
public class FormController {

    @InitBinder 1 
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

    // ...
}
```

<mark>1 </mark>定义了一个`@InitBinder`方法

另外，当通过共享的`FormattingConversionService`使用基于`Formatter`的设置时，可以重新使用相同的方法并注册特定于控制器的`Formatter`实现：

```java
@Controller
public class FormController {

    @InitBinder 1
    protected void initBinder(WebDataBinder binder) {
        binder.addCustomFormatter(new DateFormatter("yyyy-MM-dd"));
    }

    // ...
}
```

<mark>1 </mark>定义一个`@InitBinder`在自定义格式器上。

### 1.3.6. 异常

`@Controller`和`@ControllerAdvice`类可以具有`@ExceptionHandler`方法来处理来自控制器方法的异常：

```java
@Controller
public class SimpleController {

    // ...

    @ExceptionHandler
    public ResponseEntity<String> handle(IOException ex) {
        // ...
    }
}
```

该异常可能与正在传播到顶级异常（例如直接应发的`IOException`）或包装器异常内的嵌套原因（例如，包装在`IllegalStateException`中的`IOException`）匹配。从5.3开始。这可以在任意愿意级别上匹配，而以前只考虑了直接原因。

对于匹配的异常类型，如前面的实例所示，最好将目标异常声明为参数方法。当多个异常方法匹配时，与异常原因匹配相比，通常首选根异常匹配。更具体地说，`ExceptionDepthComparator`用于根据从引发的异常类型开始的深度对异常进行排序。

另外，注解声明可以缩小异常类型以使其匹配，如一下示例所示：

```java
@Controller
public class SimpleController {

    // ...

    @ExceptionHandler
    public ResponseEntity<String> handle(IOException ex) {
        // ...
    }
}
```

甚至可以使用带有非常通用的参数签名的特定异常类型的列表，如以下示例所示：

```java
@ExceptionHandler({FileSystemException.class, RemoteException.class})
public ResponseEntity<String> handle(Exception ex) {
    // ...
}
```

>  根异常和原因异常匹配之间的区别可能会令人惊讶。
> 
> 在前面显示的`IOException`变体中，通常使用实际的`FileSystemExcepiton`或`RemoteException`实例作为参数来调用该方法，因为这两个实例均从`IOException`扩展。但是，如果任何此类匹配异常都在本身是`IOException`的包装器异常中传播，则传入的异常实例就是该包装器异常。
> 
> 在`handle(Exception)`变体中这种行为甚至更简单。在包装方案中，它总是使用包装程序异常来调用此方法。在这种情况下，实际匹配的异常可以通过`ex.getCause()`找到。仅当将他们作为顶级异常抛出时，传入的异常才是实际的`FileSystemException`或`RemoteException`实例。

通常建议在参数签名中尽可能具体，以减少跟类型和原因异常类型之间不匹配的可能性。考虑将多重匹配方法分解为单独的`@ExceptionHandler`方法，每个方法均通过其签名匹配单个特定的异常类型。

在多个`@ControllerAdvice`排列中，建议在以相应顺序优先的`@ControllerAdvice`上声明主要的根异常映射。尽管根异常匹配是原因的首选，但这是在指定控制器扩`@ControllerAdvice`类的方法之间定义的。这意味着优先级较高的`@ControllerAdvice` bean上的原因匹配优先于优先级较低的`@ControllerAdvice` bean上的任何匹配（例如，根）。

最后但并非不重要的一点是，`@ExceptionHandler`方法实现可以选择通过以原始形式重新抛出异常来退出处理指定异常的实例。在仅对根级别匹配或无法静态确定的特定上下文的匹配的情况下，这很有用。重新抛出的异常会在其余的解决方案链中传播，就像指定的`@ExceptionHandler`方法最初不会匹配一样。

Spring MVC中对`@ExceptionHandler`方法的支持建立在`DispatcherServlet`级别，`HandlerExceptionrsolver`机制上。

**方法参数（略...）**

**返回类型（略...）**

**REST API 异常**

对于REST服务的常用需求是在响应中包含错误详细信息。Spring框架不会自动这样做，因为响应体的错误细节的表示是特定于应用程序的。但是，`@RestController`可以使用带有`ResponseEntity`返回值的`@ExceptionHandler`方法来设置状态和响应体。这样的方法也可以声明在`@ControllerAdvice`类中并将他们应用于全局。

应用程序实现带有错误细节响应体的全局异常处理，应该考虑扩展ResponseEntityExceptionHandler

，它可以提供处理那些Spring MVC引发的异常，并且提供钩子以自定义响应体。要使用此功能，请创建ResponseEntityExceptionHandler的子类，并使用@ControllerAdvice对其进行注释，重写必需的方法，并将其声明为Spring bean。

### 1.3.7. Controller Advice

通常，`@ExceptionHandler`,`@InitBinder`和`@ModelAttribute`方法与`@Controller`类一起应用（或该类的继承）。如果想要将这些方法应用于全局（跨控制器），可以将他们声明在带有`@ControllerAdvice`或`@RestControllerAdvice`的类中。

`@ControllerAdvice`通常带有`@Component`批注，意味着这些类可以通过组件扫描注册为Spring beans。`@RestControllerAdvice`是一个组合注解，它是`@ControllerAdvice`与`@ResponseBody`的组合，本质上意味着`@ExceptionHandler`方法通过消息转换（多种视图解析或模板绘制）来呈现响应体。

启动时，`@RequestMapping`和`@ExceptionHandler`方法的基础结构类将检测使用`@ControllerAdvice`注解的Spring beans，然后在运行时应用其方法。全局`@ExceptionHandler`方法（从`@ControllerAdvice`）在本地方法（来自`@Controller`）之后应用。相比之下，全局`@ModelAttribute`和`@InitBinder`方法在本地方法之前应用。

默认情况下，`@ControllerAdvice`方法应用于每个请求（也就是说，所有的控制器），但是可以通过使用注解属性来缩小控制器的范围：

```java
// Target all Controllers annotated with @RestController
@ControllerAdvice(annotations = RestController.class)
public class ExampleAdvice1 {}

// Target all Controllers within specific packages
@ControllerAdvice("org.example.controllers")
public class ExampleAdvice2 {}

// Target all Controllers assignable to specific classes
@ControllerAdvice(assignableTypes = {ControllerInterface.class, AbstractController.class})
public class ExampleAdvice3 {}
```

前面示例中的选择器在运行时进行评估，如果广泛使用，可能会对性能产生负面影响。

## 1.4. 函数式端点（略...）

Spring Web MVC包含WebMvc.fn，一个轻量级的函数式编程模型，这些函数用来路由和处理请求，并且将合约设计为不可变的。它是基于注解的编程模型的替代方案，但可以在同一个`DispatcherServlet`上运行。

## 1.5. URI 连接（略...）

## 1.6. 异步请求

Spring MVC扩展集成了Servlet 3.0的异步请求处理：

* 在控制器方法中`DeferredResult`和`Callable`的返回值，并为单个异步返回值提供基本支持。

* 控制器可以流式传输多个值，包括SSE和原始数据。

* 控制器可以使用反应式客户端并为响应处理返回反应式类型。

### 1.6.1. `DeferredResult`

一旦在Servlet容器中开启异步请求处理的功能，控制器可以用`DeferredResult`包装任何支持的控制器方法返回值：

```java
@GetMapping("/quotes")
@ResponseBody
public DeferredResult<String> quotes() {
    DeferredResult<String> deferredResult = new DeferredResult<String>();
    // Save the deferredResult somewhere..
    return deferredResult;
}

// From some other thread...
deferredResult.setResult(result);
```

控制器从另一个线程中产生异步返回值-例如，响应外部时间（JMS消息），一个调度任务或其他事件。

### 1.6.2. `Callbale`

控制器可以使用`java.util.concurrent.Callbale`将返回值包装：

```java
@PostMapping
public Callable<String> processUpload(final MultipartFile file) {

    return new Callable<String>() {
        public String call() throws Exception {
            // ...
            return "someView";
        }
    };
}
```

然后，可以通过配置的`TaskExecutor`运行指定任务来获取返回值。

### 1.6.3. 处理

这是Servlet异步请求处理的非常简洁的描述：

* `ServletRequest`可以调用`request.startAsync()`方法设置为异步模式。这样做的主要效果是`Servlet`（以及所有过滤器）可以退出，但是响应保持打开状态，以便以后完成处理。

* 调用`request.startAsync()`返回`AsyncContext`，可以将其用于进一步控制异步处理。例如，它提供`dispatch`方法，与Servlet API的转发非常相似，不同之处在于，它使应用程序可以恢复对Servlet容器线程的请求处理。

* `ServletRequest`提供访问当前`DispatcherType`,可以使用它来区分处理初始请求，异步调度、转发和其他调度类型。

`DeferredResult`处理工作如下：

* 控制器返回`DeferredResult`，并将其保存在一些内存队列或列表中，可以在其中访问。

* Spring MVC调用`request.startAsync()`。

* 同时，`DispatcherServlet`和所有已配置的过滤器退出请求处理线程 ，但响应保持打开状态。

* 应用程序从某个线程设置`DeferredResult`，Spring MVC将请求分派会Servlet容器。

* `DespatcherServlet`再次被调用，并使用异步产生的返回值恢复处理。

**异常处理**

当使用`DeferredResult`时，可以选择是否回调`setResult`或带有异常的`setErrorResult`。在这两种情况下，Spring MVC都将请求分派回Servlet容器以完成处理。然后将其视为控制器方法返回了指定值，或者好像它产生了指定的异常一样。然后，异常将通过常规的异常处理机制进行处理（例如，调用`@ExceptionHandler`方法）。

当使用`Callbale`时，会发生相似的处理逻辑，主要的区别在于结果或引发的异常是从`Callbal`返回的。

**拦截**

`HandlerInterceptor`实例可以是`AsyncHandlerInterceptor`类型，用来接收在异步处理的初始请求（而不是`postHandler`和`afterCompletion`）上的`afterConcurrentHandlingStated`回调。

它的实现也可以注册`CallableProcessingInterceptor`或`DeferredResultProcessingINterceptor`，用于与异步请求的生命周期进行更深入的集成（例如，处理超时时间）。

`DeferredResult`提供`onTimeout(Runnable)`和`onCompletion(Runnable)`回调。参考 [javadoc of DeferredResult](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/web/context/request/async/DeferredResult.html)来获取更多细节。可以用`Callable`代替`WebAsyncTask`，它公开了超时和完成回调的其他方法。

**与WebFlux比较**

Servlet API最初是为了通过Filter-Servlet链进行一次传递而构建的。在Servlet 3.0中，增加了异步请求处理，让应用程序退出Filter-Servlet链，但保留响应以进行进一步处理。Spring MVC异步支持围绕该机制构建。当控制器返回一个`DeferredResult`时，Filter-Servlet链退出，并且Servlet 容器线程被释放。随后，当`DeferredResult`被设置时，进行`ASYNC` 调度（到相同的UR），在此期间再次唤醒控制器，但是不是调用它，而是使用`DeferredResult`值（就像控制器返回它一样）来恢复处理。

相比，Spring WebFlux不是构建在Servlet API之上的，它也不需要诸如异步请求处理这样的功能，因为它在设计上是异步的。异步处理已内置在所有框架约定中，并在请求处理的所有阶段得到内在支持。

从编程模型的角度来说，Spring MVC和Spring WebFlux都支持异步，并在控制器方法中可以将Reactive Types作为返回值。Spring MVC甚至支持流，包括反应背压（**在数据流从上游生产者向下游消费者传输的过程中，上游生产速度大于下游消费速度，导致下游的 Buffer 溢出，这种现象就叫做 Backpressure 出现。**）。但是与WebFlux不同，WebFlux依赖于非阻塞I/O，并且每次写入都不需要额外的线程，因此对响应的单个写入仍然处于阻塞状态（并在单独的线程上执行）。

另外一个基本区别是，Spring MVC在控制器方法参数中不支持异步或响应类型（例如，`@RequestBody`，`@RequestPart`等），它也没有对异步和反应式类型作为模型属性的任何显示支持。Spring WebFlux支持所有这些功能。

### 1.6.4. HTTP 流

可以使用`DeferredResult`和`Callable`用于单个异步返回值。如果要产生多个异步值并将这些值写入响应中应该怎么办？下面会讨论如何实现。

**Objects**

可以使用`ResponseBodyEmitter`返回值来生产对象流，每个对象通过`HttpMessageConverter`序列化并且写入响应，如下面展示的例子：

```java
@GetMapping("/events")
public ResponseBodyEmitter handle() {
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```

可以在`ResponseEntity`中使用`ResponseBodyEmitter`作为消息体，可以自定义响应状态和响应头。

当`emitter`抛出`IOException`时（例如，如果远程客户端消失了），应用程序不负责清理连接，并且不应该调用`emitter.complete`或`emitter.completeWithError`。相反，servlet容器自动初始化一个`AsyncListener`错误通知，Spring MVC在该通知中调用`completeWithError`。这个调用依次向应用程序执行最后一次异步调度，在此期间Spring MVC调用配置的异常解析器并完成请求。

**SSE**

`SseEmitter`（`ResponseBodyEmitter`的一个子类）为Server-Sent Events提供支持，该事件来自服务器，根据W3C SSE规范进行格式化。要从控制器生成`SSE`流，需要返回`SseEmitter`，如以下示例所示：

```java
@GetMapping(path="/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter handle() {
    SseEmitter emitter = new SseEmitter();
    // Save the emitter somewhere..
    return emitter;
}

// In some other thread
emitter.send("Hello once");

// and again later on
emitter.send("Hello again");

// and done at some point
emitter.complete();
```

虽然SSE是流式传输到浏览器的主要选项，但请注意，IE不支持服务器发送事件。考虑使用Spring的WebSocket messaging，与针对广泛浏览器的SockJS备选传输结合使用。

**原始数据**

有时候，绕过消息转换，直接流式传输到响应`OutputStream`很有用（例如，文件下载）。可以使用`StreamingResponseBody`作为返回值：

```java
@GetMapping("/download")
public StreamingResponseBody handle() {
    return new StreamingResponseBody() {
        @Override
        public void writeTo(OutputStream outputStream) throws IOException {
            // write...
        }
    };
}
```

可以在`ResponseEntity`中将`StreamingResponseBody`作为消息体，来自定义响应的状态和响应头。

### 1.6.5. 反应式类型（略...）

### 1.6.6. 断开连接

当远程客户端消失时，Servlet API没有提供任何通知。因此，在流式传输到响应时，无论是否通过`SseEmitter`还是反应式类型，定期发送数据是很重要的，因为如果客户端断开连接，写入将失败。发送可以采用空的(仅带有注释的)SSE事件或任何其他数据的形式，另一方必须将其解释为心跳并忽略它。

或者，考虑使用具有内置心跳机制的Web消息传递解决方案（例如，基于WebSocket的STOMP或具有SockJS的WebSocket）。

### 1.6.7. 配置（略...）

异步请求处理功能必须在Servlet容器级别开启。MVC配置也暴露了一些异步请求的可选项。

## 1.7. 跨域资源共享

Spring MVC可以处理CORS（跨域资源共享）。

### 1.7.1. 介绍

处于安全考虑，浏览器禁止AJAX调用当前域之外的资源。例如，可以在一个标签中拥有银行账户，在另一个标签中拥有evil.com。来自evil.com的脚本不能使用用户凭据向用户银行API发出AJAX请求，例如从账户中提取资金。

跨域资源共享（CORS）是由大多数浏览器实现的W3C规范，可以让用户指定授权哪种类型的跨域请求，而不是使用基于IFRAME或JSONP的安全性较低且功能较弱的变通办法。

### 1.7.2. 处理

跨域规范区分预检请求，简单请求和实际请求。为了了解跨域如何共走，可与阅读[this article](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)及其他内容，来获取更多详细信息。

Spring MVC `HandlerMapping`的实现为跨域提供了内置的支持。在成功将请求映射到处理器之后，`HandlerMaping`的实现为指定请求，处理器检查跨域配置并采取进一步动作。预检请求直接被处理，但是简单和实际跨域请求被拦截，验证，并要求设置跨域响应头。

为了开启跨域请求（也就是说，`Origin`头存在并区别于主机的请求），需要一些明确声明的跨域配置。如果没有找到匹配的跨域配置，预检请求会被拒绝。没有将CORS头添加到简单和实际CORS请求的响应中，因此，浏览器拒绝了他们。

每个`HandlerMapping`可以通过基于URL模式的`CorsConfiguration`映射单独配置。在大多数情况下，应用程序使用MVC Java配置或XML命名空间来声明这样的映射，这将导致将单个全局映射传递给所有`HandlerMapping`实例。

可以在`HandlerMapping`级别的全局跨域配置与更细粒度的处理程序级别的CORS配置结合使用。例如，可以将`@CrossOrigin`注解用于类或方法级别（其他处理器可以实现`CorsConfigurationSource`）。

全局和局部配置相结合的规则通常是可加的-例如，所有全局和所有本地来源。对于那些只能接受单个值的属性，例如`allowCredentials`和`maxAge`,则局部配置覆盖全局配置。

> 要了解更多源代码或进行高级定制，请查看后面的代码:
> 
> * `CorsConfiguration`
> 
> * `CorsProcessor`，`DefaultCorsProcessor`
> 
> * `AbstractHandlerMapping`

### 1.7.3. `@CorssOrigin`

使用该注解，为控制器方法开启跨域请求：

```java
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

默认情况下，`@CrossOrigin`允许：

* 所有源

* 所有头

* 所有控制器方法映射的请求方法

`allowCredentials`默认不开启，因为它将建立一个信任级别，以公开敏感的用户特定信息（例如cookie和CSRF令牌），并仅在适当的地方使用。当开启后，`allowOrigins`设置一个或多个特定域（而不是特殊值”*“），或者将`allowOringins`属性用于匹配动态的一组源。

`maxAge`被设置为30分钟。

`@CrossOrigin`也支持类级别，并且所有方法都继承它：

```java
@CrossOrigin(origins = "https://domain2.com", maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

也可以在类级别和方法级别同时使用该注解：

```java
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/account")
public class AccountController {

    @CrossOrigin("https://domain2.com")
    @GetMapping("/{id}")
    public Account retrieve(@PathVariable Long id) {
        // ...
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        // ...
    }
}
```

### 1.7.4. 全局配置

除了细粒度的控制器方法级别配置，可能想要定义一些全局跨域配置。可以设置独立的基于URL的`CorsConfiguration`映射在任意`HandlerMapping`上。但是，大多数应用程序都使用MVC Java配置或MVC XML名称空间来做到这一点。

默认情况下，全局配置启用如下：

* 所有源

* 所有请求头

* `GET`，`HEAD`，`POST`方法

`allowCredentials`默认不开启，因为它将建立一个信任级别，以公开敏感的用户特定信息（例如cookie和CSRF令牌），并仅在适当的地方使用。当开启后，`allowOrigins`设置一个或多个特定域（而不是特殊值”*“），或者将`allowOringins`属性用于匹配动态的一组源。

`maxAge`被设置为30分钟。

**Java 配置**

为了在MVC Java配置中开启跨域，可以使用`CorsRegistry`回调：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
            .allowedOrigins("https://domain2.com")
            .allowedMethods("PUT", "DELETE")
            .allowedHeaders("header1", "header2", "header3")
            .exposedHeaders("header1", "header2")
            .allowCredentials(true).maxAge(3600);

        // Add more mappings...
    }
}
```

**XML配置**

为了在XML命名空间中开启跨域，可以使用`<mvc:cors>`元素：

```java
<mvc:cors>

    <mvc:mapping path="/api/**"
        allowed-origins="https://domain1.com, https://domain2.com"
        allowed-methods="GET, PUT"
        allowed-headers="header1, header2, header3"
        exposed-headers="header1, header2" allow-credentials="true"
        max-age="123" />

    <mvc:mapping path="/resources/**"
        allowed-origins="https://domain1.com" />

</mvc:cors>
```

### 1.7.5. 跨域过滤器

通过内置的`CorsFilter`，可以应用跨域支持。

> 如果尝试与Spring Security一起使用`CorsFilter`，记住Spring Security为跨域提供内置的支持。

为了配置过滤器，需要为它的构造器传递一个`CorsConfigurationSource`：

```java
CorsConfiguration config = new CorsConfiguration();

// Possibly...
// config.applyPermitDefaultValues()

config.setAllowCredentials(true);
config.addAllowedOrigin("https://domain1.com");
config.addAllowedHeader("*");
config.addAllowedMethod("*");

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", config);

CorsFilter filter = new CorsFilter(source);
```

## 1.8. Web安全

Spring Security项目为保护web应用程序提供支持，以保护恶意攻击。Spring Security参考文档包括：

* [Spring MVC Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#mvc)

* [Spring MVC Test Support](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#test-mockmvc)

* [CSRF protection](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#csrf)

* [Security Response Headers](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#headers)

[HDIV](https://hdiv.org/)是另一个web安全框架，可以与Spring MVC集成。

## 1.9. HTTP缓存

HTTP缓存能够为web应用程序显著提升性能。它围绕`Cache-Control`响应头以及随后的条件请求头（例如`Last-Modified`和`ETag`）。`Cache-Control`为私有（例如浏览器）和公共（例如代理）缓存提供有关如何缓存和重用响应的建议。`ETag`头用于发出条件请求，如果内容未更改，则可能导致没有消息体的304（NOT_MODIFIED）。`ETag`可以看做是`Last-Modified`头的更复杂的后继者。

本节描述了Spring Web MVC中与HTTP缓存相关的选项。

### 1.9.1. `CacheControl`

`CacheControl`支持配置与`Cache-Control`头相关的设置，并在许多地方作为参数被接受：

* [WebContentInterceptor](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/web/servlet/mvc/WebContentInterceptor.html)

* [WebContentGenerator](https://docs.spring.io/spring-framework/docs/5.3.3/javadoc-api/org/springframework/web/servlet/support/WebContentGenerator.html)

* [Controllers](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-caching-etag-lastmodified)

* [Static Resources](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-caching-static-resources)

虽然RFC 7234描述了`Cache-Control`响应头的所有可能指令，但`CacheControl`类型采用了面向用例的方法，着重于常见方案：

```java
// Cache for an hour - "Cache-Control: max-age=3600"
CacheControl ccCacheOneHour = CacheControl.maxAge(1, TimeUnit.HOURS);

// Prevent caching - "Cache-Control: no-store"
CacheControl ccNoStore = CacheControl.noStore();

// Cache for ten days in public and private caches,
// public caches should not transform the response
// "Cache-Control: max-age=864000, public, no-transform"
CacheControl ccCustom = CacheControl.maxAge(10, TimeUnit.DAYS).noTransform().cachePublic();
```

`WebContentGenerator`也接受一个更简单的`cachePeriod`属性：

* `-1`不会生成`Cache-Control`响应头

* `0`可以防止使用`Cache-Control`：无存储指令

* n > 0值通过使用'Cache-Control: max-age=n'指令将给定的响应缓存n秒。

### 1.9.2. 控制器

控制器可以增加对HTTP缓存的显示支持。建议这么做，因为需要先计算资源的`lastModified`或`ETag`值，然后才能将其与条件请求头进行比较。控制器可以增加`ETag`头和`Cache-Control`设置到`ResponseEntiry`：

```java
@GetMapping("/book/{id}")
public ResponseEntity<Book> showBook(@PathVariable Long id) {

    Book book = findBook(id);
    String version = book.getVersion();

    return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
            .eTag(version) // lastModified is also available
            .body(book);
}
```

如果与条件请求标头的比较表明内容未更改，则前面的示例发送带有空正文的304（NOT_MODIFIED）响应。否则，`ETag`和`Cache-Control`标头将添加到响应中。

还可以根据控制器中的条件请求头进行检查：

```java
@RequestMapping
public String myHandleMethod(WebRequest request, Model model) {

    long eTag = ...  1

    if (request.checkNotModified(eTag)) {
        return null;  2
    }

    model.addAttribute(...);  3
    return "myViewName";
}
```

<mark>1 </mark>特定于应用程序的计算

<mark>2 </mark>响应被设置为304（NOT_MODIFIED）-没有更进一步处理

<mark>3 </mark>继续处理请求

可以使用三种变体来检查针对`eTag`值，`lastModified`值或者两者的条件请求。对于条件GET和HEAD请求，您可以将响应设置为304（NOT_MODIFIED）。对于条件POST，PUT和DELETE，您可以将响应设置为412（PRECONDITION_FAILED），以防止并发修改。

### 1.9.3 静态资源

应该为静态资源提供Cache-Control和条件响应标头，以实现最佳性能。请参阅有关配置静态资源的部分。

### 1.9.4. `ETag`过滤器

可以使用`ShallowEtagHeaderFilter`来添加根据响应内容计算的”浅“`eTag`值，从而节省带宽，但不节省CPU时间。

## 1.10 视图技术

 在Spring MVC中使用的视图技术是可插拔的。无论决定使用哪种视图技术，主要的问题是配置改变。本章将涵盖通过Spring MVC集成视图技术。

> Spring MVC应用程序的视图位于该应用程序的内部信任范围内。视图可以访问应用程序上下文中的所有bean。因此，不建议在外部源可编辑模板的应用程序中使用Spring MVC的模板支持，因为这可能会带来安全隐患。

### 1.10.1. Thymeleaf（略...）

### 1.10.2. FreeMarker（略...）

### 1.10.3. Groovy Markup（略...）

### 1.10.4. Script Views（略...）

### 1.10.5. JSP和JSTL（略...）

### 1.10.6. Tiles（略）

### 1.10.7. RSS和Atom（略...）

### 1.10.8. PDF和Excel

Spring提供了返回HTML以外的输出的方法，包括PDF和Excel电子表格。本节介绍如何使用这些功能。

**文档视图简介**

HTML页面并非始终是用户查看模型输出的最佳方法，而Spring使从模型数据动态生成PDF文档或Excel电子表格变得简单。该文档是视图，并从服务器以正确的内容类型进行流传输，以（希望）使客户端PC能够运行其电子表格或PDF查看器应用程序作为响应。

为了使用Excel视图，您需要将Apache POI库添加到您的类路径中。为了生成PDF，您需要添加（最好是）OpenPDF库。

> 如果可能，您应该使用基础文档生成库的最新版本。特别是，我们强烈建议您使用OpenPDF（例如，OpenPDF 1.2.12）而不是过时的原始iText 2.1.7，因为OpenPDF会得到积极维护并修复了不可信任PDF内容的重要漏洞。

**PDF 视图**

可以扩展`org.springframework.web.servlet.view.document.AbstractPdfView` 并实现`buildPdfDocument()`方法：

```java
public class PdfWordList extends AbstractPdfView {

    protected void buildPdfDocument(Map<String, Object> model, Document doc, PdfWriter writer,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<String> words = (List<String>) model.get("wordList");
        for (String word : words) {
            doc.add(new Paragraph(word));
        }
    }
}
```



控制器可以从外部视图定义（按名称引用）返回此视图，也可以从处理程序方法返回为视图实例。



**Excel视图**

从Spring框架4.2开始，`org.springframework.web.servlet.view.documnet.AbstractXlsView`为Excel视图提供了基础类。它基于Apache POI，具有取代过时的特殊类（`AbstractXlsxView`和`AbstractXlsxStreamingView`）。



编程模型类似于`AbstractPdfView`，其中`buildExcelDocument()`作为中央模板方法，控制器能够从外部定义（按名称）或从处理程序方法作为`View`实例返回这种视图。



### 1.10.9. Jackson

Spring提供对Jackson JSON库的支持。



**基于Jackson的JSON MVC视图**

`MappingJackson2JsonView`使用Jackson库的`ObjectMapper`将JSON作为响应内容呈现。默认情况下，模型的整个内容映射的编码是JSON（拥有框架指定的异常类）。对于那些要滤映射内容的需求，可以通过使用`modelKeys`属性，指定一组特殊的模型属性来编码。也可以使用`extractValueFormSingleKeyModel`属性，将单键模型中的值提取并序列化，而不是作为模型属性的映射。



可以使用Jackson提供的注解来自定义JSON映射。当需要更进一步的控制时，可以通过`ObjectMapper`属性注入一个自定义的`ObjectMapper`，为特殊类型提供自定义的JSON序列化和反序列化。



**基于Jackson的XML视图**

`MappingJackson2XmlView`使用Jackson XML的扩展`XmlMapper`将响应内容作为XML呈现。如果模型包含多个实体，应该通过使用`modelKey`明确设置被序列化的对象。如果模型包含单个实体，它会被自动序列化。



可以通过使用JAXB或Jackson提供的注解，自定义XML映射。当需要更进一步的控制时，可以通过`ObjectMapper`属性注入一个自定义的`ObjectMapper`，为特殊类型提供自定义的XML序列化和反序列化。



### 1.10.10. XML编组

`MarshallingView`使用XML `Marshaller`（定义在`org.springframework.oxm`包中）将响应内容呈现为XML。可以使用`MarshallingView`实例的`modelKey`属性来明确设置需要编组的对象。或者，该视图遍历所有模型属性，并封送`Marshaller`支持的第一个类型。



### 1.10.11. XSTL视图（略...）



## 1.11. MVC配置

MVC Java配置和MVC XML命名空间为大多数应用程序提供了默认适当的配置并为自定义配置提供了配置API。



有关配置API中不可用的更多高级定制，请参阅Advanced Java Config和Advanced XML Config。



不需要了解由MVC Java配置和MVC名称空间创建的基础bean。如果要了解更多信息，请参阅特殊Bean类型和Web MVC Config。



### 1.11.1. 开启MVC配置

在Java配置中，可以使用`@EnableWebMvc`注解来开启MVC配置：

```java
@Configuration
@EnableWebMvc
public class WebConfig {
}
```



在XML配置中，可以使用`<mvc:annotation-driven>`元素来开启配置：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven/>

</beans>

```



前面的例子注册了一系列的Spring MVC基础设施bean，并且在类路径上适配可用的依赖（如，对于payload的JSON，XML等转换器）。



### 1.11.2. MVC配置API

在Java配置中，可以实现`WebMvcConfigurer`接口：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    // Implement configuration methods...
}
```

在XML中，可以检查`<mvc:annotation-driven/>`的属性和子元素。可以查看Spring MVC XML Schema或使用IDE的代码完成功能来发现可用的属性可子元素。



### 1.11.3. 类型转换

默认情况下，安装了多种数字和日期的格式化器，以及为自定义字段提供了`@NumberFormat`和`DateTimeFormat`。



为了在Java配置中注册自定义的格式化器和转换器，需要如下配置：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // ...
    }
}
```



使用如下XML配置可以得到相同的效果：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven conversion-service="conversionService"/>

    <bean id="conversionService"
            class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="converters">
            <set>
                <bean class="org.example.MyConverter"/>
            </set>
        </property>
        <property name="formatters">
            <set>
                <bean class="org.example.MyFormatter"/>
                <bean class="org.example.MyAnnotationFormatterFactory"/>
            </set>
        </property>
        <property name="formatterRegistrars">
            <set>
                <bean class="org.example.MyFormatterRegistrar"/>
            </set>
        </property>
    </bean>

</beans>
```



默认情况下，Spring MVC考虑了本地语言环境的请求解析和格式化日志值。这适用于使用“输入”表单字段将日期表示为字符串的表单。但是，对于“日期”和“时间”表单字段，浏览器使用HTML规范中定义的固定格式。在这种情况下，日期和时间格式可以按以下方式自定义：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }
}
```



### 1.11.4. 校验

默认情况下，如果classpath（例如，Hibernate校验器）上存在bean校验，`LocalValidatorFactoryBean`会注册为全局校验器，使用`@Valid`和`Validated`在控制器方法参数上提供校验。



在Java配置中，可以自定义全局`Validator`实例：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public Validator getValidator() {
        // ...
    }
}
```



使用XML可以完成相同的工作：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:mvc="http://www.springframework.org/schema/mvc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/mvc
        https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <mvc:annotation-driven validator="globalValidator"/>

</beans>
```



注意，也可以在本地注册一个`Validator`实现：

```java
@Controller
public class MyController {

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new FooValidator());
    }
}
```

> 如果需要在某个地方注入`LocalValidatorFactoryBean`，请创建一个bean并用`@Primary`进行标记，以避免与MVC配置中声明的那个冲突。



### 1.11.5. 拦截器

在Java配置中，可以注册拦截器并应用于请求：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
        registry.addInterceptor(new ThemeChangeInterceptor()).addPathPatterns("/**").excludePathPatterns("/admin/**");
        registry.addInterceptor(new SecurityInterceptor()).addPathPatterns("/secure/*");
    }
}
```



使用下面的XML配置可以达到相同效果：

```xml
<mvc:interceptors>
    <bean class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor"/>
    <mvc:interceptor>
        <mvc:mapping path="/**"/>
        <mvc:exclude-mapping path="/admin/**"/>
        <bean class="org.springframework.web.servlet.theme.ThemeChangeInterceptor"/>
    </mvc:interceptor>
    <mvc:interceptor>
        <mvc:mapping path="/secure/*"/>
        <bean class="org.example.SecurityInterceptor"/>
    </mvc:interceptor>
</mvc:interceptors>
```



### 1.11.6. 内容类型

可以通过配置，让Spring MVC决定来自请求的媒体类型（例如，`Accept`头，URL路径扩展，查询参数等等）。



默认情况下，首先检查URL路径扩展名-将`json`，`xml`，`rss`和`atom`注册为已知扩展名（取决于类路径依赖项）。其次检查`Accept`头。



考虑将这些默认值更改为“Accept”头，并且，如果必须使用基于URL的内容类型解析，请考虑对路径扩展使用查询参数策略。有关更多详细信息，请参见后缀匹配和后缀匹配以及RFD。



在Java配置中，可以自定义请求的内容类型解析，如以下示例所示：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("json", MediaType.APPLICATION_JSON);
        configurer.mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```



以下示例显示了如何在XML中实现相同的配置：

```xml
<mvc:annotation-driven content-negotiation-manager="contentNegotiationManager"/>

<bean id="contentNegotiationManager" class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
    <property name="mediaTypes">
        <value>
            json=application/json
            xml=application/xml
        </value>
    </property>
</bean>
```



### 1.11.7. 消息转换器

在Java配置中，通过覆盖`configureMessageConverters()`（替换通过Spring MVC创建的默认转换器）或`extendMessageConverters()`（自定义默认转换器或将附加的转换器加入到默认转换器中）来自定义`HttpMessageConverter`。



下面的例子通过使用`ObjectMapper`来增加XML和Jackson JSON转换器，用来代替默认转换器：

```java
@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
                .indentOutput(true)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .modulesToInstall(new ParameterNamesModule());
        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
        converters.add(new MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(true).build()));
    }
}
```



在前面的例子中,`Jackson2ObjectMapperBuilder`是用来创建一个通用配置`MappingJackson2HttpMessageConverter`和`MappingJackson2XmlHttpMessageConverter`启用了缩进,一个定制的日期格式,和jackson-module-parameter-names注册,这增加了支持访问参数名称(功能添加到Java 8)。



这个建造者自定义了Jackson的默认属性，如下：

* 禁用[`DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`](https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/DeserializationFeature.html#FAIL_ON_UNKNOWN_PROPERTIES)

* 禁用[`MapperFeature.DEFAULT_VIEW_INCLUSION`](https://fasterxml.github.io/jackson-databind/javadoc/2.6/com/fasterxml/jackson/databind/MapperFeature.html#DEFAULT_VIEW_INCLUSION)



如果在classpath中检测到以下模块，它将自动注册以下模块：

* [jackson-datatype-joda](https://github.com/FasterXML/jackson-datatype-joda)：支持Joda时间类型

* [jackson-datatype-jsr310](https://github.com/FasterXML/jackson-datatype-jsr310)：支持Java 8日志和时间API类型

* [jackson-datatype-jdk8](https://github.com/FasterXML/jackson-datatype-jdk8)：支持其他Java 8类型，例如`Optional`

* [`jackson-module-kotlin`](https://github.com/FasterXML/jackson-module-kotlin)： 支持Kotlin类和日志类



其他可用的Jackson模块是：

* [jackson-datatype-money](https://github.com/zalando/jackson-datatype-money)：支持`javax.money`类型（非官方模块）

* [jackson-datatype-hibernate](https://github.com/FasterXML/jackson-datatype-hibernate)：支持Hibernate特定的类型和属性（包括惰性加载切面）



下面的例子展示了如何使用XML来完成相同的配置：

```xml
<mvc:annotation-driven>
    <mvc:message-converters>
        <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
            <property name="objectMapper" ref="objectMapper"/>
        </bean>
        <bean class="org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter">
            <property name="objectMapper" ref="xmlMapper"/>
        </bean>
    </mvc:message-converters>
</mvc:annotation-driven>

<bean id="objectMapper" class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean"
      p:indentOutput="true"
      p:simpleDateFormat="yyyy-MM-dd"
      p:modulesToInstall="com.fasterxml.jackson.module.paramnames.ParameterNamesModule"/>

<bean id="xmlMapper" parent="objectMapper" p:createXmlMapper="true"/>
```



### 1.11.8. 视图控制器

这是定义`ParameterizableViewController`的快捷方式，它在被调用时立即转发给视图。当视图生成响应之前没有Java控制器逻辑要运行时，可以在静态情况下使用它。



下面的Java配置示例将对`/`的请求转发给一个名为`home`的视图:

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
    }
}
```



通过使用`<mvc:view-controller>`元素来完成相同的工作：

```xml
<mvc:view-controller path="/" view-name="home"/>
```

如果`@RequestMapping`方法映射到任何HTTP方法的URL，则视图控制器不能用于处理相同的URL。这是因为通过URL匹配带注释的控制器被认为是端点所有权的足够强的指示，因此可以向客户机发送405 (METHOD_NOT_ALLOWED)、415 (UNSUPPORTED_MEDIA_TYPE)或类似的响应，以帮助进行调试。因此，建议避免在带注释的控制器和视图控制器之间拆分URL处理。



### 1.11.9. 视图解析

MVC配置简化了视图解析器的注册。



以下Java配置示例通过使用JSP和Jackson作为JSON呈现的默认视图来配置内容协商视图解析：

```java
Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(new MappingJackson2JsonView());
        registry.jsp();
    }
}
```



使用XML配置：

```xml
<mvc:view-resolvers>
    <mvc:content-negotiation>
        <mvc:default-views>
            <bean class="org.springframework.web.servlet.view.json.MappingJackson2JsonView"/>
        </mvc:default-views>
    </mvc:content-negotiation>
    <mvc:jsp/>
</mvc:view-resolvers>
```



但是请注意，FreeMarker、tile、Groovy标记和脚本模板也需要底层视图技术的配置。



MVC命名空间提供了专用元素。以下示例适用于FreeMarker：

```xml
<mvc:view-resolvers>
    <mvc:content-negotiation>
        <mvc:default-views>
            <bean class="org.springframework.web.servlet.view.json.MappingJackson2JsonView"/>
        </mvc:default-views>
    </mvc:content-negotiation>
    <mvc:freemarker cache="false"/>
</mvc:view-resolvers>

<mvc:freemarker-configurer>
    <mvc:template-loader-path location="/freemarker"/>
```



在Java配置中，可以相应的bean：

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.enableContentNegotiation(new MappingJackson2JsonView());
        registry.freeMarker().cache(false);
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer() {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("/freemarker");
        return configurer;
    }
}
```



### 1.11.10. 静态资源（略...）

### 1.11.11. 默认Servlet（略...）

### 1.11.12. Path匹配（略...）

### 1.11.13. 高级Java配置（略...）

### 1.11.14. 高级XML配置（略...）



## 1.12. HTTP/2（略...）
