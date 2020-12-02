# 2. Resources

这个章节覆盖了Spring如何处理资源并且在Spring中如何用资源工作。包括了如下如下的话题：

* 介绍

* Resource接口

* 内置Resource实现

* `ResourceLoader`

* `ResourceLoaderAware`接口

* Resources作为依赖

* Application Context和Resource Paths



## 2.1. 介绍

不幸的是，Java的标准`java.net.URL`类和用于各种URL前缀的标准处理程序不足以满足所有对低级资源的访问。例如，没有标准化的URL实现用来从classpath或相对于ServletContext获得资源。虽然可以注册一个新的特殊`URL`前缀（类似于已经存在的处理器例如`http:`）处理器，这通常来说非常复杂，并且`URL`接口仍然缺少一些期望的功能，例如一个用来检查所指向资源是否存在的方法。



## 2.2. Resource接口

Spring的`Resource`接口意味着它是一个有更多能力的可以访问到底层resource的抽象接口。下面列出了`Resource`接口的定义：

```
public interface Resource extends InputStreamSource {

    boolean exists();

    boolean isOpen();

    URL getURL() throws IOException;

    File getFile() throws IOException;

    Resource createRelative(String relativePath) throws IOException;

    String getFilename();

    String getDescription();
}
```

就像`Resource`接口定义展示的那样，它扩展了`InputStreamSource`接口。下面列出了`InputStreamSource`接口的定义：

```
public interface InputStreamSource {

    InputStream getInputStream() throws IOException;
}
```

`Resource`接口的一些最重要的方法是：

* `getInputStream()`：定位和打开资源，从resource中读取并返回一个`InputStream`。它希望每次调用并返回一个新的`InputStream`。调用者有责任区关闭这个流。

* `exists()`：返回一个`boolean`表明资源是否以物理形式存在。

* `isOpen()`：返回一个`boolean`表明资源是否是一个打开的流。如果为`true`，`Inputstream`不能多次读取并且只能读取一次然后关闭来避免资源泄露。对于所有常规资源的实现，返回`false`，但`InputStreamResource`除外。

* `getDescription()`：返回一个资源的描述，用来当一起与资源工作时的错误输出。这常用来完全限定的文件名或实际的URL资源。

其他方法可以获取一个实际的代表了资源的`URL`或`File`对象（如果底层的实现满足并支持那个功能）。



Spring自身广泛地使用`Resource`抽象，当资源被需要时，在许多方法签名中作为一种参数类型。Spring APIs中的其他方法（例如多种`ApplicationContext`实现的变种的构造器）接受一个`String`，该``
