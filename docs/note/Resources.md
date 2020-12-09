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



### 2.3.2. `ClassPathResource`

这个类代表了应该从classpath下获取的资源。它使用线程上下文类加载器，给定的类加载器或给定的类来加载资源。



如果类路径资源驻留在文件系统中，但是没有作为classpath 资源驻留在一个jar中并且没有扩展到系统文件（通过servlet引擎或任何系统环境），则`Resource`实现以`java.io.File`形式支持解析。为了解决这个问题，各种`Resource`实现始终支持将解析当做一个`java.net.URL`。



一个`ClassPathResource`通过java代码明确使用`ClassPathResource`构造器来创建，但是当使用一个字符串参数作为路径来调用API方法时通常是隐式创建。对于后一种情况，`PropertyEditor`识别特殊前缀`classpath:`，并在这种情况下创建`ClassPathResource`。

### 2.3.3. `FileSystemResource`

这个`Resource`实现是用来处理`java.io.File`和`java.nio.file.Path`的。它支持解析一个`File`和一个`URL`。

### 2.3.4. `ServletContextResource`

这个`Resource`实现是针对`ServletContext`资源的，用来解析相关web应用程序根目录的相对路径。

它始终支持流访问和URL访问，但是只有当web应用程序归档是展开的并且资源以物理方式存放在文件系统时，才允许以`java.io.File`访问。它是在文件系统上展开或者是直接从JAR或其他类似数据库中访问，实际上取决于Servlet容器。

### 2.3.5. `InputStreamResource`

一个`InputStreamResource`是一个对于给定`InputStream`的一个`Resource`的实现。它应该只能被用在如果没有指定特定的资源。尤其是，尽可能使用`ByteArrayResource`或者任何基于文件的`Resource`实现。

和其他`Resource`对比，这是一个对于已经打开的资源的描述符。因此，调用`isOpen()`方法会返回`true`。如果需要在某些地方保持资源描述符或需要多次读取流，不应该使用它。

### 2.3.6. `ByteArrayResource`

这是一个对于给定的字节数组的`Resource`实现。对于给定的字节数组，会创建一个`ByteArrayInputStream`。

对于从任何给定的字节数组加载内容很有用，而不必采用一次性的`InputStreamResource`。
