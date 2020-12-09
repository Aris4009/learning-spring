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

## 2.4. `ResourceLoader`

`ResourceLoader`接口旨在可以返回`Resource`实例的对象实现。下面列出了`ResourceLoader`接口的定义：

```
public interface ResourceLoader {

    Resource getResource(String location);
}
```

所有应用程序上下文实现这个接口。因此，所有应用程序上下文可以用来获取`Resource`实例。

当在一个特定的应用程序上下文上调用`getResource()`时，并且指定的位置路径没有特定的前缀时，将获得适合该特定应用程序上下文的`Resource`类型。例如，假设下面的代码片段依赖一个`ClassPathXmlApplicationContext`实例：

```
Resource template = ctx.getResource("some/resource/path/myTemplate.txt");
```

依靠`ClassPathXmlApplicationContext`，代码返回的是一个`ClassPathResource`。如果同样的方法依赖的是一个`FileSystemXmlApplicationContext`实例，它将返回一个`FileSystemResource`。对于一个`WebApplicationContext`，它返回一个`ServletContextResource`。对于每个上下文会类似的返回适合的对象。



结果，可以以适合特定应用程序上下文的方式加载资源。



另一方面，也可以强制使用`ClassPathResource`，无论应用程序上下问的类型是什么，通过指定特殊的`classpath:`前缀：

```
Resource template = ctx.getResource("classpath:some/resource/path/myTemplate.txt");

```



类似地，可以对于任何标准前缀的`java.net.URL`，可以强制使用`UrlResource`。下面是一组使用`file`和`http`前缀的例子：

```
Resource template = ctx.getResource("file:///some/resource/path/myTemplate.txt");

```

```
Resource template = ctx.getResource("https://myhost.com/resource/path/myTemplate.txt");

```



下面的表格总结了`Resource`对象转换`String`对象的策略：

| Prefix     | Example                        | Explanation               |
| ---------- | ------------------------------ | ------------------------- |
| classpath: | classpath:com/myapp/config/xml | 从classpath中加载             |
| file:      | file:///data/config.xml        | 从文件系统加载。                  |
| http://    | https://myserver/logopng       | 从URL中加载                   |
| (none)     | /data/config.xml               | 依赖底层的`ApplicationContext` |



## 2.5. `ResourceLoaderAware`接口

`ResourceLoaderAware`接口是一个特殊的回调接口，它用来识别那些希望提供一个`ResourceLoader`引用的组件。下面的例子展示了`ResourceLoaderAware`接口的定义：

```
public interface ResourceLoaderAware {

    void setResourceLoader(ResourceLoader resourceLoader);
}
```

当一个类实现了`ResourceLoaderAware`并且部署到一个应用程序上下文中（作为一个Spring管理的bean），通过应用程序上下文，它被识别为`ResourceLoaderAware`。应用程序上下文然后调用`setResourceLoader(ResourceLoader)`，将它自己作为参数提供给这个方法（记住，所有在Spring中的的应用程序上下文都实现了`ResourceLoader`接口）。

因为一个`ApplicationContext`是一个`ResourceLoader`，这个bean可能也实现了`ApplicationContextAware`接口并且提供给应用程序上下文来直接加载资源。但是，通常来说，使用特定的`ResourceLoader`接口更好。代码将会解藕，只需要资源加载接口（可以 被认为是工具接口）并且不需要整个Spring的`ApplicationContext`接口。

在应用程序组件中，可能会依赖自动装配`ResourceLoader`作为一个可选的`ResourceLoaderAware`接口的实现。传统的`constructor`和`byType`自动装配模式各自对于构造参数或一个setter方法参数有能力提供一个`ResourceLoader`。为了更富有弹性（包含自动装配字段的能力和多参数方法），考虑使用基于注解的自动装配特性。在这种情况下，只要有问题的字段，构造函数或方法带有`@Autowired`注解，`ResourceLoader`就会自动装配到需要`ResourceLoader`类型的字段，构造函数参数或方法参数中。



## 2.6. 资源依赖

如果bean自身将要通过某种动态过程来决定和提供资源路径，使用`ResourceLoader`接口来加载资源很有意义。例如，考虑加载某种模板，指定的资源需要依赖用户的角色。如果资源是静态的，则有必要完全消除对`ResourceLoader`接口的使用，让Bean公开所需的`Resource`属性，并期望将其注入。



注入这些属性的会变得微不足道，应用程序上下文都注册和使用了一个特殊的`PropertyEditor`，可以将`String`路径转换为`Resource`对象。因此，如果`myBean`有一个`Resource`类型的模板属性，它可以通过简单的字符串来进行配置，例如：

```
在这种情况下，只要有问题的字段，构造函数或方法带有@Autowired批注，ResourceLoader就会自动连接到需要ResourceLoader类型的字段，构造函数参数或方法参数中
```

注意资源路径没有前缀。因此，由于应用程序上下文自身作为一个`ResourceLoader`，资源本身通过`ClassPathResource`，`FileSystemResource`或`ServletContextResource`来加载，依赖上下文的确切类型。



如果需要强制使用指定的`Resource`类型，可以使用一个前缀。下面的两个例子展示了如何强制一个`ClassPathResource`和一个`UrlResource`(后者被用来访问一个文件系统文件)：

```
<property name="template" value="classpath:some/resource/path/myTemplate.txt">
```



```

```
