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

前面的例子在Spring MVC应用程序中使用，应该包含`DispatcherServlet`配置。但是，Spring的WebSocket和SockJS支持不依赖于Spring MVC。在`WebSocketHttpRequestHander`的帮助下，将`SockJsHttpRequestHandler`集成到其他HTTP服务环境中相对简单。



在浏览器端，应用程序可以使用`sockjs-client`（1.0x版本）。它模拟了W3C WebSocket API并且通过选择最佳传输项来连接服务器，具体取决于运行它的浏览器。参考[sockjs-client](https://github.com/sockjs/sockjs-client/)和浏览器支持的传输类型列表。客户端也可以提供一些配置选项，例如，指定包含哪种传输类型。



### 4.3.3. IE 8和IE 9

IE 8和IE 9仍然在使用。他们是使用SockJS的关键原因。本接涵盖了有关运行在这些浏览器的重要考虑。



SockJS在IE 8和9中通过使用微软的`XDomainRequest`来支持Ajax/XHR流。他们可以跨域工作但是不支持cookie。Cookie通常对于Java应用程序来说是必不可少的。但是，因为SockJS可以与许多服务类型使用（不仅仅是Java），它需要了解是否支持cookie的问题。如果支持，SockJS客户端使用Ajax/XHR。否则，它依赖于基于iframe的技术。



首先，来自SockJS客户单的`/info`请求是对信息的请求，它可能影响客户端选择传输类型。这些详细信息之一是服务器应用程序是否依赖Cookie（例如，出于身份验证目的或使用粘性会话进行群集）。Spring对SockJS的支持包括一个名为`sessionCookieNeeded`的属性。由于大多数Java应用程序都依赖`JSESSIONID` cookie，因此默认情况下启用该功能。如果应用程序不需要它，可以将此选项关闭，并且SockJS客户单应该在IE 8和9中选择`xdr-streaming`。



如果不使用基于iframe的传输方式，记住浏览器可以通过设置HTTP响应头，将`X-Frame-Options`设置为`DENY`，`SAMEORIGIN`，或`ALLOW-FROM <origin>`，来指示浏览器阻止在指定的页面上使用iframe。这用于防止点击劫持[clickjacking](https://www.owasp.org/index.php/Clickjacking)。



> Spring Security 3.2以后提供在每个响应上设置`X-Frame-Options`。默认情况下，Spring Security Java配置将它设置为`DENY`。在3.2中，Spring Security XML命名空间不能默认设置响应头，但是可以通过配置来设置。在将来的版本中，它可能会被默认设置。
> 
> 
> 
> 
> 参考Spring Security文档中的[Default Security Headers](https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#headers)部分来获取有关如何配置`X-Frame-Oprions`头的更多细节。也可以参考[SEC-2501](https://jira.spring.io/browse/SEC-2501)来获取附加的背景知识。



如果应用程序添加了`X-Frame-Options`响应头（它也应该被添加）并依赖基于iframe的传输，需要将响应头的值设置为`SAMEORIGIN`或`ALLOW-FROM <origin>`。Spring SockJS支持还需要知道SockJS客户端的位置，因为它从iframe中加载。默认情况下，iframe被设置为从CDN的位置下载SockJS。最好将此选项配置为使用与应用程序源相同的URL。



下面的例子展示了这样的Java配置：

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/portfolio").withSockJS()
                .setClientLibraryUrl("http://localhost:8080/myapp/js/sockjs-client.js");
    }

    // ...

}
```



XM命名空间通过`<websocket:sockjs>`元素来提供相似选项。

> 在初始开发时，开启SockJS客户端的`devel`模式，用来阻止浏览器中缓存的SockJS请求（像iframe），否则他们会被缓存。更多有关如何开启它请参考 [SockJS client](https://github.com/sockjs/sockjs-client/)。



### 4.3.4. 心跳

SockJS协议要求服务器发送心跳信息，以防止代理断定连接已经挂起。Spring SockJS配置有一个名为`heartbeatTIme`的属性，可以使用它来自定义心跳频率。默认情况下，假设连接上没有发送其他消息，心跳会在25秒后被发送。这个25秒的值符合以下IETF对公共Internet应用程序的建议。



> 但在WebSocket和SockJS上使用STOMP时，如果STOMP客户端和服务端协商要交换心跳，则会禁用SockJS心跳。



Spring SockJS也可以配置`TaskScheduler`来调度心跳任务。任务调度器由线程池支持，其默认设置基于可用处理器的数量。应该考虑根据特定需求自定义设置。



### 4.3.5. 客户端断开连接

 HTTP流和HTTP长轮询SockJS传输要求连接保持打开的时间比通常长。有关这些技术的概述，请参阅 [this blog post](https://spring.io/blog/2012/05/08/spring-mvc-3-2-preview-techniques-for-real-time-updates/)。



在Servlet容器中，这是通过Servlet 3异步支持完成的，该支持允许退出Servlet容器线程，处理请求并继续写入另一个线程的响应。



一个特定的问题是，Servlet API不会为已离开的客户端提供通知。参考[eclipse-ee4j/servlet-api#44](https://github.com/eclipse-ee4j/servlet-api/issues/44)。但是，Servlet容器在随后尝试写入响应时引发异常。由于Spring的SockJS服务支持服务器发送的心跳信号（默认情况下，每25秒发送一次），这意味着通常会在该时间段内（或更早，如果消息发送频率更高）检测到客户端断开连接。



> 结果，会发生网络I/O失败，因为客户端已经断开连接，这可能会在日志中填充不必要的堆栈跟踪。Spring尽最大努力识别这种表示客户机断开连接(特定于每个服务器)的网络故障，并使用专用日志类别DISCONNECTED_CLIENT_LOG_CATEGORY(在AbstractSockJsSession中定义)记录最小的消息。如果需要查看堆栈跟踪，可以将该日志类别设置为TRACE。



### 4.3.6. SockJS和跨域

如果允许跨域请求，SockJS协议将跨域用于XHR流和轮询传输中的跨域支持。因此，跨域头会自动添加，除非检测到响应中存在CORS头。因此，如果应用程序如果已经配置 提供跨域的支持（例如，通过Servlet Filter），Spring的`SockJsService`会跳过这部分。



也可以通过在Spring的SockJsService中设置`suppressCors`来禁用附加的跨域头。



SockJS期望下列头和值：

* `Access-Control-Allow-Origin`：从`Origin`请求头中初始化值。

* `Access-Control-Allow-Credentials`：总是设置为`true`。

* `Access-Control-Request-Headers`：从等效请求头中的值初始化。

* `Access-Control-Allow-Methods`：传输支持的HTTP方法（请参见`TransportType`枚举）。

* `Access-Control-Max-Age`：设置为31536000（1年）。



对于额外的信息，参考在`AbstractSockJsService`源代码中的`addCorsHeaders`和`TransportType`枚举。



另外，如果允许跨域配置，考虑通过SockJS端点前缀来排除URLs，然后让Spring的`SockJsService`处理它。



### 4.3.7. `SockJsCLient`

Spring提供一个SockJS客户端用来连接远程的SockJS端点而无需使用浏览器。当需要在公共网络上的两个服务器之间进行双向通信时(也就是说，在网络代理可以排除使用WebSocket协议的情况下)，这一点特别有用。它对于测试也非常有用（例如，模拟大量并发用户）。



该客户端支持`websocket`，`xhr-streaming`和`xhr-polling`传输。其余的仅在浏览器中有意义。



可配置的`WebSocketTransport`：

* 在JSR-356运行时的`StandardWebSocketClient`

* 通过使用Jetty 9以上的本地WebSocket API的`JettyWebSocketClient`

* 任意Spring的`WebSocketClient`实现



`XhrTransport`定义同时支持`xhr-streaming`和`xhr-polling`，因此， 从客户机的角度来看，除了用于连接到服务器的URL之外，没有其他区别。目前有两种实现:

* `RestTemplateXhrTransport`使用Spring的`RestTemplate`用于HTTP请求

* `JettyXhrTransport`使用Jetty的`HttpClient`用于HTTP请求



下面的例子展示了如何创建SockJS客户端并且连接到SockJS端点：

```java
List<Transport> transports = new ArrayList<>(2);
transports.add(new WebSocketTransport(new StandardWebSocketClient()));
transports.add(new RestTemplateXhrTransport());

SockJsClient sockJsClient = new SockJsClient(transports);
sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://example.com:8080/sockjs");
```

> SockJS使用JSON格式化消息数组。默认情况下，使用Jackson 2并且需要它在classpath下。或者，可以配置一个自定义的`SockJsMessageCodec`实现并将它配置在`SockJsClient`上。



为了使用`SockJsClient`模拟大量的并发用户，需要在底层HTTP客户端（对于XHR传输）上配置允许足够的连接数和线程数。下面的例子展示了如何通过Jetty使用：

```java
HttpClient jettyHttpClient = new HttpClient();
jettyHttpClient.setMaxConnectionsPerDestination(1000);
jettyHttpClient.setExecutor(new QueuedThreadPool(1000));
```



下面的例子展示了服务端SockJS相关的属性，用户也应该考虑自定义：

```java
@Configuration
public class WebSocketConfig extends WebSocketMessageBrokerConfigurationSupport {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/sockjs").withSockJS()
            .setStreamBytesLimit(512 * 1024) 1
            .setHttpMessageCacheSize(1000) 2
            .setDisconnectDelay(30 * 1000); 3 
    }

    // ...
}
```

<mark>1 </mark> 设置`streamBytesLimit`属性为512KB（默认为128KB-128*1024*）

<mark>2 </mark> 设置`httpMessageCacheSize`属性为1000（默认为`100`）

<mark>3 </mark> 设置`disconnectDelay`属性为30秒（默认为5秒-5*1000）



## 4.4. STOMP

WebSocket定义了两类消息（文本和二进制），但是，他们的内容未定义。协议为客户端和服务端定义了一个机制，用来协商要在WebSocket上使用的子协议（即高级消息协议），以定义每种协议可以发送的消息类型，消息格式，消息内容等等。子协议的使用是可选的，但是无论哪种方式，客户端和服务器都需要就定义消息内容的某种协议达成共识。



### 4.4.1. 概述

STOMP（简单文本面向消息协议）最初是为脚本语言（例如Ruby，Python和Perl）创建的，用来连接企业级的消息代理。它旨在解决常用消息传递模式的最小子集。STOMP可以在任何可靠的双向流网络协议上使用，例如TCP和WebSocket。尽管STOMP是面向文本的协议，但是消息有效负载可以是文本或二进制。



STOMP是基于帧的协议，其帧以HTTP为模型。以下清单显示了STOMP帧的结构：

```
COMMAND
header1:value1
header2:value2

