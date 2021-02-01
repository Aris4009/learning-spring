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
