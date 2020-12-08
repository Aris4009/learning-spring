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

* `getInputStream()`：找到并打开资源，读取资源后返回一个`InputStream`。它希望每次调用并返回一个新的`InputStream`。调用者有责任区关闭这个流。

* `exists()`：返回一个`boolean`表示资源是否以物理形式存在。

* `isOpen()`：返回一个`boolean`表示此资源是否表示带有打开流的句柄。如果为`true`，`Inputstream`不能多次读取并且只能读取一次然后关闭来避免资源泄露。对于所有常规资源的实现，返回`false`，但`InputStreamResource`除外。

* `getDescription()`：返回一个资源的描述，用来当一起与资源工作时的错误输出。这通常是标准文件名或者资源的实际URL。
  
  

其他方法可以获取一个实际的代表了资源的`URL`或`File`对象（如果底层的实现满足并支持那个功能）。



当需要资源时，Spring本身广泛使用Resource抽象作为许多方法签名中的参数类型。Spring APIs中的其他方法（例如多种`基于ApplicationContext`实现的构造函数）采用`String`形式，该字符串以未经修饰或简单的形式来创建适用于该上下文实现的`Resource`，或者通过`String`路径上的特殊前缀，让调用者必须创建和使用特定的`Resource`实现。



虽然`Resource`接口大量被Spring直接或间接使用，但对于用户自身的代码来说，它实际上可以被当做一个访问资源的工具类，甚至代码不需要了解或关注任何Spring的其他部分。虽然这会让代码和Spring产生耦合，但实际上仅将其耦合到这套使用程序类，他们充当URL的更强大代替，并且可以被认为等同于将用于这个目的的任何其他库。



*`Resource`抽象不能代替功能。它尽可能地包装它。例如，一个`UrlResource`包装 一个URL，并使用包装的URL来完成其工作。*



## 2.3. 内置Resource实现

Spring包含了如下`Resource`实现：

* `UrlResource`

* `ClassPathResource`

* `FileSystemResource`

* `ServletContextResource`

* `InputStreamResource`

* `ByteArrayResource`

### 2.3.1. `UrlResource`

`UrlResource` 包装了一个`java.net.URL`并且可以用于访问通常通过URL访问的任何对象，例如文件，HTTP目标，FTP目标等。所有的URLs都具有标准化的`String`表示形式，以便使用适当的标准化前缀来指示另一个URL类型。这包含了访问文件系统路径的`file:`，通过HTTP协议访问资源的`http:`通过FTP访问资源的`ftp:`等等。



`UrlResource`是由Java代码通过显示调用`UrlResource`构造函数创建的，但是通常在调用带有`String`参数表示路径的API方式时隐式创建。对于后一种情况，`PropertyEditor`最终决定使用哪种类型的`Resource`来创建。如果路径字符串包含一直的前缀（例如`classpath:`），对于这种前缀，它会创建适当的特定`Resource`。但是，如果它不认识这个前缀，它假设这个字符串是标准的URL字符串并创建一个`UrlResource`。
