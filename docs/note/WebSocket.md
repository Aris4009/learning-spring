# 4. WebSockets

这部分参考文档涵盖了支持的Servlet栈，WebSocket消息包括原始WebSocket集成，通过SockJS进行WebSocket模拟，以及通过STOMP的发布订阅消息传递作为WebSocket上的子协议。



## 4.1. 介绍

WebSocket协议，RFC 6455，提供一个标准化的方式，可以通过单个TCP连接在客户端和服务端之间建立全双工双向通信通道。它是与HTTP不同的TCP协议，但旨在通过端口80和443在HTTP上工作，并允许重复使用现有的防火墙规则。



WebSocket交互始于一个HTTP请求，该请求使用HTTP `Upgrade`头进行升级或者在这种情况下切换到WebSocket协议：

```http
GET /spring-websocket-portfolio/portfolio HTTP/1.1
Host: localhost:8080
Upgrade: websocket 1
Connection: Upgrade 2
Sec-WebSocket-Key: Uc9l9TMkWGbHFD2qnFHltg==
Sec-WebSocket-Protocol: v10.stomp, v11.stomp
Sec-WebSocket-Version: 13
Origin: http://localhost:8080
```

<mark>1 </mark>`Upgrade`头

<mark>2 </mark>使用`Upgrade`连接



具有WebSocket支持的服务器代替通常的200状态代码，返回类似于以下内容的输出：

```http
HTTP/1.1 101 Switching Protocols 1
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: 1qVdfYHU9hPOl4JYYNXF623Gzn0=
Sec-WebSocket-Protocol: v10.stomp
```

<mark>1 </mark>协议切换



在成功握手后，HTTP升级请求的底层TCP socket将保持打开状态，客户端和服务器均可继续发送和接受消息。



WebSocket的完整工作流程超越了本文档的范围。请参考RFC 6455,HTML5的WebSocket章节或有关Web的介绍和教程。



注意，如果WebSocket服务器运行在一个web服务器后（例如，nginx），可能需要配置它来传递WebSocket升级请求到WebSocket服务器。同样，如果应用程序在云环境中运行，请检查与WebSocket支持相关的云提供商的说明。



### 4.1.1. HTTP对比WebSocket

尽管WebSocket被设计为与HTTP兼容并以HTTP请求开头，但重要的是了解这两个协议导致 截然不同的体系结构和应用程序编程模型。



在HTTP和REST中，应用程序被建模为许多URL。为了与应用程序交互，客户端通过请求-应答的方式访问这些URL。服务器根据HTTP URL，方法和头将请求路由到适当的处理程序。



相比，在WebSocket中，初始连接通常只有一个URL。随后，所有应用程序消息都在同一个TCP连接上流动。这指向了一个完全不同的异步，事件驱动的消息传递体系结构。



WebSocket与HTTP不同，它是一个低层传输协议，没有为消息的内容规定任何语义。这意味着除非客户端和服务器就消息语义达成一致，否则就无法路由或处理消息。



WebSocket客户端和服务器可以通过HTTP握手请求上的`Sec-WebSocket-Protocol`头写上使用更高级别的消息传递协议（例如STOMP）。在这种情况下，他们需要提出自己的约定。



### 4.1.2. 何时使用WebSockets

WebSockets可以使web页面具有动态性和可交互性。但是，在多数情况下，Ajax和HTTP流或长轮询的结合可以提供一种简单有效的解决方案。



例如，新闻，邮件和社交订阅源需要动态更新，但是每隔几分钟这样做可能是完全可以的。另一方面，协作、游戏和金融应用程序需要更接近实时性。



仅延迟不是决定因素。如果消息量相对较低(例如，监视网络故障)，HTTP流或轮询可以提供有效的解决方案。低延迟、高频率和高容量的结合是WebSocket使用的最佳条件。



记住，在Internet上，不受控制的限制性代理可能会阻止WebSocket交互，要么是因为它们没有被配置为传递升级报头，要么是因为它们关闭了看起来空闲的长时间连接。这意味着与面向公众的应用程序相比，将WebSocket用于防火墙内部的应用程序是一个更直接的决定。



## 4.2. WebSocket API

Spring框架提供了WebSocket API，以便使用他们可以编写客户段和服务端的应用程序来处理WebSocket消息。



### 4.2.1. `WebSocketHandler`

创建一个WebSocket服务器就像实现`WebSocketHandler`一样简单，或者扩展`TextWebSocketHandler`或`BinaryWebSocketHandler`。下面的例子使用了`TextWebSocketHandler`：

```java
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

public class MyHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // ...
    }

}
```

有专用的WebSocket Java配置和XML名称空间支持，用于将前面的WebSocket处理程序映射到特定的URL，如以下示例所示：

```java
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/myHandler");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyHandler();
    }

}
```



下面使用XML配置可以与前面的例子达到相同效果：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <websocket:handlers>
        <websocket:mapping path="/myHandler" handler="myHandler"/>
    </websocket:handlers>

    <bean id="myHandler" class="org.springframework.samples.MyHandler"/>