Body^@
```

客户单可以使用`SEND`或`SUBSCRIBE`命令来发送或订阅消息，以及`destination`头，它用来描述应该由谁接受。这启用了一种简单的发布-订阅机制，可以使用该机制通过代理将消息发送到其他连接的客户端，或者将消息发送到服务器，以请求执行某些工作。



当使用Spring的STOMP支持时，Spring WebSocket应用程序充当客户端的STOMP代理。消息被路由到@Controller消息处理方法或简单的内存中代理，该代理跟踪订阅并向订阅的用户广播消息。还可以将Spring配置为与专用STOMP代理（例如RabbitMQ，ActiveMQ等）一起使用，以实际广播消息。在那种情况下，Spring维护与代理的TCP连接，将消息中继给它，并将消息从它传递到连接的WebSocket客户端。因此，Spring Web应用程序可以依靠基于HTTP的统一安全性，通用验证以及用于消息处理的熟悉的编程模型。



以下示例显示了一个订阅以接收股票报价的客户端，服务器可能会定期发出该股票报价（例如，通过计划任务，该任务通过`SimpMessagingTemplate`将消息发送给代理）:

```
SUBSCRIBE
id:sub-1
destination:/topic/price.stock.*

^@
```



以下示例显示了一个发送交易请求的客户端，服务器可以通过@MessageMapping方法处理该请求：

```
SEND
destination:/queue/trade
content-type:application/json
content-length:44

