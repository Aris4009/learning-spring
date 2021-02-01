# 2. REST客户端

本节介绍了客户端对REST端点的访问选项。

## 2.1. `RestTemplate`

`RestTemplate`是一个同步的客户端，用来执行HTTP请求。它是原始的Spring REST客户端并且基于HTTP客户端库，提供了一个简单的，模板式的方法API。

> 从5.0版本开始，`RestTemplate`处于维护模式，以后只有很少的更改和错误请求被接受。请考虑使用提供更现代API并支持同步，异步和流传输方案的`WebClient`。



## 2.2. `WebClient`

它是非阻塞，响应的执行HTTP请求的客户端。它在5.0后被引入，并且提供了更现代的API用来代替`RestTemplate`，它支持同步和异步两种有效的方式，并支持流场景。



对比`RestTemplate`，`WebClient`支持如下特性：

* 非阻塞I/O

* 反应式流背压

* 用更少的硬件资源来支持高并发

* 利用Java 8 lambdas的函数式、流畅的API

* 同步和异步交互

* 从服务器流向上或向下流



# 3. 测试

被节总结了Spring MVC应用程序在`spring-test`中的可用选项：

* Servlet API Mocks：Servlet API合约的模拟实现，用于单元测试控制器，过滤器和其他Web组件。

* TestContext Framework：支持在JUnit和TestNG测试中加载Spring配置文件，包括跨测试方法高效的缓存已加载的配置，并支持通过`MockServletContext`加载`WebApplicationContext`。

* Spring MVC Test：一个框架，也被称为`MockMvc`，通过`DispatcherServlet`（也就是说，支持注解）用于测试注解的控制器，使用Spring MVC基础设施，但是不适用HTTP服务器。

* Client-side REST：`spring-test`提供一个`MockRestServiceServer`，但是可以将它作为mock server来使用，用于client-side代码，它内部使用了`RestTemplate`。

* `WebTestClient`：用于构建测试WebFlux应用程序，但是它也被用在任意服务器的通过HTTP连接的端到端的集成测试。它是非阻塞的，响应式客户端，并且也适合用于异步和流式场景的测试。