</beans>
```

前面的示例是用于Spring MVC应用程序的，应该包含在DispatcherServlet的配置中。但是，Spring的WebSocket支持不依赖于Spring MVC。在WebSocketHttpRequestHandler的帮助下将`WebSocketHandler`集成到其他HTTP服务环境中相对简单。



直接或间接使用`WebSocketHandler` API时，例如通过STOMP消息传递，由于基础标准WebSocket会话（JSR-356）不允许并发发送，因此应用程序必须同步消息的发送。一种选择是用`ConcurrentWebSocketSessionDecorator`包装WebSocketSession。



### 4.2.2. WebSocket握手

自定义初始化HTTP WebSocket握手请求的最简单方式是通过`HandShakeInterceptor`，它为握手公开了"before"和"after"方法。可以使用这样的拦截器来阻止握手或使任何属性对`WebSocketSession`可用。下面的例子使用内置的拦截器来传递HTTP session属性到WebSocket session：

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyHandler(), "/myHandler")
            .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

}
```



接下来的例子使用XML配置达到与前面例子相同的效果：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <websocket:handlers>
        <websocket:mapping path="/myHandler" handler="myHandler"/>
        <websocket:handshake-interceptors>
            <bean class="org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor"/>
        </websocket:handshake-interceptors>
    </websocket:handlers>

    <bean id="myHandler" class="org.springframework.samples.MyHandler"/>

</beans>
```



更多高级选项需要扩展`DefaultHandshakeHandler`，它执行WebSocket握手的步骤，包括验证客户端源，协商子协议以及其他细节。应用程序如果需要配置自定义的`RequestUpgradeStrategy`，来适配尚未支持的WebSocket服务器引擎和版本，则可能需要使用此选项。Java配置和XML名称空间都使配置自定义`HandshakeHandler`成为可能。

> Spring提供一个`WebSocketHandlerDecorator`基类，让用户使用它来装饰带有附加行为的`WebSocketHandler`。当使用WebSocket Java配置或XML命名空间时，默认情况下会提供和添加日志和异常处理实现。`ExceptionWebSocketHandlerDecorator`捕获由任何`WebsocketHandler`方法引起的所有未捕获的异常，并关闭状态为1011（指示服务器错误）的WebSocket会话。



### 4.2.3. 部署

Spring WebSocket API可以简单地集成到Spring MVC应用程序中，`DispatcherServlet`可以为HTTP WebSocket握手和其他HTTP请求提供服务。它也可以很容易地通过调用`WebSocketHttpRequestHandler`集成在其他HTTP处理场景中。这非常便捷并容易理解。但是，对于JSR-356运行时，有一些特殊的考虑。



Java WebSocket API（JSR-356）提供两种部署机制。首先在启动时，调用Servlet容器classpath扫描（Servlet 3功能）。然后注册API使用Servlet容器初始化。这两种机制都无法对使用单个“前端控制器”处理所有HTTP请求（包括WebSocket握手和所有其他HTTP请求，例如Spring MVC的`DispatcherServlet`）。



这是JSR-356非常重要的限制，Spring的WebSocket支持使用特定于服务器的`RequestUpgradeStrategy`实现来解决这个问题，即使在JSR-356运行时也不例外。Tomcat，Jetty，GlassFish，WebLogic，WebSphere和Undertow（以及WildFly）目前存在此类策略。

> 已经创建了克服Java WebSocket API中的上述限制的请求，可以在eclipse-ee4j / websocket-api＃211中进行跟踪。Tomcat，Undertow和WebSphere提供了自己的API替代方案，使之可以做到这一点，而Jetty也可以实现。我们希望更多的服务器可以做到这一点。



另一个要考虑的因素是，期望支持JSR-356的Servlet容器执行`ServletContainerInitializer`（SCI）扫描，这可能会减慢应用程序的启动速度。如果在升级到支持JSR-356的Servlet容器版本后观察到重大影响，那么应该可以通过使用web中的`<absolute-order/>`元素有选择地启用或禁用web片段(和SCI扫描)。xml，如下例所示:

```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://java.sun.com/xml/ns/javaee
        https://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
    version="3.0">

    <absolute-ordering/>

</web-app>
```



然后，可以按名称选择性地启用Web片段，例如，Spring的`SpringServletContainerInitializer`，它提供对Servlet 3 Java初始化API的支持：

```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://java.sun.com/xml/ns/javaee
        https://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
    version="3.0">

    <absolute-ordering>
        <name>spring_web</name>
    </absolute-ordering>

</web-app>
```



### 4.2.4. 服务器配置

每个底层WebSocket引擎公开了配置属性，以便控制运行时特性，例如消息缓冲的大小，空闲超时时间等。



对于Tomcat, WildFly, and GlassFish,可以增加`SrvletServerContainerFactoryBean`到WebSocket Java配置中：

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }

}
```



也可以使用XML配置来完成：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <bean class="org.springframework...ServletServerContainerFactoryBean">
        <property name="maxTextMessageBufferSize" value="8192"/>
        <property name="maxBinaryMessageBufferSize" value="8192"/>
    </bean>