{"action":"BUY","ticker":"MMM","shares",44}^@
```



执行后，服务器可以向客户广播交易确认消息和详细信息。



在STOMP规范中，目的地的含义是故意不透明的。它可以是任何字符串，并且完全由STOMP服务器定义它们支持的目的地的语义和语法。但是，目的地通常是类似路径的字符串，其中/ topic / ..表示发布-订阅（一对多），而/ queue /表示点对点（一对一）消息交流。



STOMP服务器可以使用`MESSAGE`命令向所有订户广播消息。以下示例显示了服务器向订阅的客户端发送股票报价的服务器：

```
MESSAGE
message-id:nxahklf6-1
subscription:sub-1
destination:/topic/price.stock.MMM

{"ticker":"MMM","price":129.45}^@
```



服务器无法发送未经请求的消息。来自服务器的所有消息都必须响应特定的客户端订阅，并且服务器消息的subscription-id头必须与客户端订阅的id头匹配。



前面的概述旨在提供对STOMP协议的最基本的了解。建议全面阅读协议规范。



### 4.4.2. 优点

使用STOMP作为子协议，让Spring框架和Spring Security提供了一个比原始WebSockets更丰富的编程模型。关于HTTP与原始TCP以及它如何使Spring MVC和其他Web框架提供丰富功能的观点相同。下面列出了它的优点：

* 无需发明自定义消息协议和消息格式。

* 可以使用STOMP客户端，包括Spring框架中的Java客户端。

* 可以（可选）使用消息代理（例如RabbitMQ，ActiveMQ和其他代理）来管理订阅和广播消息。

* 可以在任意数量的@Controller实例中组织应用程序逻辑，并且可以基于STOMP目标头将消息路由到它们，而不是通过给定连接使用单个WebSocketHandler处理原始WebSocket消息。

* 可以使用Spring Security基于STOMP目的地和消息类型来保护消息。



### 4.4.3. 开启STOMP

`spring-messaging`和`spring-websocket`模块提供了对WebSocket的支持。一旦有这些依赖，可以公开一个STOMP端点，在WebSocket上使用SockJS Fallback：

```java
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/portfolio").withSockJS(); 1 
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app"); 2
        config.enableSimpleBroker("/topic", "/queue"); 3
    }
}
```

<mark>1 </mark>`protfolio`是HTTP URL，用于客户端需要WebSocket握手的端点。

<mark>2 </mark>其目标标头以/ app开头的STOMP消息被路由到 @Controller类中的@MessageMapping方法。

<mark>3 </mark> 使用内置的消息代理进行订阅和广播，以及 将目标标头以`/ topic`或`/ queue`开头的消息路由到代理。



下面的XML配置可以达到相同的效果：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:websocket="http://www.springframework.org/schema/websocket"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/websocket
        https://www.springframework.org/schema/websocket/spring-websocket.xsd">

    <websocket:message-broker application-destination-prefix="/app">
        <websocket:stomp-endpoint path="/portfolio">
            <websocket:sockjs/>
        </websocket:stomp-endpoint>
        <websocket:simple-broker prefix="/topic, /queue"/>
    </websocket:message-broker>

</beans>
```