</beans>
```

> 对于客户端WebSocket配置，应该使用`WebSocketContainerFacotyrBean`（XML）或`ContainerProvider.getWebSocketContainer()`（Java 配置）。



### 4.2.5. 允许的来源

Spring框架4.1.5以后，WebSocket和SockJS的默认行为只能接受同源请求。它也可以允许所有源或指定列表的源。这个检查主要是为浏览器客户端设计的。没有任何措施可以阻止其他类型的客户端修改Origin头值（有关更多详细信息，请参阅RFC 6454：Web Origin概念）。



三种可能的行为是：

* 仅允许同源请求（默认）：在这种模式下，当开启SockJS时，Iframe HTTP响应头`X-Frame-Options`设置为`SAMEORIGIN`，并且`JSONP`禁止传输，因为它不允许检查请求的来源。因此，启用该模式时，IE6和IE7不支持。

* 允许指定源列表：每个被允许的源必须以`http://`或`https://`开头。在这种模式下，当开启SockJS时，IFrame传输被禁用。因此，该模式下不支持IE6到IE9。

* 允许所有源：开启这个模式后，应该提供`*`作为允许的来源值。在这个模式下，所有传输都是可用的。



可以配置WebSocket和SockJS允许的源：

```java
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/myHandler").setAllowedOrigins("https://mydomain.com");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyHandler();
    }

}
```



下面的XML配置与前面的例子等效：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <websocket:handlers allowed-origins="https://mydomain.com">
        <websocket:mapping path="/myHandler" handler="myHandler" />
    </websocket:handlers>

    <bean id="myHandler" class="org.springframework.samples.MyHandler"/>

</beans>
```



## 4.3. 回退SockJS

在公共互联网上，控件外部的局限性代理可能会组织WebSocket交互，这可能是因为未将他们配置为传递`Upgrade`头，或者是因为他们关闭了长期处于空闲状态的连接。



解决此问题的方法是模拟WebSocket，即先尝试使用WebSocket，然后再尝试使用基于HTTP的技术来模拟WebSocket交互并公开相同的应用程序级API。



在Servlet堆栈上，Spring框架为SockJS协议提供服务器（和客户端）支持。



### 4.3.1. 概述

SockJS的目标是，让应用程序使用WebSocket API，在运行时回退到非WebSocket而无需改变应用程序代码。



SockJS包括：

* SockJS协议以可执行叙述测试的形式定义

* SockJS JavaScript客户端-在浏览器中使用的客户端库

* SockJS服务器实现，包括Spring框架`spring-websocket`模块中的一个。

* 在`spring-websocket`中的SockJS Java客户端（4.1版本以后）。



SockJS为浏览器使用设计。它使用多种技术来支持广泛的浏览器版本。对于所有列出的SocketJS传输类型和浏览器，请参考SockJS client。传输分为三类：WebSocket，HTTP流和HTTP长轮询。对于分类的概述，请参考[this blog post](https://spring.io/blog/2012/05/08/spring-mvc-3-2-preview-techniques-for-real-time-updates/)。



SockJS客户端开始通过发送`GET /info`，从服务器获取基本信息。然后，它必须决定使用那种传输。如果可能，就使用WebSocket。如果不可用，在多数浏览器中，至少有一个HTTP流选项。如果也不可用，就使用HTTP长轮询。



所有传输请求有如下URL结构：

```http
https://host:port/myApp/myEndpoint/{server-id}/{session-id}/{transport}
```

* `{server-id}`对于在集群中路由请求很有用，否则不使用

* `{session-id}`关联属于SockJS会话的HTTP请求

* `{transport}`指示传输类型（例如，`websocket`，`xhr-streaming`等）



WebSocket传输仅需要单个HTTP请求即可进行WebSocket握手。此后所有消息在该套接字上交换。



HTTP传输需要更多请求。例如，Ajax/XHR流依赖于服务器到客户端消息的一个长时间运行的请求，以及对客户端到服务器消息的其他HTTP POST请求。长轮询类似，不同支出在于长轮询在每次服务器到客户端发送后结束当前请求。



SockJS增加了少量的消息帧：例如，服务器发送字母`o`（“open” frame）初始化，发送的消息为`a["message1","message2"]`（JSON编码的数组），字母`h`（"heartbeat" frame），如果在25秒（默认）中没有任何消息，字母`c`（"close" frame）来关闭会话。



如果想要了解更多，需要将例子运行在浏览器中以便观察HTTP请求。SockJS客户端允许固定传输列表，因此可以一次查看每个传输。SockJS客户端也提供debug标志位，开启后可以帮助了解在浏览器控制台中的消息。在服务器端，可以开启`TRACE`日志。



### 4.3.2. 开启SockJS

可以通过Java配置开启SockJS：

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myHandler(), "/myHandler").withSockJS();
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyHandler();
    }

}
```



下面的例子使用XML可以达到相同配置：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <websocket:handlers>
        <websocket:mapping path="/myHandler" handler="myHandler"/>
        <websocket:sockjs/>
    </websocket:handlers>

    <bean id="myHandler" class="org.springframework.samples.MyHandler"/>

</beans>
```

前面的例子在Spring MVC应用程序中使用，应该包含`DispatcherServlet`配置。