> 对于内置的简单代理，/topic和/queue前缀没有任何特殊含义。它们仅是区分发布订阅消息传递和点对点消息传递的约定（即，许多订户与一个消费者）。使用外部代理时，请检查代理的STOMP页面以了解其支持哪种STOMP目标和前缀。



要从浏览器进行连接，使用`sockjs-client`。对于STOMP，多数应用程序使用`jmesnil/stomp-websocket`库（也就是stomp.js），它功能齐全，已在生产中使用了多年，但已不再维护。目前，`JSteunou/webstomp-client`是该库中最活跃且发展最快的后继程序。以下示例代码基于此：

```js
var socket = new SockJS("/spring-websocket-portfolio/portfolio");
var stompClient = webstomp.over(socket);

stompClient.connect({}, function(frame) {
}
```

或者，可以通过WebSocket连接（不适用SockJS）：

```js
var socket = new WebSocket("/spring-websocket-portfolio/portfolio");
var stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
}
```

注意，在前面例子中的`stompClient`不需要`login`和`passcode`头。 即使这样，它们也会在服务器端被忽略(更确切地说，是被覆盖)。

### 4.4.4. WebSocket Server

为配置底层的WebSocket服务器，使用`Server Configuration`中的信息。对于Jetty，需要通过`StompEndpointRegistry`设置`HandshakeHandler`和`WebSocketPolicy`：

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/portfolio").setHandshakeHandler(handshakeHandler());
    }

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {

        WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize(8192);
        policy.setIdleTimeout(600000);

        return new DefaultHandshakeHandler(
                new JettyRequestUpgradeStrategy(new WebSocketServerFactory(policy)));
    }
}
```



### 4.4.5. 消息流（略）

其余部分略...
