# 1.IoC容器
在spring框架中，最重要的就是IoC（即控制翻转）。
## 1.1 介绍Spring IoC容器和Beans
这个章节将介绍spring框架实现IoC的原理。IoC也被陈如果为DI（即依赖注入）。依赖注入就是类只通过构造参数、工厂方法参数或者属性设置与他们相关的依赖，然后通过构造器或者工厂方法返回对象实例。为什么说是这就是控制翻转（这个过程从根本上讲，是通过使用类的直接构造器或者注入服务定位器模式之类的控件，来控制其依赖的实例）。

`org.springframework.beans`和`org.springframework.context`包是最基本的spring IoC容器。`BeanFactory`接口提供了一种高级配置机制，能够管理任何类型的对象。`ApplicationContext`是`BeanFactory`的一个子接口，它新增了：
* 更方便的集成spring框架的AOP特性
* 消息资源处理（用于国际化）
* 事件发布
* 应用层特定的上下文，例如`WebApplicationContext`

简单来说，`BeanFactory`提供框架的配置和基础功能，`ApplicationContext`增加了更多特定的企业级功能。
### 1.2 容器概述
`org.springframework.context.Application` 接口表示表示spring中的IoC容器，它用来负责实例化、配置并组装beans。容器通过读取配置元数据来实例化、配置并组装beans。元数据的表示有多重形式，有XML、Java注解或者Java代码。

在spring的单体应用中，`ClassPathXmlApplication`或`FileSystemXmlApplication`通常用来被创建容器的实例。XML格式的文件是传统的元数据配置，也可以使用Java注解或者Java代码来提供元数据的声明。
#### 1.2.1 元数据配置
除了传统的XML元数据配置文件，spring容器还支持基于注解的配置和基于Java代码的配置。
spring配置至少包含一个或多个bean的定义，才能被容器管理。
**基于XML的元数据配置表示为:**

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="..." class="...">  
        <!-- collaborators and configuration for this bean go here -->
    </bean>

    <bean id="..." class="...">
        <!-- collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions go here -->

</beans>
```
`ClassPathXmlApplicationContext`的UML图
![](https://raw.githubusercontent.com/Aris4009/attachment/main/ClassPathXmlApplicationContext.png)

```
package com.example.demo.xml.ioc.container;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * xml方式初始化bean
 */
public class InitBeans {

    public static final Logger log = LoggerFactory.getLogger(InitBeans.class);

    public static void main(String[] args) {
        try {
            String path = "classpath*:beans.xml";
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
            Hello hello = context.getBean(Hello.class);
            log.info("init hello bean {}",hello);
            context.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
```


```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="hello" class="com.example.demo.hello.Hello">
        <constructor-arg name="name"  value="xmlName"/>
        <constructor-arg name="age" value="2"/>
    </bean>
</beans>
```

**基于Java代码的元数据配置表示为(`@Bean`方法注解需要结合`@Configuration`类注解一起使用)：**
```
package com.example.demo.ioc.container;

import com.example.demo.hello.Hello;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于java的元数据配置
 */
@Configuration
public class JavaBasedConfiguration {

    @Bean
    public Hello helloBean() {
        Hello hello = new Hello();
        hello.setName("hello");
        hello.setAge(12);
        return hello;
    }
}
```
### 1.2.2 初始化一个容器
ClassPathXmlApplicationContext通过配置文件载入配置元数据。
```
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- services -->

    <bean id="petStore" class="org.springframework.samples.jpetstore.services.PetStoreServiceImpl">
        <property name="accountDao" ref="accountDao"/>
        <property name="itemDao" ref="itemDao"/>
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions for services go here -->

</beans>
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="accountDao"
        class="org.springframework.samples.jpetstore.dao.jpa.JpaAccountDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <bean id="itemDao" class="org.springframework.samples.jpetstore.dao.jpa.JpaItemDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions for data access objects go here -->

</beans>
```
在前面的例子中，service层包含了`PetStoreServiceImpl`和`JapAccountDao`、`JpaItemDao`（这两个类是基于JPA的ORM标准实现的）。`property name`标签是指JavaBean的属性名，`ref`标签指这个属性的bean定义。`id`和`ref`元素之间的这种联系表达了协作对象的依赖性。

**基于XML的配置元数据构成**
把bean的定义，分割成多个XML文件非常有用。通常，每个独立的XML配置文件代表软件架构中的一个逻辑分层或者模块。
可以使用application context 构造器从这些XML片段中读取bean的定义。这个构造器可以获取多个`Resource`。另一方面，也可以使用一个或多个`import`标签元素来载入其他的XML配置文件。 
```
<beans>
    <import resource="services.xml"/>
    <import resource="resources/messageSource.xml"/>
    <import resource="/resources/themeSource.xml"/>

    <bean id="bean1" class="..."/>
    <bean id="bean2" class="..."/>
</beans>
```
在前面的例子中，外部的bean定义文件有三个：`services.xml`，`messageSource.xml`，`themeSource.xml`。所有的文件路径都是相对路径，`services.xml`必须和classpath location在同一个目录，然而`messageSource.xml`和`themeSource.xml`必须在`resources`目录下才能被导入。

*可以但不建议使用相对路径"../"来引用父目录的文件。这样做会创建当前应用程序外部文件的依赖,尤其不建议使用*`classpath:` 、`classpath:../servics.xml`。*classpath 的改变会导致选择到不正确的目录*
*可以使用合格的路径来代替相对路径，例如：*`file:C:/config/services.xml`或者`classpath:/config/service.xml`。*然而，这样做就会导致应用跟配置文件的绝对路径耦合。通常，最好为这样的绝对位置保留一个间接寻址，例如通过在运行时对JVM系统属性解析的${}占位符*

代码示例：
```
package com.example.demo.xml.ioc.container;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitMultipleImportBeans {
    public static final Logger log = LoggerFactory.getLogger(InitMultipleImportBeans.class);

    public static void main(String[] args) {
        try {
            String path = "classpath*:multiple.xml";
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
            Hello hello = context.getBean(Hello.class);
            log.info("init hello bean {}",hello);
            context.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    <import resource="beans.xml"/>
</beans>
```

### 1.2.3使用容器
`ApplicationContext`是一个高级工厂接口，该工厂能够维护不同Bean以及他们依赖项的注册表。通过调用方法`T getBean(String name,Class<T> requiredType)`，可以获取bean的实例对象。

## 1.3 Bean概述
一个spring IoC容器管理着一个或更多的beans。这些Bean是通过元数据配置文件，在容器中创建的。
在容器内部，这些Bean定义被表示为`BeanDefinition`对象，包含如下元数据：
* 包限定的类名：一个类的具体实现。
* Bean行为元素配置，用于声明Bean在容器中的行为（作用于、生命周期回调等）。
* 其他Bean的引用，这些Bean的引用也被称为协作者或者依赖项。
* 在新创建的对象中设置其他配置-例如，线程池的大小、链接数等

元数据被翻译成一系列的属性来组成每个bean定义。下面的列表描述了这些属性

**表格1 Bean定义**

| Property                 | Explained in...          |
|--------------------------|--------------------------|
| Class                    | Instantiating Beans      |
| Name                     | Naming Beans             |
| Scope                    | Bean Scopes              |
| Constructor arguments    | Dependency Injection     |
| Properties               | Dependency Injection     |
| Autowiring mode          | Autowiring Collaborators |
| Lazy initialization mode | Lazy-initialized Beans   |
| Initialization method    | Initialization Callbacks |
| Destruction method       | Destruction Callbacks    |

除此之外，bean definitions 包含怎样去创建一个特殊的bean信息，`ApplicationContext`的实现允许用户注册容器外的已存在的对象。通过访问实现了ApplicationContext's BeanFactory的`DefaultListableBeanFacotory`的`getBeanFacotory()`方法来完成。`DefaultListableBeanFacotory`支持通过`registerSingleton(..)`和`registerBeanDefinition(..)`注册bean。d按时，典型的应用程序只能通过常规的的元数据定义来定义bean。

*Bean元数据和手动提供的单例实例需要尽早注册，以便容器在自动装配和其他自省步骤中正确地装配它们*

### 1.3.1 Bean的命名
每一个Bean有一个或多个标识符。这些标识符在容器中必须是惟一的。一个bean通常只有一个标识符，然而，如果需要更多的标识符，考虑使用别名。

在基于XML配置的元数据中，可以使用`id`属性，`name`属性或者可以同时使用二者。可以精确地定义一个`id`属性。按照惯例，这些名称都使用字母和数字去定义，例如('myBean','someService'等等)，但是，也可以包含一些特殊字符。如果想给bean增加一些别名，就使用`name`属性，多个别名使用逗号(,)，分号(;)或者空格来分割。在Spring 3.1之前的版本中，`id`属性被定义为`xsd:ID`类型，限制了可能使用到的字符类型。在3.1之后，属性的类型被定义为`xsd:string`类型。注意，bean 的`id`唯一性仍然后容器强制执行，尽管不在由XML解析器执行。

`id`,`name`属性对于bean来说不需要显示指定。如果明确的不需要`name`或`id`，容器通常会生成一个唯一的名字给bean。然而，如果想通过名字来引用bean，通过使用`ref`元素或者Service Locator查找，就必须提供一个名字。不提供名字的动机通常是指`inner bean`或者`autowiring collaborators`。

*Bean 命名约定为驼峰命名*

**在Bean定义之外定义bean的别名**
在bean本身的定义中，可以通过制定`id`属性和任意数量的`name`属性来为bean提供多个名称。这些名称等同于bean的别名并且在某些情况下很有用。例如，通过使用特定的bean名称，让应用程序中的每个组件都引用一个公共的依赖项。

在实际定义bean的地方定义所有别名并不总是足够的，然而，有时候需要在别处定义bean的别名。这通常发生在配置文件被分割为多个子系统的大型系统中，每个子系统拥有一系列自己的对象定义。在基于XML的元数据配置中，可以使用`<alias>`元素来完成别名的设置。
`<alias name="formName",alias="toName">`
在这个例子中，一个bean在同一个容器中被命名为fromName，然后toName也是指这个bean。

例如，在包含子系统的元数据配置中，A系统的数据源名称为`subsystemA-dataSource`,B系统的数据源配置为`subsystemB-dataSource`。当组成这两个子系统时，住应用程序的数据源名为`myApp-dataSource`。为了将三个引用指向同一个数据源，需要在别名定义的时候增加如下的信息:
```
<alias name="myApp-dataSource" alias="subsystemA-dataSource"/>
<alias name="myApp-dataSource" alias="subsystemB-dataSource"/>
```

现在，主应用程序保证数据源命名的唯一性并且不会发生冲突，并且他们指向同一个bean。

如果使用Javaconfiguration，`@Bean`注解可以提供别名。

### 1.3.2 初始化Beans

Bean定义本质上是创建一个或者多个对象。容器会查看命名的Bean配置，并使用该Bean定义封装的配置元数据来创建（或获取）实际的对象。
如果使用基于XML的配置元数据，则要在`<bean/>`元素的`class`属性中指定要实例化的对象的类型或者类。`class`属性通常是需要强制指定的(在`BeanDefinition`实例中表示为`Class`的内部属性)。有两种方法来使用`Class`属性：
* 通常，容器本身通过反射调用其构造函数来直接创建Bean,这种方式等价于使用java代码执行new操作。
* 指定包含静态工厂方法的类，调用该方法来创建对象，这种情况不太常见。调用静态工厂方法返回的对象类型可以是同一个类，也可以是一个完全不同的类。

***内部类名***
*如果要为静态嵌套类配置Bean
定义，必须使用嵌套类的二进制名称*

*例如，如果在包`com.example`下有一个类是`Somthing`，并且这个类有一个静态嵌套类为`OtherThing`，那么bean定义的`class`属性将会是`com.example.Somthing$OtherThing`*

*注意，名称中使用`$`字符来区分嵌套类和外部类*

**使用构造函数初始化**
当通过构造函数来创建bean时，所有普通类都可以被spring使用并兼容。也就是说，这些类不需要实现特定的接口或者以特定的方式进行编码。只需要指定bean的class就足够了。但是，这取决于使用哪种类型的IoC来定义bean，可能需要一个默认的（空）构造函数。

Spring的IoC容器几乎可以管理任何需要管理的类。它不仅限于管理真实的JavaBeans。多数Spring的用户更喜欢这样的JavaBeans,它只有一个默认的无参构造函数，并且根据容器中的属性建模，含有适当的setter和getter方法。还可以在容器中拥有更多奇特的非bean样式的类。例如，需要使用不符合JavaBean规范的遗留数据库连接池，Spring也可以对其进行管理。

基于XML配置元数据，可以想下面的例子一样定义bean：
```
<bean id="exampleBean" class="examples.ExampleBean"/>
<bean name="anotherExample" class="examples.ExampleBeanTwo"/>
```

**通过静态工厂方法初始化**
当需要使用静态工厂方法来定义Bean时，属性`class`需要被指定为静态工厂类，`factory-method`属性用来指定工厂类的工厂方法。这个工厂方法应该能被调用并返回一个可用的对象，这个对象将被视为通过构造函数创建的。这种bean定义方式的一个用法是在遗留的代码中调用静态工厂。

下面的bean定义就是通过调用工厂方法来创建bean的。该定义不需要指定返回的类型（或者类），只需要指定该类的工厂方法。在这个例子中，`createInstance()`方法必须是静态方法。下面的例子将展示如何定义一个工厂方法：
```
<bean id="clientService"
    class="examples.ClientService"
    factory-method="createInstance"/>
```
下面的例子展示了可与前面的bean定义一直使用的类:
```
public class ClientService {
    private static ClientService clientService = new ClientService();
    private ClientService() {}

    public static ClientService createInstance() {
        return clientService;
    }
}
```

**通过实例工厂方法，初始化bean**
与静态工厂方法类似，在容器中，通过一个已经存在的bean，调用实例工厂方法来初始化一个新的bean。为了使用这种机制，需要将`class`属性设置为空，并且，需要在`factory-bean`属性中，指定当前容器（父容器或者祖先容器）所包含的需要调用的实例方法来创建对象。设置`factory-method`属性为工厂方法名称。下面的例子将展示如何配置这样的bean
```
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
    <!-- inject any dependencies required by this locator bean -->
</bean>

<!-- the bean to be created via the factory bean -->
<bean id="clientService"
    factory-bean="serviceLocator"
    factory-method="createClientServiceInstance"/>
```

下面的例子展示了相应的类：
```
public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    private static AccountService accountService = new AccountServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }

    public AccountService createAccountServiceInstance() {
        return accountService;
    }
}
```

*在spring文档中，"factory bean" 指在Spring容器中配置并通过实例或者静态工厂方法创建的bean对象。`FactoryBean`(注意大小写)只Spring的`FactoryBean`的实现类*

***确定bean的运行时类型***
确定特定bean的运行时类型并非易事。在bean元数据定义中，一个特定的类只是初始类的引用，与已经声明的工厂方法结合使用，或者是可能导致Bean的不同运行时类型的FactoryBean类，或根本不设置实例工厂方法（而通过制定`factory-bean`名称来代替）。另外，AOP代理可能使用基于接口代理来包装bean实例，而目标bean的实际类型的暴露程度有限。
找出bean实际的运行时类型的推荐做法是，通过调用`BeanFactory.getType`来获得。这考虑了上述所说的所有情况，并且对于相同的bean名称来说，返回了与调用`BeanFactory.getBean`相同的对象类型。

##1.4 依赖##
典型的企业级应用程序不只包含一个单独的对象（或者在spring属于中，成为bean）。即使最简单的应用程序，也有一些对象需要协同工作，以便为最终用户呈现一直的应用程序。下一部分将说明如何从定义多个独立的bean到实现对象写作，以实现完整的应用程序。

###1.4.1 依赖注入###
依赖注入是通过构造函数参数、工厂方法参数或者在构造或创建对象实例后，在对象实例上设置属性来定义其依赖关系。当容器创建bean时，会注入这些依赖。从本质上讲，这个过程是通过类的构造函数、Service Locator模式来控制bean自身依赖关系的逆过程，因此也成为IoC（控制反转）。

依赖注入使代码更简洁，更有效的解耦。对象本身不需要查找它自身的依赖，并且不需要定位这些依赖。结果是，这些类更容易测试，特别是当依赖项是在接口或者抽象基类上，允许在单元测试中模拟测试数据。

依赖注入主要由两种形式：构造函数的依赖注入和基于Setter方法的依赖注入

**基于构造函数的依赖注入**
基于构造函数的依赖注入，是容器调用构造函数来完成的。调用静态工厂方法来构造bean与基于构造函数的依赖注入有相同的效果。这里讨论的构造函数的参数与静态工厂方法的参数相似。下面的例子展示了一个类只能通过构造函数来注入以依赖。
```
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on a MovieFinder
    private MovieFinder movieFinder;

    // a constructor so that the Spring container can inject a MovieFinder
    public SimpleMovieLister(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // business logic that actually uses the injected MovieFinder is omitted...
}
```
注意，在这个类里没有其他特别的东西。他就是一个普通java对象（POJO）并且没有依赖任何的接口、基类或者注解。

**构造参数解析**
构造参数解析匹配通过使用参数的类型进行。如果bean定义的构造函数参数中不存在潜在的歧义，那么他的顺序就是bean被初始化时，构造器的顺序。考虑下面的类：

```
package x.y;

public class ThingOne {

    public ThingOne(ThingTwo thingTwo, ThingThree thingThree) {
        // ...
    }
}
```

假设`ThingTwo`和`ThingThree` 没有通过继承来关联，没有潜在的歧义。因此，如下的配置将会很好的工作，在<constructor-arg/>中不需要指定构造函数的indexes或者明确的类型。
例如：
```
<beans>
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg ref="beanTwo"/>
        <constructor-arg ref="beanThree"/>
    </bean>

    <bean id="beanTwo" class="x.y.ThingTwo"/>

    <bean id="beanThree" class="x.y.ThingThree"/>
</beans>
```

当另一个bean被引用时，他的类型是已知的，并且能够匹配（像前面的例子一样）。当使用一个简单类型时，例如<value>true</value>，spring不能决定value的类型，并且在没有帮助的情况下无法按类型进行匹配，考虑下面的类：

```
package examples;

public class ExampleBean {

    // Number of years to calculate the Ultimate Answer
    private int years;

    // The Answer to Life, the Universe, and Everything
    private String ultimateAnswer;

    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```

**构造参数配型匹配**
在前面的例子中，容器通过使用`type`属性来将简单类型匹配到构造参数中。例如：
```
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg type="int" value="7500000"/>
    <constructor-arg type="java.lang.String" value="42"/>
</bean>
```

**通过指定构造参数顺序**
可以使用`index`属性来明确的指定构造参数的顺序，例如：
```
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg index="0" value="7500000"/>
    <constructor-arg index="1" value="42"/>
</bean>
```

除了解决多个简单值的歧义性之外，指定索引还可以解决具有两个相同类型的参数的歧义性。

*注意：索引是从0开始的*

**通过指定参数名称**
可以通过指定构造函数的参数名称来消除歧义，如下面的例子:
```
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg name="years" value="7500000"/>
    <constructor-arg name="ultimateAnswer" value="42"/>
</bean>
```
请记住，代码必须在开启debug flag的情况下进行编译，以便spring可以从构造函数中查找参数名称。如果不想启用debug flag，可以使用`@ConstructorProperties`注解来明确参数的名字，如下面的例子：
```
package examples;

public class ExampleBean {

    // Fields omitted

    @ConstructorProperties({"years", "ultimateAnswer"})
    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```

**基于setter方法的依赖注入**
基于setter方法的依赖注入是在容器调用bean的无参构造函数或一个无参静态工厂方法初始化bean后，调用setter方法来实现的。

下面的立即展示了使用纯setter注入的例子。这个类是一个常规的Java类。他没有依赖特别的接口、基类或者注解。
```
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on the MovieFinder
    private MovieFinder movieFinder;

    // a setter method so that the Spring container can inject a MovieFinder
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // business logic that actually uses the injected MovieFinder is omitted...
}
```

`ApplicationContext`支持基于构造函数或者基于setter方法的依赖注入管理。在通过构造函数注入了某些依赖之后，它还支持基于setter的依赖注入。以`BeanDefinition`的形式配置依赖项，将其与`PropertyEditor`实例结合使用，将属性从一种格式转换为另一种格式。然而，大部分的spring用户不需要直接使用这些类而是使用XML的`bean`定义，组件注解或者基于在有`@Configuration`的Java类上，对方法使用`@Bean`注解。然后这些源在内部会转换为`BeanDefinition`实例，并用于加载整个Spring IoC容器实例。

-------
***基于构造函数还是基于setter？***
由于可以混合使用基于构造函数和setter的依赖注入，对于强依赖，使用构造函数，对于可选依赖，使用setter方法是一个很好的原则。注意，在setter方法上使用`@Required`注解会使依赖变成必须的，但是，最好使用带有参数校验的构造函数进行注入。

Spring 拥护构造函数注入，它可以让应用组件实现为不可变对象并且确保依赖不为`null`。此外，构造函数注入的组件始终以完全初始化的状态返回给客户端。附带说明一下，大量的构造函数参数是坏的代码，这暗示了这个类可能承担了太多的责任，应该对其重构以便更好地解决关注点分离的问题。

Setter注入只应该使用在类中分配合理的默认值的可选依赖项。除此之外，在使用这些依赖的代码中，必须进行非空检查。使用setter注入的一个好处是setter方法可以使该类的对象在以后重新分配或者重新注入。因此，通过`JMX MBeans`进行管理是使用setter注入的例子。

对特定的类也可以使用依赖注入。有时候，在决定使用第三方类，但是并没有源码的时候，到底使用哪种风格取决于用户。例如，如果第三方的类没有暴露任何的setter方法，那么构造函数注入的方式是唯一的选项。


-------

**依赖解析过程**
容器执行bean依赖的解析过程如下：
* 使用所有描述bean的元数据配置创建并初始化`ApplicationContext`。配置元数据可以通过XML、Java代码或者注解方式来定义。
* 对于每一个bean，它的依赖以属性、构造函数参数或者静态工厂方法参数来表示。当bean被实际创建时，这些依赖会提供给bean。
* 每一个属性或构造参数是一个实际被定义了的值或者是在容器中的另一个bean的引用。
* 每个属性或构造参数都将从其指定的格式转换为实际类型。默认情况下，spring可以将字符串提供的值转换为所有内置类型，如`int`，`long`，`String`，`boolean`等。

在创建容器时，spring容器会验证每个bean的配置。但是，bean属性在bean真正被创建时，才会设置。当容器被创建时，单例范围的bean会被预先初始化。在`Bean Scopes`中，定义范围。此外，仅在请求时才创建bean。创建和分配bean的依赖关系时，可能会导致创建一个bean图。请注意，不匹配的依赖项可能在后期出现，即第一次创建受影响的bean时。

***循环依赖***


-------
如果主要使用构造函数注入，则可能会创建无法解决的循环依赖问题。
例如：class A的构造参数需要class B的实例注入，并且class B的构造参数需要class A的实例注入。如果配置了class A和class B相互注入，Spring IoC容器会在运行时检测到循环引用，并且抛出`BeanCurrentlyInCreationException`。

一个解决办法是，重新编辑某些类的源码，用setter注入替换构造函数注入。或者，避免使用构造函数注入，只使用setter注入。 虽然不建议使用这样的方式，但是使用setter注入可以解决循环依赖问题。

与典型情况不同（没有循环依赖的情况），Bean A和Bean B之间的循环依赖关系迫使其中一个Bean在完全初始化之前被注入到另一个Bean（先有鸡还是先有蛋的问题）。

-------

通常可以相信Spring会做正确的事情。它在容器被加载时，会检测配置问题，例如引用不存在的bean、循环依赖。在bean被实际创建时，Spring尽可能的推迟解决依赖关系。这意味着如果创建对象或依赖项存在问题，已经正确加载的spring容器会在请求对象时发生异常-例如，bean抛出丢失属性或无效属性的异常结果。这可能会延迟某些配置问题的可见性，这就是为什么默认情况下`ApplicationContext`实现会预先实例化单例bean。在实际需要这些bean之前，花一些时间和内存来创建他们，这会在创建`ApplicationContext`时发现问题，而不是稍后才能发现。用户可以覆盖这种行为，以便单例延迟初始化，而不是预先初始化。

如果没有循环依赖的存在，当一个或者更多的协作bean被注入到一个依赖的bean时，每个协作的bean在注入到依赖bean之前都已经完全配置。这意味着，如果bean A对bean B有依赖关系，Spring IoC容器会在调用bean A的setter方法之前，事先完成bean B的配置。换句话说，这个bean被实例化（如果不是预先实例化的单例），它的依赖已经被设置，相关生命周期的方法被调用（例如`configured init method`，或者`InitializingBean callback method`）。

### 1.4.2 依赖和配置细节

在前面的章节中提到，可以定义bean属性和构造参数来引用其他被管理的bean或者被定义的值。基于XML配置的元数据支持`<property/>和<constructor-arg/>`子元素，来达到这个目的。

**字面值（原语、字符串等）**
`<property/>`元素的`value`属性是指一个属性或者构造参数，他们都被字符串来表示。Spring的`conversion service`用来将这些字符串转换为实际的属性或参数。下面的例子显示了多种value的设置：

```
<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <!-- results in a setDriverClassName(String) call -->
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
    <property name="username" value="root"/>
    <property name="password" value="misterkaoli"/>
</bean>
```

下面的例子使用`p`命名空间进行更简洁的XML配置
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close"
        p:driverClassName="com.mysql.jdbc.Driver"
        p:url="jdbc:mysql://localhost:3306/mydb"
        p:username="root"
        p:password="misterkaoli"/>
</beans>
```

上面的XML配置更简洁。然而，错别字会在运行时发现，并不是在设计时，除非IDE在定义bean时（例如Intellij IDEA或者Spring Tools for Eclipse）支持属性自动完成。强烈建议使用此类IDE帮助。

用户也可以配置一个`java.util.Properties`实例，如：
```
<bean id="mappings"
    class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">

    <!-- typed as a java.util.Properties -->
    <property name="properties">
        <value>
            jdbc.driver.className=com.mysql.jdbc.Driver
            jdbc.url=jdbc:mysql://localhost:3306/mydb
        </value>
    </property>
</bean>
```

Spring 容器使用JavaBeans的`PropertyEditor`机制，将`<value/>`元素内的文本转为一个`java.util.Properties`实例。

`idref`**元素**

`idref`元素只是一种防止错误的方法，可以将容器中另一个bean的id(**字面值，并不是引用**)传递给`<constructor-arg>`或者`<property/>`元素。下面的例子显示了如何使用它：
```
<bean id="theTargetBean" class="..."/>
<bean id="theClientBean" class="...">
    <property name="targetName">
        <idref bean="theTargetBean"/>
    </property>
</bean>
```

前面的例子与下面的例子具有相同的效果：
```
<bean id="theTargetBean" class="..." />
<bean id="client" class="...">
    <property name="targetName" value="theTargetBean"/>
</bean>
```
第一种形式比第二种形式更好，因为使用`idref`标签让容器在部署时验证引用的命名bean实际上是否存在。在第二个例子中，不会对传递的`targetName`执行验证。拼写错误尽在实例化客户端bean时才发现（可能导致致命的结果）。如果`client`是一个原型bean，这种错误和异常结果可能在部署容器很久之后才能被发现。

*idref元素的local属性在bean XSD4.0中不在提供支持。*

`<idref>`元素在Spring 2.0之前的版本中，通常被用在ProxyFactoryBean 定义的AOP拦截器配置中。指定拦截器名称时，使用`<idref/>`元素可防止拼写出错误的拦截器ID。

**引用其他的bean**
`ref`元素是`<constructor-arg/>`或`<property/>`元素内的最终的元素。容器管理的bean可以被设置为bean的属性。被引用的bean是要设置属性bean的依赖，并且在设置属性之前需要初始化它。（如果协作者是单例bean，他可能已经被容器初始化了。）所有引用最终都是对另一个对象的引用。范围和验证取决于是否通过`bean`还是`parent`属性指定一个对象的ID或者名称。

通过`<ref/>`标记的bean属性指定目标bean是最通用的形式，它允许在同一容器或者父容器中创建任何bean的引用。不管是否是在同一个XML文件中。`bean`的值可能与目标bean的`id`属性相同或者与目标bean的`name`属性相同。下面的例子展示了如何使用`ref`元素
```
<ref bean="someBean">
```

通过`parent`属性来指定目标bean可以创建当前容器的父容器中的bean的引用。`parent`属性的值可能与目标bean的`id`属性或者目标bean的`name`属性相同。目标bean必须在当前容器的父容器中。这种变量主要应该用在具有层级结构的容器中和想要在父容器中将现有bean包装在父容器的同名代理bean，下面的例子展示了如何使用`parent`属性：

```
<!-- in the parent context -->
<bean id="accountService" class="com.something.SimpleAccountService">
    <!-- insert dependencies as required as here -->
</bean>
```

```
<!-- in the child (descendant) context -->
<bean id="accountService" <!-- bean name is the same as the parent bean -->
    class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target">
        <ref parent="accountService"/> <!-- notice how we refer to the parent bean -->
    </property>
    <!-- insert other configuration and dependencies as required here -->
</bean>
```

**内部Bean**
在`<property/>`或者`<constructor-arg>`内部定义bean使用`<bean/>`标签，例如：
```
<bean id="outer" class="...">
    <!-- instead of using a reference to a target bean, simply define the target bean inline -->
    <property name="target">
        <bean class="com.example.Person"> <!-- this is the inner bean -->
            <property name="name" value="Fiona Apple"/>
            <property name="age" value="25"/>
        </bean>
    </property>
</bean>
```

一个内部的bean定义不需要指定ID或者name。如果指定了，容器也不会使用该值作为标识符。容器也会忽略`scope`标志位，因为内部beans总是匿名的并且总是随着外部bean被创建。不可能独立访问到内部bean或者将他们注入到其他bean中。

一个极端情况，可以从自定义范围接受销毁回调-例如，一个单例bean包含范围是request-scoped的内部bean。内部bean实例的创建与包含它的bean绑定在一起，但是销毁回调使其可以参与request scope范围的生命周期。这不是普遍的情况。内部bean通常只共享其包含bean的作用于。

**集合**
`<list/>`，`<set/>`，`<map/>`，`<props/>`元素可以设置集合类型的属性参数，他们分别对应Java集合类型的`List`，`Set`，`Map`，`Properties`。下面的例子将展示如何使用他们：
```
<bean id="moreComplexObject" class="example.ComplexObject">
    <!-- results in a setAdminEmails(java.util.Properties) call -->
    <property name="adminEmails">
        <props>
            <prop key="administrator">administrator@example.org</prop>
            <prop key="support">support@example.org</prop>
            <prop key="development">development@example.org</prop>
        </props>
    </property>
    <!-- results in a setSomeList(java.util.List) call -->
    <property name="someList">
        <list>
            <value>a list element followed by a reference</value>
            <ref bean="myDataSource" />
        </list>
    </property>
    <!-- results in a setSomeMap(java.util.Map) call -->
    <property name="someMap">
        <map>
            <entry key="an entry" value="just some string"/>
            <entry key ="a ref" value-ref="myDataSource"/>
        </map>
    </property>
    <!-- results in a setSomeSet(java.util.Set) call -->
    <property name="someSet">
        <set>
            <value>just some string</value>
            <ref bean="myDataSource" />
        </set>
    </property>
</bean>
```

map的key或value，set的value也可以使一下任意元素：
```
bean | ref | idref | list | set | map | props | value | null
```

**合并集合**
Spring容器支持集合的合并。应用开发者可以定义一个`<list/>`，`<set/>`，`<map/>`，`<props/>`的父元素，并且子元素`<list/>`，`<set/>`，`<map/>`，`<props/>`可以集成并覆盖父元素的值。子集合框架的值就是父元素和子元素合并后的值。

这个章节是讨论父子bean的合并机制。不熟悉父bean和子bean定义的可能虚妄在之前阅读相关的内容。

下面的例子展示了集合合并：
```
<beans>
    <bean id="parent" abstract="true" class="example.ComplexObject">
        <property name="adminEmails">
            <props>
                <prop key="administrator">administrator@example.com</prop>
                <prop key="support">support@example.com</prop>
            </props>
        </property>
    </bean>
    <bean id="child" parent="parent">
        <property name="adminEmails">
            <!-- the merge is specified on the child collection definition -->
            <props merge="true">
                <prop key="sales">sales@example.com</prop>
                <prop key="support">support@example.co.uk</prop>
            </props>
        </property>
    </bean>
<beans>
```

注意，需要在子bean的`<props/>`元素中使用了`merge=true`属性。当子bean被容器解析和初始化时，实例结果的`adminEmails`属性包含了合并后的结果，下面的结果展示了合并后的结果：
```
child:{support=support@example.co.uk, administrator=administrator@example.com, sales=sales@example.com}
```
合并行为支持`<list/>`，`<set/>`，`<map/>`，`<props/>`集合类型。对于`<list/>`元素，语义上和`List`集合类型一致（这就意味着需要注意集合的顺序）。父列表的值优先于子列表的值。对于`Map`，`Set`和`Properties`的集合类型，不存在顺序问题。因此，任何排序语义对这些集合都是无效的。

**集合合并的限制**
不能讲不通类型的集合进行合并（例如`Map`和`List`）。如果尝试这样做，将会抛出异常。`merge`属性必须在更低层具有集成关系的孩子中定义。定义在父集合的`merge`是多余的，不会进行合并。

**强类型集合**
随着在Java5中引入泛型类型，可以定义集合框架的类型。这意味着可以声明集合框架的类型（例如集合中只包含`String`类型）。如果使用Spring依赖注入一个强类型的集合框架到bean中，可以利用Spring提供的类型转换支持，以便将请类型的集合框架实例的元素转换为适当的类型，然后添加到集合框架中。下面的例子展示了如何定义这样的bean：
```
public class SomeClass {

    private Map<String, Float> accounts;

    public void setAccounts(Map<String, Float> accounts) {
        this.accounts = accounts;
    }
}
```

```
<beans>
    <bean id="something" class="x.y.SomeClass">
        <property name="accounts">
            <map>
                <entry key="one" value="9.99"/>
                <entry key="two" value="2.75"/>
                <entry key="six" value="3.99"/>
            </map>
        </property>
    </bean>
</beans>
```

当`accounts`的`something`属性准备注入时，元素类型的泛型信息通过`Map<String,FLoat>`的强类型反射得到。Spring的类型转换将多个值转换为`Float`的实际类型。

**Null和空字符串**
Spring对于属性为空的参数类似于空`Strings`。下面的配置设置的`email`属性的值为("")。
<bean class="ExampleBean">
    <property name="email" value=""/>
</bean>

前面的例子与下面的代码类似：
```
exampleBean.setEmail("");
```

`<null/>`标签用来处理`null`值。例如下面的例子：
```
<bean class="ExampleBean">
    <property name="email">
        <null/>
    </property>
</bean>
```
上面的例子与下面的代码类似：
```
exampleBean.setEmail(null);
```

**具有p命名空间的XML快捷方式**
p命名空间可以用来代替<property/>元素，描述bean的属性。

Spring支持基于XML Schema扩展命名配置格式。

下面的例子会解析出同样的结果：
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean name="classic" class="com.example.ExampleBean">
        <property name="email" value="someone@somewhere.com"/>
    </bean>

    <bean name="p-namespace" class="com.example.ExampleBean"
        p:email="someone@somewhere.com"/>
</beans>
```

这个例子展示了在bean定义中，属性使用p命名空间。Spring包含了属性的声明。前面提到了，p命名空间没有schema定义，所以可以设置属性的名字到property上。

下面的例子包含了引用另一个bean的定义：
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean name="john-classic" class="com.example.Person">
        <property name="name" value="John Doe"/>
        <property name="spouse" ref="jane"/>
    </bean>

    <bean name="john-modern"
        class="com.example.Person"
        p:name="John Doe"
        p:spouse-ref="jane"/>

    <bean name="jane" class="com.example.Person">
        <property name="name" value="Jane Doe"/>
    </bean>
</beans>
```

这个例子不仅包含了使用p命名空间来设置属性值，而且使用了特殊的格式来声明引用。第一个bean使用了`<property name="spouse" ref="jane"/>`，创建从bean john到bean jane的引用，第二个bean定义使用了`p:spouse-ref="jane`作为属性来达到相同的目的。在这个例子中，`spouse`是属性名，`-ref`部分表名这个值不是字面量值，而是另一个bean的引用。

*p命名空间不是灵活的XML标准格式。例如，声明属性引用的格式与以`Ref`结尾的属性有冲突，标准的XML格式就不会。建议小心选择适当的格式，避免同时使用三种格式*

**c命名空间的XML快捷方式**
与p命名空间类似，c命名空间出现在Spring 3.1，用构造函数的内联属性参数代替`<construct-arg>`元素。

下面的例子使用c命名空间，展示基于构造函数的注入：

```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:c="http://www.springframework.org/schema/c"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="beanTwo" class="x.y.ThingTwo"/>
    <bean id="beanThree" class="x.y.ThingThree"/>

    <!-- traditional declaration with optional argument names -->
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg name="thingTwo" ref="beanTwo"/>
        <constructor-arg name="thingThree" ref="beanThree"/>
        <constructor-arg name="email" value="something@somewhere.com"/>
    </bean>

    <!-- c-namespace declaration with argument names -->
    <bean id="beanOne" class="x.y.ThingOne" c:thingTwo-ref="beanTwo"
        c:thingThree-ref="beanThree" c:email="something@somewhere.com"/>

</beans>
```

`c:`命名空间和`p:`命名空间有相同的约定（以`-ref`结尾表示bean的引用）。类似的，它需要通过XML文件声明，没有呗定义在XSD schema中。

对于极少数情况下，构造函数的参数名字是不可用的（通常如果是被编译好的字节码并且没有debug信息），可能会使用参数的索引来表示参数，例如：
```
<bean id="beanOne" class="x.y.ThingOne" c:_0-ref="beanTwo" c:_1-ref="beanThree"
    c:_2="something@somewhere.com"/>
```

*由于XML语法，索引符号需要使用`_`表示，作为XML的属性名，不允许以数字开头。一个正确的索引符号也可以用在<construct-arg>元素中，但是不常用，因为那里通常只需要简单的声明即可。*

实践中，构造函数解析机制对于匹配参数来说是非常有效的，除非真的需要这样做，建议在配置中使用名字符号来表示。

**复合属性名**
当设置bean属性时，可以使用复合或者嵌套属性名，只要路径的所有组成部分（最终属性名除外）都不为`null`。考虑下面的bean定义：
```
<bean id="something" class="things.ThingOne">
    <property name="fred.bob.sammy" value="123" />
</bean>
```

### 1.4.3. 使用`depends-on`
如果一个bean是另一个bean的依赖，这通常意味着这个bean需要被设置为另一个bean的属性。通常可以通过`<ref/>`元素来完成。然而，有时候bean之间的依赖不太直接。例如，当一个类需要触发静态初始化时，就像数据库驱动注册。`depends-on`属性可以强制在bean使用他们之前被初始化。下面的例子展示了使用`depends-on`属性来表示一个单例bean的依赖：
```
<bean id="beanOne" class="ExampleBean" depends-on="manager"/>
<bean id="manager" class="ManagerBean" />
```

为了表示依赖多个bean，`depends-on`支持bean名称的列表（逗号，空白符或者分号来分割）：
```
<bean id="beanOne" class="ExampleBean" depends-on="manager,accountDao">
    <property name="manager" ref="manager" />
</bean>

<bean id="manager" class="ManagerBean" />
<bean id="accountDao" class="x.y.jdbc.JdbcAccountDao" />
```

*`depends-on`属性既可以指定初始化时间相关性，也可以仅在单例bean的情况下指定相应的销毁时间相关性。与给定bean定义依赖关系的从属bean首先被销毁，然后再销毁给定bean本身。因此，依赖也可以控制关闭顺序。*

### 1.4.4. 延迟初始化Bean

默认情况下，在初始化过程中，ApplicationContext的实现会尽可能早的创建和配置所有单例bean。通常，提前这种预实例化是可取的，因为错误的配置或者错误的环境会立即被发现，而不是很久以后才发现。当不需要这种预处理时，可以通过标记延迟加载来标记bena定义。一个延迟加载的bean会通知IoC容器，当它被请求时，才会创建实例而不是在启动时就创建。

在XML中，这种行为是通过`<bean/>`元素中的`lazy-init`来控制的，下面的例子展示了延迟加载：
```
<bean id="lazy" class="com.something.ExpensiveToCreateBean" lazy-init="true"/>
<bean name="not.lazy" class="com.something.AnotherBean"/>
```

当前面的配置被`ApplicationContext`使用时，启动`ApplicationContext`时，不急于预先实例化被标记为`lazy-init`的bean，其他的bean被预先实例化。

然而，当延迟初始化的bean是一个单例bean的依赖，这会导致这个bean不会延迟初始化，`ApplicationContext`会在启动阶段创建延迟bean的实例，因为它biubiu满足单例的依赖。延迟初始化的bean被注入到其他未初始化的单例bean中。

通过使用`<bean/>`元素中的`default-lazy-init`属性，可以在容器级别上控制延迟初始化，例如下面的例子：
```
<beans default-lazy-init="true">
    <!-- no beans will be pre-instantiated... -->
</beans>
```

### 1.4.5 自动装配

Spring容器可以自动装配互相协作的bean。可以通过检查`ApplicationContext`的内容，让Spring来自动解析协作者。自动装配的优点如下:
* 自动装配可以显著的减少属性或者构造参数。（在这方面，其他机制例如bean模板，也很有价值。）
*  随着对象的演变，自动装配可以更新配置。例如，如果需要在一个类中添加依赖，在不需要修改配置的情况下就可以自动满足依赖。自动装配对于开发阶段特别有用，当代码库变得更加稳定时，不需要取消明确的自动装配选项。

当使用基于XML配置的元数据时，可以通过`<bean/>`标签的`autowire`属性来指定自动装配的模式。自动装配功能有4中模式。可以为每个bean指定自动装配。下面的表格描述了自动装配的模型：

| Mode          | Explanation                                                                                                                                          |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `no`          | (默认的)没有自动装配。bean引用必须通过`ref`元素来定义。对于大型开发项目来说，不建议改变默认的设置，因为明确指定协作者可以提供更大的控制力和清晰度。在某种程度上，它记录了系统的结构。                                                     |
| `byName`      | 通过属性名字来自动装配。Spring要查找和需要自动装配的属性同名的bean。例如，如果一个bean通过名字设置的自动装配，并且它包含一个`master`的属性（也就是说，它有一个`setMaster()`的方法），Spring会查找一个名字为`master`的bean并且把它设置到这个属性上。 |
| `byType`      | 如果容器中恰好存在一个该属性类型的bean，则该属性会自动装配。如果有超过一个的存在，会引发致命的异常，这表明可能不该为该bean使用`byTppe`                                                                          |
| `constructor` | 与`byType`类似，但是是用在构造参数上的。如果容器中不存在和构造参数一个类型的bean，将会引发致命错误。                                                                                             |

使用`byType`或者`constructor`自动装配模式，可以装配数组和集合框架。在这些情况下，容器中所有自动装配的候选期望与预期的类型匹配，以满足依赖。如果希望`Map`的key类型是`String`，可以自动装配一个强类型的`Map`实例。一个自动装配的`Map`实例的值包含所有希望匹配到该类型的bean实例，并且Map的实例的keys包含响应的bean名称。

**自动装配的局限和缺点**

当在项目中一致使用自动装配时，效果最好。如果通常不使用自动装配，则可能会使开发人员仅适用自动装配来链接一两个bean定义而感到困惑。

自动装配的缺点和局限如下：
* 属性和构造函数参数中的显示依赖会始终覆盖自动装配。不能装配原始类型，如`String`，和`Classes`（以及此类简单属性的数组）。这是设计上的局限性。
* 自动装配不如显示装配精确。尽管如此，Spring还是谨慎地避免在可能产生意外结果的歧义情况下进行猜测。Spring管理的对象之间的关系不在有明确记录。
* 装配信息可能对于需要从Spring容器中生成文档的工具来说不适用。
* 容器内的多个bean定义可能与setter方法或者构造函数参数指定的类型匹配。对于数组、集合或者`Map`实例，这不一定是问题。然而，对于需要单个值的依赖项，不会任意解决此歧义。如果没有唯一可用的bean定义，会抛出一个异常。

在后一种情况下，有几种选择：
* 放弃自动装配，转而使用明确的装配
* 通过设置bean的`autowire-candidate`属性为`false`来避免自动装配
* 通过将其`<bean>/`元素的`primary`属性设置为`true`，将单例bean定义为主要的候选项。
* 通过基于注解的配置，更细粒度的实现控件。

**从自动装配中排除bean**
在每个bean的基础上，可以从制动装配中排除一个bean。在Spring的XML格式中，设置`<bean/>`元素的`autowire-candidate`属性为`false`。容器使用特定的bean定义，使自动装配不可用（包括注解配置，例如使用`@Autowired`）。

*`autowire-candidate`属性仅影响类型的自动装配。它不会影响按名称的显示引用，即使未将制定的bean标记为自动装配的候选项，该名称也可以得到解析。因此，如果名称匹配，按名称自动装配仍然会注入bean。*

可以基于bean名称的模式匹配来限制自动装配候选。顶级元素`<beans/>`在其`default-autowire-candidates`属性中接受一个或者更多的模式。例如，限制候选状态是以`Repository`结尾的任何bean，需要提供一个`*Repository`字符值。对于多个匹配模式，可以使用逗号分隔符。bean定义的`autowire-candidate`属性的值`true`或者`false`始终是优先的。对于此类bean，匹配模式不会生效。

这些技巧对于不希望通过自动装配来注入bean非常有用。这并不意味着排除的bean本身不能进行自动装配。相反，bean本身不能变为其他bean的候选装配对象。

### 1.4.5 方法注入

在大多数场景中，容器中的多数bean是单例的。当一个单例bean需要和另一个单例bean协作或一个非单例bean需要和另一个非单例bean协作时，通常将一个bean定义为另一个bean的属性来处理依赖关系。当bean的生命周期不同时，问题就出现了。假设单例bean A需要一个非单例（属性）bean B，也许实在A的每个方法上调用。容器只创建单例bean A一次，因此只有一次机会去设置属性。容器不能在需要的时候给bean A每次都提供一个新的bean B的实例。

解决方案是放弃某些控制反转。可以通过实现`ApplicationContextAware`接口使bean A知道该容器，使每次在bean A需要的时候，通过请求一个新的bean B的实例，下面的方法展示了这种情况：

```
// a class that uses a stateful Command-style class to perform some processing
package fiona.apple;

// Spring-API imports
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CommandManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Object process(Map commandState) {
        // grab a new instance of the appropriate Command
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    protected Command createCommand() {
        // notice the Spring API dependency!
        return this.applicationContext.getBean("command", Command.class);
    }

    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
```

前面的例子是不可取的，因为业务逻辑代码和Spring框架耦合了。方法注入是Spring IoC容器提供的一项高级功能，可以更干净地处理此用例。

**查找方法注入**
查找方法注入是容器重写容器管理的bean上的方法并返回容器中另一个命名bean的查找结果的能力。查找通常涉及原型bean，如上一节所述。Spring框架通过使用CGLIB库的字节码来动态生成覆盖该方法的子类，从而实现此方法注入。

* *为了让动态生成的子类工作，Spring容器的子类不能是final，而要覆盖的方法也不能是final修饰的方法。*
* *对具有抽象方法的类进行单元测试，需要用户自己对该类进行子类化，并提供抽象方法的实现。*
* *组件扫描时，需要具体的类去选择具体的方法。*
* *另一个关键限值是，查找方法不适用于工厂方法，尤其不适用与配置类中的`@Bean`方法。因为在这种情况下，容器不负责创建实例，因此无法创建运行时生成的动态子类*。

对于上面的`CommandManager`类中，Spring可以 容器动态覆盖并实现`createCommand()`方法，并且`CommandManager`类没有任何Spring
依赖，如下面的例子：
```
package fiona.apple;

// no more Spring imports!

public abstract class CommandManager {

    public Object process(Object commandState) {
        // grab a new instance of the appropriate Command interface
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    // okay... but where is the implementation of this method?
    protected abstract Command createCommand();
}
```

每当需要新的`myCommand` bean实例时，标识为`commandManager`的bean就会调用自己的`createCommand()`方法。`myCommand`bean作为一个原型类，在缺失需要的时候才使用。如果是一个单例，`myCommand`会在每次返回相同的实例。

还有一种方法可是实现方法查找注入，那就是基于注解的组件模型。可以通过`@Lookup`注解声明，下面的例子展示了如何使用这种技术：
```
public abstract class CommandManager {

    public Object process(Object commandState) {
        Command command = createCommand();
        command.setState(commandState);
        return command.execute();
    }

    @Lookup("myCommand")
    protected abstract Command createCommand();
}
```

或者，更惯用的方法是，可以依赖目标bean根据lookup方法的声明的返回类型来解析，如下：
```
public abstract class CommandManager {

    public Object process(Object commandState) {
        MyCommand command = createCommand();
        command.setState(commandState);
        return command.execute();
    }

    @Lookup
    protected abstract MyCommand createCommand();
}
```

注意，通常应该使用具体的子类实现声明此类带注释的查找方法，以使他们与Spring组件扫描规则兼容（默认情况下，抽象类会被忽略）。此限制不适用于显示注册或者显示导入的Bean类。

*另外一种访问不同范围目标bean的方法是一个`ObjectFactory/Provider`注入点，可查看Bean的依赖范围*
*`ServiceLocatorFactoryBean`或许会有帮助*

**任意方法替换**

相比查找方法注入，一个更少使用的方法注入形式是任意方法替换。

基于XML配置的元数据，可以使用`replace-method`元素来替换已经存在的方法实现。思考下面的类，有一个方法叫`computeValue`是需要覆盖的：

```
public class MyValueCalculator {

    public String computeValue(String input) {
        // some real code...
    }

    // some other methods...
}
```

实现了`org.springframework.beans.factory.support.MethodReplacer`的类提供了一个新的方法定义，如下：
```
/**
 * meant to be used to override the existing computeValue(String)
 * implementation in MyValueCalculator
 */
public class ReplacementComputeValue implements MethodReplacer {

    public Object reimplement(Object o, Method m, Object[] args) throws Throwable {
        // get the input value, work with it, and return a computed result
        String input = (String) args[0];
        ...
        return ...;
    }
}
```

原始类定义和指定的覆盖方法类似如下例子：
```
<bean id="myValueCalculator" class="x.y.z.MyValueCalculator">
    <!-- arbitrary method replacement -->
    <replaced-method name="computeValue" replacer="replacementComputeValue">
        <arg-type>String</arg-type>
    </replaced-method>
</bean>

<bean id="replacementComputeValue" class="a.b.c.ReplacementComputeValue"/>
```

在`<replaced-method/>`元素中，可以使用一个或者多个`<arg-type/>`元素来指定被覆盖方法的签名。如果方法被重载并且有多重存在的变体，签名参数是必须的。为了方便，字符串类型的参数可以用子字符串来代替全限定类型名。例如下面的例子都可以匹配到`java.lang.String`：

```
java.lang.String
String
Str
```

因为参数数量通常足以区分每个可能的选择，通过仅键入与参数类型匹配的最短字符串，来节约键入时间。

## 1.5 Bean范围
当创建一个bean definition时，通过它可以创建一个清单，这个清单是用来创建实例的。这个想法非常重要，它和类一样，可以通过一个清单，创建多个类的实例对象。

用户不仅可以从bean definition中控制对象的各种依赖和配置值，而且可以控制bean的范围。这个方法是强大而灵活的，因为可以通过配置可以选择创建对象的范围，不需要在Java类级别拷贝对象的范围。Bean可以被定义多个范围中的一个。Spring支持6中范围，如果建立web应用程序是，其中4中才可以使用。用户也可以自己创建bean范围。

下面的表格描述了支持的范围：

| Scope       | Description                                                           |
|-------------|-----------------------------------------------------------------------|
| singleton   | Spring IoC容器默认创建的是单例bean。                                             |
| prototype   | 将单个bean定义的作用域限定为任意数量                                                  |
| request     | 将单个bean定义的作用于限定为单个HTTP 请求的生命周期内。因此，每个HTTP请求会生成一个实例对象。只有当应用为web程序时，才有效 |
| session     | 将单个bean定义的作用于限定为单个HTTP Session的生命周期内。只有当应用为web程序时，才有效                 |
| application | 将单个bean定义的作用于限定为这个ServletContext生命周期内。只有当应用为web程序时，才有效                |
| websocket   | 将单个bean定义的作用于限定为一个WebSocket的生命周期内。只有当应用为web程序时，才有效。                   |

### 1.5.1 单例范围

仅管理一个单例bean的共享实例，所有请求通过ID来匹配bean definition会导致Spring 容器返回一个bean的特殊实例。

换一个方式，当定义一个bean definition并且它的范围是单例，Spring IoC容器会创建该bean definition所定义个对象的一个实例。这个实例被缓存在singleton beans中，所有后续的请求或引用这个被命名的bean都将获得缓存对象。下面的图展示了单例范围是如何工作的：

![](https://raw.githubusercontent.com/Aris4009/attachment/main/singleton.png)

Spring中的单例bean的概念与GoF书中定义的单例模式不同。GoF单例是在对象中硬编码，对于每个特定的类，在每个类加载器中，只有一个该类的实例。Spring的单例范围最好的描述是每个容器每个bean。这意味着，如果在一个单独的Spring容器中为一个特定的类定义bean，这个Spring容器通过bean definition只会创建一个该类的实例。单例范围是Spring定义bean的默认范围。如果使用XML作为配置，可以像下面例子那样定义bean：
```
<bean id="accountService" class="com.something.DefaultAccountService"/>

<!-- the following is equivalent, though redundant (singleton scope is the default) -->
<bean id="accountService" class="com.something.DefaultAccountService" scope="singleton"/>
```

### 1.5.2. 原型范围

非单例的原型范围，会导致在每次请求指定bean时，创建一个新的实例。也就是说，该bean被注入到另一个bean或者通过`getBean`方法调用来请求他。有一条规则，用户应该在有状态的bean中使用原型范围而在无状态的bean中使用单例范围。

下面这个图证实了Spring中的原型范围：

![](https://raw.githubusercontent.com/Aris4009/attachment/main/20201102173503.png) 

（一个DAO不是典型的原型配置，因为典型的DAO不需要持有任何会话状态。对用户来说，重用单例图的核心更加容易。）

下面的定义展示了如何定义一个原型：
```
<bean id="accountService" class="com.something.DefaultAccountService" scope="prototype"/>
```

对比其他bean范围，Spring不会完全管理原型bean的生命周期。容器负责初始化、配置，组装原型对象，然后交给客户端，而无需对该原型实例进一步记录。因此，虽然初始化生命周期的回调方法在所有对象上呗调用，而不管作用域如何，对于原型，不会调用已配置的销毁生命周期回调。客户端代码必须清除原型对象，并且释放持有的昂贵的系统资源。为了使Spring容器释放原型作用域拥有的资源，可是尝试使用一个特定的`bean post-processor`，其中包含那些需要清理的bean的引用。

在某些方面，Spring容器的原型作用域是用来代替Java的`new`运算符的。超过这点的所有生命周期管理必须由客户端处理。

### 1.5.3 具有原型依赖关系的单例bean

当使用对原型bean有依赖的单例作用域的bean时，依赖关系在实例化时期已经被解析。因此，如果在一个单例作用域的bean中注入原型作用域的bean，原型bean已经被实例化然后被注入到单例bean中。这个实例是提供给单例bean的唯一实例。

然而，假设在运行时，想要让单例bean重复请求一个新的原型bean实例，不能讲原型bean注入到单例bean中。因为当容器初始化单例bena并且解析和注入他的依赖时，注入仅发生一次。如果需要运行时获取一个原型bean的实例，需要参考方法注入。

### 1.5.3 Request,Session,Application,WebSocket作用域
仅在使用web感知的Spring `Application`的实现时（例如：`XmlWebApplicationContext`），才能使用
`request`,`session`,`application`,`websocket`这四种bean作用域。如果将这些作用域与常规Spring IoC容器一起使用，例如`ClassPathXmlApplication`，会抛出`IllegalStateException`异常。

**初始化Web配置**

为了支持上面四种bean作用域，在定义bean时，需要一些少量的初始配置。（这些配置不需要在`singleton`和`prototype`这两种标准作用域中设置）。

如何完成初始化配置，依赖于用户特定的Servlet环境。

如果通过Spring Web MCV访问bean的作用域，事实上，是通过Spring的`DispatcherServlet`来处理请求的，不需要特殊的设置，`DispatcherServlet`   已经暴露了所有相关的状态。

如果使用Servlet 2.5的web容器，请求在Spring的`DispatcherServlet`外部被处理（例如，当使用JSF或者Struts），需要注册`org.springframework.web.context.request.RequestContextListener``ServletRequestListener`。对于Servlet3.0以后的版本，可以通过使用`WebApplicationInitializer`接口编程来实现。还有另一种方法，对于旧的容器，通过在web应用的`web.xml`中增加如下声明：
```
<web-app>
    ...
    <listener>
        <listener-class>
org.springframework.web.context.request.RequestContextListener
        </listener-class>
    </listener>
    ...
</web-app>
```

或者，如果监听器设置有问题，考虑使用Spring的`RequestContextFilter`。这个过滤器依赖web应用程序的配置，所以里必须适当的改变它。下面的监听器列出了web应用程序过滤器的一部分内容：
```
<web-app>
    ...
    <filter>
        <filter-name>requestContextFilter</filter-name>
        <filter-class>org.springframework.web.filter.RequestContextFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>requestContextFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>
```

`DispatcherServlet`，`RequestContextListener`，`RequestContextFilter`都有相同的作用，即将HTTP请求对象绑定到处理请求的线程上。这使得在请求和会话作用域的bean可以在调用链的下游使用。

**Request作用域**

思考下面定义bean的XML配置：
```
<bean id="loginAction" class="com.something.LoginAction" scope="request"/>
```

Spring容器通过使用`loginAction`定义，在每次HTTP请求时，创建一个`LoginAction`bean的实例。也就是说，`loginAction`bean的作用域是HTTP request级别的。用户可以在实例创建后随意改变该实例的状态，因为其他相同的实例不会看到这些状态的改变。他们都是对于单个请求的。当请求完成处理后，这个作用域的实例就会被丢弃。

当使用基于注解驱动组件或者Java配置时，`@RequestScope`注解可以用来将组件分配为`request`的作用域。下面的例子就是这么做的：

```
@RequestScope
@Component
public class LoginAction {
    // ...
}
```

**Session作用域**
思考下面定义bean的XML配置：
```
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>
```

Spring容器对于在单个HTTP `Session`的生命周期内，通过`userPreferences`bean定义创建一个新的实例。换句话说，`userPreferences`bean的有效作用于实在HTTP `Session`级别。与request作用于的bean一样，用户可以在实例被创建后改变内部状态，其他HTTP `Session`实例在使用`userPreferences`bean定义创建实例时，看不到这些状态的变化，因为他们是对于单个HTTP `Session`的。当HTTP `Session`最终被丢弃，这个bean也会被丢弃。

当使用注解驱动组件或者Java配置时，可以使用`@SessionScope`注解来讲组件的作用域定义为`session`级别。

```
@SessionScope
@Component
public class UserPreferences {
    // ...
}
```

**Application作用域**
思考下面定义bean的XML配置：
```
<bean id="appPreferences" class="com.something.AppPreferences" scope="application"/>
```

Spring容器在一个web应用程序中，通过`appPreferences`bean定义创建一个新的`AppPreferences`实例。这个实例的范围是`ServletContext`级别的，并且被存储到`ServletContext`属性中。这在某些地方和Spring的单例bean相似，但是有两个重要的不同：它对每个`ServletContext`是单例，而不是对Spring的`ApplicationContext`（在给定的Web应用程序中可能有多个），并且它是公开的，因此作为`ServletContext`属性。

当使用基于注解驱动的组件或者Java配置时，可以使用`ApplicationScope`注解来分配组件的作用域为`application`。下面的例子就是这样做的：

```
@ApplicationScope
@Component
public class AppPreferences {
    // ...
}
```

**有作用域的bean作为依赖**

Spring IoC容器不仅管理者对象的实例化，而且会装配协作者（或者依赖）。如果想注入（像例子一样）的一个HTTP request作用域的bean注入到另一个作用域范围更长的bean中，可能选择注入一个作用域的AOP代理。也就是说，需要注入一个代理对象，和作用域bean一样，暴露相同的公共接口，但是可以从相关作用域获得目标对象，并且用委托方法调用真实对象。

*也可以在单例bean之间，使用`<aop:scoped-proxy/>`，然后，通过引用中间代理序列化，因此在反序列化时可以重新获取目标单例bean*

*针对`prototype`作用域的bean声明`<aop:scoped-proxy/>`时，共享代理的每个方法调用都会导致创建新的目标实例，然后将该调用转发到目标实例。*

同样，作用域的代理不是以安全生命周期的方式从较短的作用域方位bean的唯一方法。用户也可以声明注入点（也就是说，构造函数或者setter方法参数或者自动装配字段），声明为`ObjectFactory<MyTargetBean>`，允许在每次需要时，调用`getObject()`来获取当前实例，无需持有这个实例或者单独存储它。

作为扩展的变体，可能会声明`ObjectProvider<MyTargetBean>`，它提供了几个附加的访问变体，包括`getIfAvailable`和`getIfUnique`。

这种JSR-330变体被称为`Provider`并且经常使用一个`Provider<MyTargetBean>`声明，通过调用`get()`来尝试获取对象。

下面的配置只有一行，但是对于理解为什么和怎么做有重要的帮助：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- an HTTP Session-scoped bean exposed as a proxy -->
    <bean id="userPreferences" class="com.something.UserPreferences" scope="session">
        <!-- instructs the container to proxy the surrounding bean -->
        <aop:scoped-proxy/> 【1】
    </bean>

    <!-- a singleton-scoped bean injected with a proxy to the above bean -->
    <bean id="userService" class="com.something.SimpleUserService">
        <!-- a reference to the proxied userPreferences bean -->
        <property name="userPreferences" ref="userPreferences"/>
    </bean>
</beans>
```

【1】这一行定义了代理

为了创建一个代理，可以在bean定义中插入一个`<aop:scoped-proxy/>`子元素。为什么需要这么做？思考下面的单例bean定义并且和前面提到的做对比（注意，下面的`userPreferences`定义是不完整的）。

```
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

在前面的例子中，单例bean`userManager`注入了HTTP Session作用于的bean引用`userPreferences`。这里的重点是，`userManager`是一个单例：只能被每个容器初始化一次，并且他的依赖项（例子中只有一个，就是`userPreferences`）也只能被注入一次。这意味着`userManager`bean只能操作相同的`userPreferences`duixiang (也就是说，最初注入的那个对象)。

当想要将短生命周期的bean注入到更长生命周期的bean中时，这不是用户希望的行为（例如，注入一个HTTP `Session`作用域的bean到单例bean中）。相反，用户只需要一个`userManager`对象，而且，在HTTP `Session`的生命周期内，需要一个HTTP `Session` 作用域的对象。因此，容器创建一个与`UserPreferences`类完全相同的接口（理想情况下，该对象是`UserPreferences`的实例），可以从作用域中获取实际的对象。容器向`userManager`注入一个代理对象，而后者不知道这是`UserPreferences`的一个代理。在这个例子中，当`UserManager`实例在`UserPreferences`对象上调用方法时，它实际上是在代理上调用方法。代理然后从HTTP `Session`作用域中
获取真实的`UserPreferences`对象并且将方法委托到真实的`UserPreferences`对象上。

因此，完整的配置如下：
```
<bean id="userPreferences" class="com.something.UserPreferences" scope="session">
    <aop:scoped-proxy/>
</bean>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

[stackoverflow lookup-method vs scoped proxy](https://stackoverflow.com/questions/50057371/spring-lookup-method-and-scoped-proxy-usage)

**Q:**I'm a bit confused about using method injection (lookup-method) and aop scoped-proxy (Since both used for different scoped beans injection) so

1) When to use method injection and when to use aop-scoped proxy ? 2) What is the reason why a aop-scoped proxy will not be used for a prototype bean ?

**A:**
Both lookup method injection and scoped proxy are means to inject shorter lived beans into longer lived beans. However, they serve different use cases.

Method injection is useful in cases where a singleton-scoped bean has a dependency on a prototype-scoped bean.

A proxy gets injected in place of the desired bean and provides that bean depending on the context. For example, if a singleton bean (such as a Spring MVC controller) auto-wires a session scoped bean, then the proxy delivers that bean belonging to the current HTTP session.

Such a proxy doesn't apply well to a situation where a prototype bean shall be obtained at runtime. Lookup method injection is one way to obtain prototype instances at runtime.

However, method injection has limitations because it builds upon abstract methods. Hence, certain things like writing unit tests are more cumbersome, as you need to provide a stub implementation of the abstract method. Component scanning doesn't work with abstract classes either.

One alternative to method injection is Spring's ObjectFactory, or its JSR equivalent Provider.

Another, straightforward way of creating prototype bean instances at runtime (which even makes it possible to provide constructor arguments) is to implement a bean factory like the following:
```
@Configuration
public class MyProvider {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MyThing create(String name) {
        return new MyThing(name);
    }

}
```

Usage:
```
@Component
public class MySingleton {

    @Autowired
    private MyProvider myProvider;

    public void doStuffThatNeedsAPrototypeBeanInstance() {
        MyThing thing = myProvider.create("some name");
        ...
    }
}
```

**选择创建代理的类型**

默认情况下，当Spring容器使用<aop:scoped-proxy/>元素创建代理时，将创建基于CGLIB的代理。

*CGLIB代理仅拦截public方法！不要在代理中调用非public方法。他们没有呗委派给实际的作用域目标对象。*

另一种方案是，创建基于JDK接口的代理，需要通过将`proxy-target-class`的属性设置为`false`。使用基于JDK接口的代理，意味着不需要在应用的classpath中添加附加的库。然而，这也意味着作用域bean必须至少实现一个接口并且所有与该bean的协作者必须通过接口来引用该bean。下面的例子展示了基于接口的代理：
```
<!-- DefaultUserPreferences implements the UserPreferences interface -->
<bean id="userPreferences" class="com.stuff.DefaultUserPreferences" scope="session">
    <aop:scoped-proxy proxy-target-class="false"/>
</bean>

<bean id="userManager" class="com.stuff.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

### 1.5.5 自定义作用域

bean作用域的机制是可扩展的。用户可以定义自己的作用域或者重新定义已经存在的作用域。尽管后者被认为是不好的做法，并且不能覆盖内置的`singleton`和`prototype`作用域。

**创建自定义作用域**

在Spring容器中要集成自定义作用域，需要实现`org.springframework.beans.factory.config.Scope`接口，这个接口将在本章中进行描述。对于如何实现自定义作用域，可以参考Spring框架自身的`Scope`实现和`Scope`文档，这些会更详细的解释需要实现的方法。

`Scope`接口有4个方法来从作用域中获取对象，从作用域中删除他们并且让他们销毁。

session作用域实现，例如，返回session-scoped的bean（如果它不存在，将它绑定到session来提供引用后，返回一个新的实例对象。）下面的方法从作用域返回对象:
```
Object get(String name, ObjectFactory<?> objectFactory)
```

session作用于的实现，例如，从会话中删除作用域bean。必须返回对象，如果没有找到指定名称的对象，也可以返回`null`。下面的例子展示 如何删除对象：
```
Object remove(String name)
```

下面的方法注册了一个回调方法，当他被销毁或者指定的作用域被销毁，应该调用这个方法：
```
void registerDestructionCallback(String name, Runnable destructionCallback)
```
关于销毁回调方法的细节，可以参阅Spring 作用于的实现文档。

下面的方法是用来获取作用于标识符：
```
String getConversationId()
```
对每个作用于来说，标识符都是不同的。对于一个session作用域的实现，这个标识符是session标识符。

**使用自定义作用域**

在编写和测试一个或者多个自定义`Scope`实现后，需要让Spring容器发现新定义的作用域。下面的方法展示了在Spring容器中，注册一个新的作用域：
```
void registerScope(String scopeName, Scope scope);
```

这个方法在`ConfigurableBeanFactory`接口中被声明，通过Spring中大多数ApplicationContext的具体实现，利用`BeanFactory`获得该属性。

这个方法中的第一个参数，是关联作用域的唯一名称。例如，像Spring容器中本身自带的`singleton`和`prototype`。第二个参数，是自定义作用域的`Scope`的实现。

假设已经有了自定义作用域的实现，然后接下来的例子展示了注册的步骤。

*下面的例子使用`SimpleThreadScope`，这个类包含在Spring中但是没有被默认注册。对于自定义范围的实现，方法是相同的。*

```
Scope threadScope = new SimpleThreadScope();
beanFactory.registerScope("thread", threadScope);
```

然后，创建一个具体的bean定义：
```
<bean id="..." class="..." scope="thread">
```

使用自定义范围实现，可以不仅仅局限于以编程方式注册范围。也可以使用`CustomScopeConfigurer`类以声明的方式进行注册，例如：

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="thread">
                    <bean class="org.springframework.context.support.SimpleThreadScope"/>
                </entry>
            </map>
        </property>
    </bean>

    <bean id="thing2" class="x.y.Thing2" scope="thread">
        <property name="name" value="Rick"/>
        <aop:scoped-proxy/>
    </bean>

    <bean id="thing1" class="x.y.Thing1">
        <property name="thing2" ref="thing2"/>
    </bean>

</beans>
```

当把<aop:scpoed-proxy/>放置在`FactoryBean`的实现中时，作用于是工厂bean本身，而不是`getObject()`返回的对象。

## 1.6. 自定义bean特性

Spring框架提供一些定制bean特性的接口。例如：
* 生命周期回调
* `ApplicationContextAware`和`BeanNameAware`
* 其他`Aware`接口

### 1.6.1 生命周期回调

为了和容器生命周期管理器进行交互，可以实现`InitializingBean`和`DisposableBean`接口。容器为前者调用`afterPropertiesSet()`并且为后者调用`destroy()`，让Bean在初始化和销毁时，执行某些操作。

*JSR-250规范里，`@PostConstruct`和`@PreDestroy`注解通常是Spring应用程序接受生命周期回调的最佳时间。使用这些注解意味着不需要与特定的Spring接口耦合。更详细的可参考`@PostConstruce`和`@PreDestroy`。*

*如果不想使用JSR-250注解但是仍然想要解耦，考虑使用`init-method`和`destroy-method`的bean定义元数据。*

在内部，Spring框架使用`BeanPostProcessor`的实现来处理任何它能找到并且调用的合适方法。如果需要自定义特性或者Spring默认不提供的其他生命周期的行为，可以实现一个`BeanPostProcessor`。更多细节，参考容器扩展点。

除了初始化和销毁回调，被Spring管理的对象可能实现了`Lifecycle`接口，所以这些对象可以参与启动与关闭进程，受容器自身生命周期的驱动。

生命周期回调接口在这个章节中会详细描述。

**初始化回调**
`org.springframework.beans.factory.InitializingBean` 接口在容器设置了所有必要属性后，可以执行初始化工作。`InitializingBean`接口定义了一个方法：
```
void afterPropertiesSet() throws Exception;
```

建议不要使用`InitializingBean`接口，因为这和Spring代码产生了耦合。另一个选择是，使用`@PostConstruct`注解或者定义一个POJO的初始化方法。在基于XML的元数据配置中，可以使用`init-method`属性来定义一个无参的void签名方法。使用Java配置，可以在`@Bean`中使用`initMethod`属性。思考下面的例子：
```
<bean id="exampleInitBean" class="examples.ExampleBean" init-method="init"/>
```

```
public class ExampleBean {
    public void init() {
        // do some initialization work
    }
}
```

前面的例子和下面的例子有相同的效果：
```
<bean id="exampleInitBean" class="examples.AnotherExampleBean"/>
```

```
public class AnotherExampleBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        // do some initialization work
    }
}
```

然而，第一个例子没有和Spring代码耦合。

**销毁回调**
实现`org.springframework.beans.factory.DisposableBean`接口可以让一个bean在容器销毁时回调方法。`DisposableBean`接口定义了一个方法：
```
void destroy() throws Exception;
```

建议不要使用`DisposableBean`接口，因为没有必要和Spring代码耦合。另一个可选的方案是建议使用`@PreDestroy`注解来定义一个常规方法。基于XML配置元数据，可以在<bean/>元素中使用`destroy-method`属性。对于Java配置，可以在`@Bean`中使用`destroyMethod`属性。思考下面的定义：
```
<bean id="exampleInitBean" class="examples.ExampleBean" destroy-method="cleanup"/>
```

```
public class ExampleBean {

    public void cleanup() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

前面的例子与下面的例子有相同的效果：
```
<bean id="exampleInitBean" class="examples.AnotherExampleBean"/>
```

```
public class AnotherExampleBean implements DisposableBean {

    @Override
    public void destroy() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

然而，第一个例子没有和Spring代码耦合。

*可以为`destroy-method`属性分配一个特殊的（可推断）值，该值指示Spring自动检测特定bean类上的公共关闭方法。（任何实现了`java.lang.AutoCloseable`或`java.io.Closeable`的都可以匹配）。还可以将该值应用到整个beans中。请注意，这是Java配置中的默认行为。*

**默认的初始化和销毁方法**

当不使用`InitializingBean`和`DisposableBean`接口实现初始化和销毁回调时，可以将方法名定义为`init()`，`initialize()`，`dispose()`等。理想情况下，这类生命周期回调方法的名称应该在整个项目中标准化，以便所有开发人员都使用相同的方法名称来确保一致性。

假设初始化回调方法被命名为`init()`并且销毁回调方法被命名为`destroy()`。类似于如下示例：

```
public class DefaultBlogService implements BlogService {

    private BlogDao blogDao;

    public void setBlogDao(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    // this is (unsurprisingly) the initialization callback method
    public void init() {
        if (this.blogDao == null) {
            throw new IllegalStateException("The [blogDao] property must be set.");
        }
    }
}
```

然后可以像下面的例子一样使用这个类：
```
<beans default-init-method="init">

    <bean id="blogService" class="com.something.DefaultBlogService">
        <property name="blogDao" ref="blogDao" />
    </bean>

</beans>
```

在`<beans/>`元素顶层，定义`default-init-method`属性，Spring容器会调用所有的`init`方法作为bean的初始化回调方法。当创建和组装bean时，如果bean包含有这样的方法，它就会在合适的时间被调用。

可以像定义默认初始化方法那样定义默认销毁方法，在XML中，使用`default-destroy-method`属性来实现。

如果现有的bean类已经具有回调方法并且这些命名方式与约定的不同，可以覆盖默认的方法名，通过使用`init-method`和`destroy-method`属性。

Spring容器保证提供所有依赖后立即调用已经配置的初始化回调。因此，初始化回调发生在bean的原始引用中，这意味着AOP拦截器等尚未应用于bean。首先，目标bean被完全创建，然后应用AOP代理的拦截器调用链。如果目标bean和代理是分开定义的，则代码甚至可以绕过代理与原始目标bean进行交互。因此，将拦截器应用于init方法将会导致不一致，因为这样会使目标bean的生命周期耦合到其代理或拦截器，并在代码直接与原始bean交互时，留下奇怪的语义。

**组合生命周期机制**

从Spring2.5开始，有3种方法可以控制bean生命周期：

* `InitializingBean`和`DisposableBean`回调接口
* 自定义`init()`和`destroy()`方法
* 使用注解`@PostConstruct`和`@Predestroy`。

*如果一个bean被配置了多个生命周期，并且每个生命周期都定义了不同的方法名称，那么每个配置方法将按照后面的顺序运行。然而，如果配置了相同的方法名，例如一个初始化方法`init()`-这个方法只执行一次。*

对于相同的bean配置多个生命周期，不同的初始化方法，会按照下面的顺序被调用：
* 定义在方法上的`@PostConstruct`注解
* 实现了`InitializingBean`接口的`afterPropertiesSet()`方法,
* 自定义的`init()`方法

销毁回调具有相同的顺序：
* 定义在方法上的`@PreDestroy`注解
* 实现了`DisposableBean`接口的`destroy()`方法,
* 自定义的`destroy()`方法

**启动和关闭回调**
接口`Lifecycle`定义了任何对象所需要的方法（例如启动、停止后台进程）：
```
public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();
}
```

任何被Spring管理的对象可以实现`Lifecycle`接口。然后，当`ApplicationContext`收到启动或者停止的信号：（例如，在运行时停止/重启的方案），它会关联所有在该上下文中定义了Lifecycle接口的实现。通过`LifecycleProcessor`来进行委托，例如：
```
public interface LifecycleProcessor extends Lifecycle {

    void onRefresh();

    void onClose();
}
```

注意，`LifecycleProcessor`本身是`Lifecycle`接口的扩展。它增加了两个额外的方法来对上下文的刷新或者关闭做出响应。

**注意，常规的`org.springframework.context.Lifecycle`接口是用于显示启动和停止通知的普通协议，并不意味着在上下文自动刷新时自动启动。对于特定bean的自动启动的细粒度控制（包括启动阶段），考虑使用`org.springframework.context.SmartLifecycle`来代替。 **

**另外，注意，不保证在销毁对象前发出停止通知。在常规的关闭中，所有`Lifecycle`bean首先会在传播销毁回调前，收到一个停止通知。但是，在上下文生存期内进行热刷新或者停止新刷新尝试，仅仅会调用destroy方法。**

启动和关闭的调用顺序是非常重要的。如果任何两个对象存在`depends-on`关系，依赖方在被依赖方后开始，并且，在被依赖方之前停止。但是，有时候直接依赖是未知项。用户可能仅仅知道某种类型的对象应该优先于另一种类型的对象。在这些例子中，`SmartLifecycle`接口的定义是另一种选择，换句话说，在它的super-interface 上定义了`getPhase()`方法，`Phased`。下面的例子展示了`Phased`接口的定义：
```
public interface Phased {

    int getPhase();
}
```

下面的定义展示了`SmartLifecycle`接口：
```
public interface SmartLifecycle extends Lifecycle, Phased {

    boolean isAutoStartup();

    void stop(Runnable callback);
}
```

当启动时，拥有更低阶段的对象首先启动。当停止时，这个顺序会反过来。因此，一个对象实现了`SmartLifecycle`接口并且`getPhase()`方法返回`Integer.MIN_VALUE`，会导致它首先启动并且最后关闭。另一方面，如果一个阶段的值定义为`Integer.MAX_VALUE`，这意味着这个对象会最后启动，并且最先停止（可能是因为它依赖于其他正在运行的进程）。这个值对那些没有实现`SmartLifecycle`接口的普通`Lifecycle`对象来说也同样重要，它的默认值是0。

定义在`SmartLifecycle`中的stop方法接受一个回调。任何实现了这个接口的对象必须在关闭进程后调用`run()`方法。在必要时，启用异步关闭，因为`LifecycleProcessor`接口的默认实现`DefaultLifecycleProcessor`会等待每个阶段内对象组的超时值以调用该回调。默认每个阶段的超时时间是30秒。可以覆盖默认的超时时间。如果只想修改超时时间，下面的定义会满足这种情况：
```
<bean id="lifecycleProcessor" class="org.springframework.context.support.DefaultLifecycleProcessor">
    <!-- timeout value in milliseconds -->
    <property name="timeoutPerShutdownPhase" value="10000"/>
</bean>
```

如前面所提到的，`LifecycleProcessor`接口对刷新和关闭上下文定义了回调方法。后者驱动关闭过程，就好像已经明确调用了`stop()`方法，但是它发生在上下文正在关闭时。另一方面，refresh回调启用了`SmartLifecycle`的另一个功能。当上下文被刷新 (在所有对象被初始化后)，回调方法被调用。在那之后，默认的生命周期处理器通过每个`SmartLifecycle`对象的`isAutoStartup()`方法来检查返回的boolean值。如果是`true`,那么对象从那时开始启动，而不是等待上下文或它自己的`start()`方法显示调用（与上下文刷新不同，对于标准的上下文实现，上下文启动不会自动发生）。`phase`的值和任何`depends-on`关系决定了启动顺序，如前面的描述一样。

**在非web应用中优雅关闭Spring的IoC容器**

*本章仅用于非web应用程序。Spring的web上下文 `ApplicationContext`的实现已经包含优雅关闭容器的功能。*

如果使用一个非web应用环境的Spring IoC 容器（例如，在富客户端客户端环境中），请向JVM注册一个关闭钩子。这样做保证了优雅关闭并且调用单例bean上的相关销毁方法，所有的资源将被释放。用户必须正确配置和实现这些销毁回调。

为了注册一个关闭钩子，调用 在`ConfigurableApplicationContext`接口中的`registerShutdownHoot()`方法，如下面的例子：
```
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

        // add a shutdown hook for the above context...
        ctx.registerShutdownHook();

        // app runs here...

        // main method exits, hook is called prior to the app shutting down...
    }
}
```

### 1.6.2 `ApplicationContextAware`和`BeanNameAware`

当使用`ApplicationContext`创建一个实现了`org.springframework.context.ApplicationContextAware` 接口的对象实例时，这个实例提供一个`ApplicationContext`的引用。下面展示了接口 `ApplicationContextAware` 的详细定义：
```
public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
```

因此，bean可使用编程的方式操纵`ApplicationContext`，通过`ApplicationContext`或者已知的子类引用（例如：`ConfigurableApplicationContext`暴露的附加功能）。一种用途是通过编程的方式检索其他bean。有时，这种能力非常有用。然而，大多数情况下，应该避免使用这种方式，因为它和Spring代码耦合在了一起并且违反了控制反转原则。`ApplicationContext`的其他方法提供对资源文件的访问、发布应用程序事件以及访问`MessageSource`。这些附加的特性在 `Additional Capabilities of the ApplicationContext`中被详细描述。

自动装配是获得`ApplicationContext`引用的另一种选择。传统的`constructor`和`byType`的自动装配方式可以提供构造参数或setter方法参数的`ApplicationContext`类型依赖。要获得更大的灵活性，包括能够自动装配字段和多个参数方法，请使用基于注解的自动装配功能。如果这样做，`ApplicationContext`被装配为一个字段，构造函数参数或者方法参数。更多信息，请参阅`@Autowired`。

当`ApplicationContext`创建一个实现了`org.springframework.beans.factory.BeanNameAware`接口的类时，这个类提供了对其关联对象中定义的名称的引用。下面列出了BeanNameAware接口的定义：
```
public interface BeanNameAware {

    void setBeanName(String name) throws BeansException;
}
```

这个方法将在填充了bean属性后，初始化回调之前被调用（例如：`InitializingBean`，`afterPropertiesSet`。）

### 1.6.3. 其他感知(`Aware`)接口

除了`ApplicationContextAware`和`BeanNameAware`（之前讨论的），Spring提供了一个范围更广的`Aware`接口，可以让beans向容器指示他们需要某种基础结构依赖。最为基本规则，名称表示依赖项类型。下面的表格总结了大多数重要的`Aware`接口：

| Name                           | Injected Dependency                                          | Explained in...                                              |
| ------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ApplicationContextAware        | Declaring `ApplicationContext`.                              | [`ApplicationContextAware` and `BeanNameAware`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-aware) |
| ApplicationEventPublisherAware | Event publisher of the enclosing `ApplicationContext`.       | [Additional Capabilities of the `ApplicationContext`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-introduction) |
| `BeanClassLoaderAware`         | Class loader used to load the bean classes.                  | [Instantiating Beans](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-class) |
| `BeanFactoryAware`             | Declaring `BeanFactory`.                                     | [`ApplicationContextAware` and `BeanNameAware`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-aware) |
| BeanNameAware                  | Name of the declaring bean.                                  | [`ApplicationContextAware` and `BeanNameAware`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-factory-aware) |
| LoadTimeWeaverAware            | Defined weaver for processing class definition at load time. | [Load-time Weaving with AspectJ in the Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-aj-ltw) |
| MessageSourceAware             | Configured strategy for resolving messages (with support for parametrization and internationalization). | [Additional Capabilities of the `ApplicationContext`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-introduction) |
| NotificationPublisherAware     | Spring JMX notification publisher.                           | [Notifications](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#jmx-notifications) |
| ResourceLoaderAware            | Configured loader for low-level access to resources.         | [Resources](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#resources) |
| ServletConfigAware             | Current `ServletConfig` the container runs in. Valid only in a web-aware Spring `ApplicationContext`. | [Spring MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc) |
| ServletContextAware            | Current `ServletContext` the container runs in. Valid only in a web-aware Spring `ApplicationContext`. | [Spring MVC](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc) |



再次注意，使用这些接口将会是代码与Spring API绑定在一起，并且没有遵循控制反转的机制。因此，建议将他们用于需要以编程方式访问容器的基础结构bean。

## 1.7 Bean Definition 继承

一个bean definition 包含了大量的配置信息，包括构造参数，属性值和特定容器信息，例如，初始化方法、静态工厂方法名等等。子bean definition可以继承来自父bean definition的配置信息数据。也可以覆盖一些值或者增加其他需要信息。使用父子配置可以节省大量的输入。实际上，这是一种模板的形式。

如果用户通过一个`ApplicationContext`接口编程，class bean definitions使用`ChildBeanDefinition`类表示。大部分用户不需要使用他们。相反，他们在诸如`ClassPathXmlApplication`的类中生命bean definition。当使用XML配置时，可以指定child bean definition使用`parent`属性，指定父bean作为此属性的值。例如：

```
<bean id="inheritedTestBean" abstract="true"
        class="org.springframework.beans.TestBean">
    <property name="name" value="parent"/>
    <property name="age" value="1"/>
</bean>

<bean id="inheritsWithDifferentClass"
        class="org.springframework.beans.DerivedTestBean"
        parent="inheritedTestBean" init-method="initialize">  
    <property name="name" value="override"/>
    <!-- the age property value of 1 will be inherited from parent -->
</bean>
```

注意`parent`属性

如果未指定child bean definition，则使用parent bean definition，但也可以覆盖它。后一种情况，子类bean必须与父类bean兼容（也就说是，它必须接收父类的属性值）。

一个child bean definition继承了范围、构造参数值，属性值和覆盖的父类方法。并且可以选择添加新的值。任何作用域，初始化方法，销毁方法或者静态工厂方法通可以覆盖父类的设置。

其余设置是从child definitionzhongoing获取：depends on,autowire mode,dependency check,singleton 和lazy init。

前面的例子明确的使用`abstract`属性来标注parent bean definition为抽象类。如果parent definition 未指定类，需要明确的标记为`abstract`。

parent bean不能被实例化，因为它没有被实现，并且它被明确指定为`abstract`。当一个bean definition是`abstract`时，通常用于纯模板定义。尝试使用标记为`abstract`的父定义，通过另一个bean的属性来引用它或者明确调用`getBean()`方法豆浆导致错误。类似的，容器的内部`preInstantiateSingletons()`方法忽略被定义为abstract的bean definitions。

*`ApplicationContext` 默认对所有单例预先初始化。因此，如果有一个bean 定义(父bean定义)打算只用来当模板使用，它非常重要（至少对单例bean来说）。并且此定义定义了一个类，必须确保这个类的abstract属性设置为true，否则，应用程序上下文将实际（视图）预先实例化抽象bean。*

## 1.8  容器扩展点

通常，应用开发者不需要实现`ApplicationContext`。Spring IoC容器可以通过插入集成接口来实现扩展Spring IoC容器。下面的部分描述了这些集成接口。

### 1.8.1. 通过`BeanPostProcessor`自定义bean

`BeanPostProcessor`接口定义了一个回调方法，用户可以自己实现（或者覆盖容器默认的）初始化逻辑，依赖解析逻辑等等。如果想要在Spring容器完成实例化、配置、初始化后自定义一些逻辑，可以插入一个或多个自定义的`BeanPostProcessor`。

可以配置多个`BeanPostProcessor`实例，并且可以按顺序控制这些实例的运行顺序。如果`BeanPostProcessor`的实现实现了`Order`接口，可以设置这个属性。如果编写自己的`BeanPostProcessor`，也需要考虑实现`Ordered` 接口。更详细的功能，可以参阅`BeanPostProcessor` and `Ordered`接口文档。

`org.springframework.beans.factory.config.BeanPostProcessor`接口有两个回调方法组成。当一个类作为post-processor注入到容器中时，对于每个容器创建的bean实例来说，这个post-processor的回调会在容器初始化方法（例如，`InitializingBean.afterPropertiesSet()`）之前或者任何声明为`init`方法之前被调用。post-processor可以对bean实例进行任何操作，包括完全忽略回调。一个bean post-processor通常会检查回调接口，或者通过代理包装一个bean。一些Spring AOP基础设施类被实现为post-processor，以提供代理包装逻辑。

一个`ApplicationContext`会自动侦测到任何实现了`BeanPostProcessor`接口的bean定义。`ApplicationContext`把这些bean作为post-processor来注册，以便在创建bean之前调用。Bean post-processor与其他任何bean一样可以部署在容器中。

注意，当在一个配置类中，使用`@Bean`工厂方法声明一个`BeanPostProcessor`时，工厂方法的返回类型至少是实现类本身或者至少是 `org.springframework.beans.factory.config.BeanPostProcessor`，这清晰的表名了bean的 post-processor 特性。否则，`ApplicationContext`无法在完全创建之前，按类型自动检测到它。因为一个`BeanPostProcessor`需要及早的实例化才能应用于上下文中其他bean的初始化，这种早期类型检测至关重要。

*编程方式注册BeanPostProcessor实例*
*推荐`BeanPostProcessor`注册方法是通过`ApplicationContext`自动检测（如前所示），用户针对`ConfigurableBeanFactory`可以使用`addBeanPostProcessor`方法编程来实现。当需要在注册事前评估条件逻辑，甚至需要跨层次结构的上下文复制bean post processor，这将非常有用。但是请注意，以编程方式添加的`BeanPostProcessor`实例不遵循`Ordered`接口。在这里，注册的顺序决定了执行的顺序。还要注意，以编程方式注册实例始终会在自动检测注册的实例之前进行处理，而不考虑明确的顺序。*

*BeanPostProcessor实例和AOP自动代理*

*实现了`BeanPostProcessor`接口的类，会被容器特殊对待。所有实例和直接引用的bean在启动时初始化，作为`ApplicationContext`启动步骤的一部分。接下来，以排序方式注册所有的`BeanPostProcessor`实例，并将其应用于容器中的其他bean。因为AOP自动代理是作为`BeanPostProcessor`本身实现的，`BeanPostProcessor`实例和其直接引用的bean不具备自动代理资格，因此，不要将这些交织在一起。*

*对于任何这样的bean，应该会看到一条参考日志消息：Bean someBean is not eligible for getting processed by all BeanPostProcessor interfaces (for example: not eligible for auto-proxying).*

*如果使用autowiring或者`@Resource`注入`BeanPostProcessor`（自动装配可能被退回），按类型搜索依赖项候选时，Spring可能会访问unexcepted beans，因此，需要让他们不要成为自动代理或其他类型的post-processing。如果有一个@Resource注释的依赖项，其中字段或者setter的名称不直接对应bean声明的名称，并且不适用name属性，Spring访问其他bean，按类型匹配他们。*

下面展示了怎么编写、注册、使用`BeanPostProcessor`实例。

**Hello Word，BeanPostProcessor-style**

第一个例子说明了基本用法。例子展示了自定义`BeanPostProcessor`实现，容器创建每个bean后，调用`toString()`方法，打印字符串结果到系统控制台。

下面是`BeanPostProcessor`实现定义：

```
package scripting;

import org.springframework.beans.factory.config.BeanPostProcessor;

public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {

    // simply return the instantiated bean as-is
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean; // we could potentially return any object reference here...
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("Bean '" + beanName + "' created : " + bean.toString());
        return bean;
    }
}
```

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:lang="http://www.springframework.org/schema/lang"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/lang
        https://www.springframework.org/schema/lang/spring-lang.xsd">

    <lang:groovy id="messenger"
            script-source="classpath:org/springframework/scripting/groovy/Messenger.groovy">
        <lang:property name="message" value="Fiona Apple Is Just So Dreamy."/>
    </lang:groovy>

    <!--
    when the above bean (messenger) is instantiated, this custom
    BeanPostProcessor implementation will output the fact to the system console
    -->
    <bean class="scripting.InstantiationTracingBeanPostProcessor"/>

</beans>
```

注意如何定义`InstantiationTracingBeanPostProcessor`。它甚至没有bean名称，虽然它是一个bean，它可以注入到其他任何bean中。（前面的配置也使用Groovy script定义了一个bean。Spring动态语言提供的支持。）

下面通过Java代码运行上面的例子：
```
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scripting.Messenger;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("scripting/beans.xml");
        Messenger messenger = ctx.getBean("messenger", Messenger.class);
        System.out.println(messenger);
    }

}
```

输出如下：
```
Bean 'messenger' created : org.springframework.scripting.groovy.GroovyMessenger@272961
org.springframework.scripting.groovy.GroovyMessenger@272961
```

**`RequiredAnnotationBeanPostProcessor`**

将回调接口或注解与自定义`BeanPostProcessor`实现结合使用，是扩展Spring IoC容器的常用方法。一个Spring的例子：`RequiredAnnotationBeanPostProcessor`实现了`BeanPostProcessor`，该实现可确保bean上标有任意注解并且确实依赖注入了一个值。

### 1.8.2 通过`BeanFactoryPostProcessor`自定义配置元数据

接下来的扩展点是`org.springframework.beans.factory.config.BeanFactoryPostProcessor`。这个接口的语义与`BeanPostProcessor`相似。主要的一个不同点是：`BeanFactoryPostProcessor`操纵的是bean配置元数据。也就是说，Spring的IoC容器让`BeanFactoryPostProcessor`读取配置元数据，并且在容器实例化除了`BeanFactoryPostProcessor`实例以外的任何bean之前，改变它。

可以配置多个`BeanFactoryPostProcessor`实例，并且，可以控制通过设置`order`属性来控制他们的运行顺序。但是，只能设置实现了`Ordered`接口的`BeanFactoryPostProcessor`实现的属性。如果编写自己的`BeanFactoryPostProcessor`，应该也考虑实现`Ordered`接口。更多细节参考`BeanFactoryPostProcessor`和`Ordered`接口的文档。

*如果想要改变实际的bean实例（也就是说，这个对象是通过配置元数据创建的），然后需要使用一个`BeanPostProcessor`。从技术上讲，可以在`BeanFactoryostProcessor`中使用bean实例（例如，使用`BeanFactory.getBean()`），这样做，会导致bean过早实例化，违反了标准的容器生命周期。这可能会有负面影响，例如绕过bean post processing。*

*另外，`BeanFactoryPostProcessor`实例也是容器级别的。这仅和使用容器层次结构相关。如果一个在一个容器中定义一个`BeanFactoryProcessor`，它仅适用于这个容器。一个容器中的bean定义不会由另一个容器中的`BeanFactoryPostProcessor`实例进行处理，即使这两个容器具有相同的层级结构。*

当bean factory post-processor在`ApplicationContext`中声明时，为了更改定义在容器中的配置元数据，它会自动运行。Spring包含了一系列的预先定义好的bean factory post-processors，例如：`PropertyOverrideConfigurer`和`PropertySourcesPlaceholderConfigurer`。可以使用自定义的`BeanFactoryPostProcessor`-例如：注册自定义属性编辑器。

`ApplicationContext`自动检测任何部署在内的实现了`BeanFactoryPostProcessor`接口的bean。在适当的时候，使用这些bean factory post-processors。可以像部署其他任何bean一样部署这些bean。

*与`BeanPostProcessor`一样，通常不想为`BeanFactoryPostProcessor`配置延迟初始化。如果没有别的bean引用一个Bean(Factory)PostProcessor，那么它将会被更早的实例化，即使在<beans/>元素上，将`default-lazy-init`属性设置为`true`。*

**例子：Class Name Substitution `PropertySourcesPlaceholderConfigurer`**

在分开的使用Java`Properties`格式的文件上，`PropertySourcesPlaceholderConfigurer`可以从bean定义中扩展属性值。这样做，可以使部署应用程序的人员可以自定义环境属性，例如数据库URLs和密码，没有修改容器一个或多个主XML定义文件的复杂性和风险性。

思考下面的XML配置元数据片段，`DataSource`使用占位符来定义值：

```
<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <property name="locations" value="classpath:com/something/jdbc.properties"/>
</bean>

<bean id="dataSource" destroy-method="close"
        class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>
```

这个例子展示了一个外部`Properties`文件属性配置。在运行时，一个`PropertySourcesPlaceholderConfigurer`被应用于替换数据库属性上。这些值以`{property-name}`的形式作为特定的占位符，这遵循了Ant、log4j、JSP EL的样式。

实际的值来自于一个标准的Java`Properties`格式文件：
```
jdbc.driverClassName=org.hsqldb.jdbcDriver
jdbc.url=jdbc:hsqldb:hsql://production:9002
jdbc.username=sa
jdbc.password=root
```

因此，`${jdbc.username}`这个字符串在运行时将被替换为`sa`，在配置文件中，其他匹配到的key也会被替换。`PropertySourcesPlaceholderConfigurer`检查bean definition中的大多数属性和属性中的占位符。此外，可以自定义占位符的前缀和后缀。

在Spring2.5后引入了`context`命名空间，可以使用专用配置元素配置属性占位符。可以在location属性中，提供一个或多个逗号分隔的列表，如下：

```
<context:property-placeholder location="classpath:com/something/jdbc.properties"/>
```

`PropertySourcesPlaceholderConfigurer`不仅在指定的`Properties`文件中查找属性。默认情况下，如果在属性文件中没有找到指定的属性，它会检查Spring Environment属性和常规的Java System属性。

*当必须在运行时选择特定的实现类时，可以使用`PropertySourcesPlaceholderConfigurer`来代替类名。下面的例子展示了这种用法：*
```
<bean class="org.springframework.beans.factory.config.PropertySourcesPlaceholderConfigurer">
    <property name="locations">
        <value>classpath:com/something/strategy.properties</value>
    </property>
    <property name="properties">
        <value>custom.strategy.class=com.something.DefaultStrategy</value>
    </property>
</bean>

<bean id="serviceStrategy" class="${custom.strategy.class}"/>

如果这个类在运行时不能被解析为有效的类，那么在创建bean时解析会失败，这是针对非延迟初始化bean的`ApplicationContext`的`preInstantiateSingletongs`阶段。
```

**`PropertyOverrideConfigurer`**

`PropertyOverrideConfigurer`，另一个bean factory post-processor，像`PropertySourcesPlaceholderConfigurer`一样，但是与之不同的是，原始的定义可以具有默认值，也可以完全没有bean 属性值。如果覆盖的属性文件没有确定的bean属性条目，则默认使用上下文定义。

注意，bean definition不会感知到被覆盖，所以，不会从XML定义里不能立即明显看出正在使用overring configurer。如果有多个`PropertySourcesPlaceholderConfigurer`实例为同一个bean属性定义了不同的值，则由于覆盖机制，最后一个实例讲会被认为是有效的。

属性文件配置行采用以下格式：
```
beanName.property=value
```

下面的清单显示了格式的示例：
```
dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql:mydb
```

这个实例文件可以被包含一个叫做`dataSource`的bean使用，他有`driver`和`url`属性。

只要路径的每个组成部分（最终属性除外）都已经非空（可能是由构造器初始化），也支持复合属性名。下面的例子，`sammy`被设置为`123`：
```
tom.fred.bob.sammy=123
```

*被指定覆盖的值总是字面值。他们不会被翻译为bean引用。当XML bean定义中的原始值指定bean引用时，这个约定也适用。*

在Spring2.5以后引入的`context`命名空间，可以使用专用配置元素配置属性覆盖，如下面的例子：
```
<context:property-override location="classpath:override.properties"/>
```

### 1.8.3 通过`FactoryBean`自定义实例化逻辑

用户可以为本身就是工厂的对象，实现`org.springframework.beans.factory.FactoryBean`接口。

`FactoryBean`接口是Spring IoC容器中的一个重要的实例化逻辑插件。如果有复杂的实例化代码，那么最好用Java表达而不要用冗长的XML配置。可以创建专属的`FactoryBean`，在类中编写复杂的实例化代码，把这个自定义的`FactoryBean`附加在容器中。

`FactoryBean`接口提供3个方法：
* `Object getObject()`：返回这个工厂创建的实例。这个实例可以是共享的，取决于这个工厂返回的是单例还是原型。
* `boolean isSingleton()`：如果返回的是`true`则表示单例，如果是`false`，结果就相反。
* `Class getObjectType()`：返回`getObject()`方法返回的对象类型，或者如果类型未知，就返回`null`。

Spring框架中使用了大量的`FactoryBean`概念和接口。Spring自带了超过50个`FactoryBean`的实现。

当需要向容器询问一个实际的`FactoryBean`实例本身而不是它产生的bean时，在调用`ApplicationContext`的`getBean()`方法时，在bean的ID前面加上`&`符号。所以，对于一个已知id为`myBean`的bean，在容器中调用`getBean("myBean")`会返回`FactoryBean`产生的bean，而调用`getBean("&myBean")`则会返回`FactoryBean`实例本身。

## 1.9. 基于注解的容器配置

```
注解比XML更好吗？

基于注解的配置引入了一个问题，就是这种方法是否比XML更好。简短的答案是视情况而定。长答案是每个方法都有优点和缺点，通常，这取决于开发者决定哪种策略更适合他们。由于定义方式不同，注解在声明中提供了大量的上下文，从而使配置更简短更简洁。然而，XML擅长连接组件而不接触其源代码或重新编译他们。一些开发者更喜欢讲装配靠近源码，另一些开发者认为带注解的类不再是POJO，此外，配置变得分散而难以控制。

无论哪种选择，Spring包容两种风格甚至可以混合使用他们。值得指出的是，通过其JavaConfig选项，Spring允许以非入侵的方式使用注解，而不接触目标组件的源码，在工具方面，Spring Toos for Eclipse支持所有配置样式。
```

基于注解的配置提供了替代XML配置方法，依靠字节码元数据装配组件，而不是尖括号声明。替换使用XML描述bean转配，开发者将配置移动到组件类本身，在相关类、方法、或字段上声明注解。之前提到了一个例子：`RequiredAnnotationBeanPostProcessor`，结合使用`BeanPostProcessor`和注解是扩展Spring IoC容器的常用方法。例如，Spring2.0引入的`@Required`注解强制执行必要属性。Spring2.5使得可以遵循相同的通用方法来驱动Spring的依赖注入。本质上，`@Autowiring`注解提供了相同的自动装配协作者的能力，但是具有更细粒度的控制和更广泛的适用性。Spring2.5也支持新加入的JSR-250注解，例如`@PostConstruct`和`@PreDestory`。Spring3.0加入了支持JSR-330(Java本身的依赖注入)的注解，在`javax.inject`包中，例如`@Inject`和`@Named`。更多详细的注解可以在相关章节中找到。

*注解注入在XML注入之前执行。因此，XML配置会覆盖注解配置。*

与往常一样，可以将他们注册为单个bean，但是，也可以通过在XML中标记来隐式注册（注意包含的`context`命名空间）:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

</beans>
```

(隐式注册的post-processor包括`AutowiredAnnotationBeanPostProcessor`, `CommonAnnotationBeanPostProcessor`, `PersistenceAnnotationBeanPostProcessor`, 和前面提到的 `RequiredAnnotationBeanPostProcessor`)

*<context:annotation-config/>只查找相同应用上下文中定义的bean注解。这就意味着，如果把<context:annotation-config/>放到一个`WebApplicationContext`中以获取`DispatcherServlet`，它只能对Controller中的`@Autowiring`bean，而不是services。更多`DispatcherServlet`请参阅相关文档。*

### 1.9.1 @Required

`@Required`注解应用于bean属性的setter方法，下面的例子是：

```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Required
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

这个注解指示必须在配置时通过bean定义中的显示属性值或通过自动装配来填充受影响的属性。如果未填充受影响的bean属性，则容器将引发异常。这允许尽早的失败，来避免以后出现`NullPointerException`实例。我们仍然建议将断言放入bean类本身中（例如，放入init方法中）。这样做会强制执行那些必须的引用和值，即使在容器外部使用该类也是如此。

*@Required注解在Spring5.1后正式被废除，使用构造函数注入必须的设置（或与bean属性设置其方法一起使用的`InitializingBean.aftrProtertiesSet()`的自定义实现）*

#### 1.9.2. 使用`@Autowired`

*JSR 330中的`@Inject`注解可以用来替换Spring中的`@Autowired`注解。*

可以将`@Autowired`注解放置在构造器上：
```
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

*在Spring4.3以后，如果目标bean仅定义一个以其开头的构造函数，则不再需要在此类构造函数上使用`@Autowired`注解。但是，如果有多个可用的构造函数并且没有primary/default构造函数，则至少需要一个被标记为`@Autowired`的构造函数，为了指示容器使用哪个。更多细节参考构造函数解析*

可以将注解应用到传统的setter方法上：
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

还可以将注解应用于具有任意名称和多个参数的方法：

```
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

甚至可以将`@Autowired`混合使用在构造器和字段上：
```
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    private MovieCatalog movieCatalog;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

确保目标组件（例如，`MovieCatalog`或`CustomerPreferenceDao`）拥有一致的类型声明是注入的关键。否则，注入可能由于运行时找不到类型匹配而失败。

对于通过类路径找到XML定义的bean或组件类，容器通常预先知道他们的类型。然而，对于`@Bean`工厂方法，需要确保声明返回的类型满足表达式。对于实现了多个接口的组件或者对于其实现类可能引用的组件，考虑在工厂方法中声明最具体的返回类型（至少与引用bean的注入点所要求的一样）。

还可以指示Spring通过将`Autowired`注解添加到该类型数组的字段或者方法中，从而为`ApplicationContext`提供特定类型的所有bean：
```
public class MovieRecommender {

    @Autowired
    private MovieCatalog[] movieCatalogs;

    // ...
}
```

同样也适用于集合框架：
```
public class MovieRecommender {

    private Set<MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Set<MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```

*如果想要让数组或者列表条目按特定顺序定义，那么目标bean可以实现接口`org.springframework.core.Ordered或使用`@Order`或`@Priority`注解`。否则，他们的顺序将遵循容器中相应目标bean定义的注册顺序。*

*可以在目标类级别上和`@Bean`方法上声明`@Order`注解。`@Order`值可能会影响注入点的优先级，但是注意，他们不会影响单例启动顺序，这是由依赖关系和`@DepandsOn`声明确定的。*

*注意，在`@Bean`级别上，标准的`javax.annotation.Priority`是不可用的，因为他不能在方法上声明。它的语义可以通过`@Order`值与`@Primary`结合在美中单例bean上进行建模。*

只要期望的key的类型是`String`，即使是类型化的`Map`实例也可以自动装配。这个Map包含了所有希望的bean类型，并且key包含的bean的名称，如下所示：
```
public class MovieRecommender {
    private Map<String, MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Map<String, MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```

默认情况下，当没有匹配到可用的候选bean时，自动装配将会失败。对于声明的数组、集合或映射，至少应有一个匹配元素。

对于注解方法和字段，默认的依赖是必须的。可以像下面的例子一样更改这种行为，通过将框架标记为非必须的注入点(即通过将`@Autowired`中的`required`属性设置为`false`)，使框架可以跳过不满足要求的注入点：

```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired(required = false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

如果不需要的方法不可用（或在多个参数的情况下，其中一个依赖项）不可用，则根本不会调用这个方法。在这种情况下，完全不需要填充非必须字段，而将其默认值保留在适当的位置。

注入构造函数和工厂方法的参数是特殊的例子，由于Spring构造函数解析算法可能会处理多个构造函数，因此`@Autowired`中的`required`属性的含义有些不同。默认情况下，构造函数和工厂方法参数的reqired是有效的，但是在单构造函数场景中有一些特殊规则，例如，如果没有可匹配的bean，则多元素注入点(数组，集合、映射)解析为空实例。这允许一种通用的实现模式，其中所有依赖项都可以在唯一的多参数构造函数中声明，例如，声明为不带`@Autowired`注解的单个公共构造函数。

*任何给定bean类只有一个构造函数能声明为`@Autowired`并且属性`reqiured`属性为`true`，这指示了Spring在自动装配bean时的构造函数。因此，如果`required`属性保留为默认值`true`，则`@Autowired`只能注释单个构造函数。如果多个构造函数声明注解，为了考虑自动装配的候选者，需要声明`required=false`（在XML中，类似的声明为`autowired=construct`）。将选择通过匹配Spring容器中的bean可以满足的依赖项数量最多的构造函数。如果没有一个候选者满意，则将使用主/默认构造函数（如果存在）。同样，如果一个类声明了多个构造函数，但都没有使用@Autowired进行注释，则将使用主/默认构造函数（如果存在）。如果一个类仅声明一个单一的构造函数开始，即使没有注释，也将始终使用它。请注意，带注释的构造函数不必是公共的。*

*建议在setter方法上使用@Autowired的required属性，而不推荐使用已弃用的@Required批注。将required属性设置为false表示该属性对于自动装配不是必需的，并且如果无法自动装配，则将忽略该属性。另一方面，@Required更为强大，因为它可以通过容器支持的任何方式强制设置属性，并且如果未定义任何值，则会引发相应的异常。*

另外，您可以通过Java 8的`java.util.Optional`表示特定依赖项的非必需性质，如以下示例所示:

```
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {
        ...
    }
}
```

Spring5.0以后，可以使用`@Nullable`注解(在任何包中为任何形式，例如JSR-305中的`javax.annotation.Nullable`)或仅利用Kotlin内置的null安全支持：

```
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {
        ...
    }
}
```

还可以将`@Autowired`用于接口：
`BeanFactory`, `ApplicationContext`, `Environment`, `ResourceLoader`,`ApplicationEventPublisher`,`MessageSource`。这些接口和他们的扩展接口，例如：`ConfigurableApplicationContext`或者`ResourcePatternResolver`会自动被解析，不需要特别的设置。下面的例子自动装配了`ApplicationContext`对象：
```
public class MovieRecommender {

    @Autowired
    private ApplicationContext context;

    public MovieRecommender() {
    }

    // ...
}
```

*`@Autowired`,`@Inject`，`@Value`和`@Resource`注解由`BeanPostProcessor`操纵实现。这意味着不能把这些注解应用到任何`BeanPostProcessor`或`BeanFactoryPostProcessor`类型上。这些类型必须通过XML或者`@Bean`方法装配。*

### 1.9.3 通过微调的自动装配`@Primary`

由于通过类型来自动装配会导致多个候选者，通常需要更多地控制选择流程。一种途径是使用Spring的`@Primary`注解。当有多个候选的需要备注的bean时，`@Primary`指明了所需要的特定引用。如果候选中恰好存在一个主要的bean，则它将成为自动装配的值。

思考下面被定义为`firstMovieCatalog`的`MovieCatalog`：

```
@Configuration
public class MovieConfiguration {

    @Bean
    @Primary
    public MovieCatalog firstMovieCatalog() { ... }

    @Bean
    public MovieCatalog secondMovieCatalog() { ... }

    // ...
}
```

下面的`MovieRecommender`将自动装配`firstMovieCatalog`：
```
public class MovieRecommender {

    @Autowired
    private MovieCatalog movieCatalog;

    // ...
}
```

在XML中，相应的bean定义如下：
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog" primary="true">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

### 1.9.3 通过微调的自动装配`@Qualifier`

当一个主要的候选者被决定时，`@Primary`对于多个需要通过类型来装配的实例来说是一种有效的途径。当需要更多的控制选择流程时，可以使用`@Qualifire`注解。可以使用特定的参数来分配限定值，缩小匹配类型的范围以便为每个参数选择指定的bean。在最简单的情况下，这可以是简单的描述性的值：
```
public class MovieRecommender {

    @Autowired
    @Qualifier("main")
    private MovieCatalog movieCatalog;

    // ...
}
```

也可以将`@Qualifier`注解放置在单独的构造函数或方法参数上，像下面的例子:
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(@Qualifier("main") MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}

XML的相同配置的例子：
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="main"/> 1

        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="action"/> 2

        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

对于候选的匹配，bean名称被视为默认的限定值。因此，可以将bean的id定义为main来代替嵌套的qualifier元素，从而得到相同的匹配结果。但是，尽管可以使用此约定名称引用特定的bean，`@Autowired`基本上是关于带有可选语义限定符的类型驱动的注入。这意味着，即使带有bean名称后退的限定值，在类型匹配中也始终具有更小的语义。
他们没有在语义上表示对唯一bean id的引用。好的限定值是`main`或`EMEA`或`persistent`,表示独立于bean id的特定组件的特征，如果是匿名bean定义（例如前面示例中的定义），则可以自动生成该属性。

限定符同样也可以应用到集合框架，像之前讨论的-例如，对于`Set<MovieCatalog>`。在这个例子中，所有匹配的bean，通过声明限定符，作为集合被注入。这表名限定符不需要是唯一的。相反，他们构成了过滤条件。例如，可以定义多个`MovieCatalog`bean拥有相同的限定值`action`，所有这些都注入到以`@Qualifier("action")`注释的`Set<MovieCatalog>`中。

*在类型匹配的候选对象中，让限定符值根据目标bean名称进行选择。如果没有其他的解析指示器（例如限定符标记或者primary标记），对于一个非唯一依赖的情景，Spring将注入点名称（即字段名称或参数名称）与目标bean名称进行匹配，然后选择同名候选人（如果有）。*

也就是说，如果打算按照名称表示注解驱动的注入，不要使用`@Autowired`，即使能够在类型匹配的候选对象中按bean名称进行选择。而是使用JSR-250的`@Resource`注解，这个注解的语义是通过其唯一名称来标识特定目标组件，而声明的类型与匹配过程无关。`@Autowired`具有非常不同的语义：在通过类型选择候选bean后，指定的String限定值仅在哪些类型选择的候选中被考虑（例如，将`account`限定值与相同限定符标签的bean进行匹配）。

对于那些被定义为即可的bean，`Map`或者数组类型，`@Resource`是一个好的解决方案，它通过唯一的名称引用特定的集合或数组。这就是说，在4.3以后，还可以通过`@Autowired`类型匹配算法来匹配`Map`和数组类型，只要元素类型信息保留在`@Bean`返回类型签名或集合继承层次结构中。在这个例子中，可以使用限定值来选择相同类型的集合框架，像前面章节列出的一样。

在4.3以后，`@Autowired`也考虑自我引用进行注入（也就是说，引用回当前注入的bean）。注意，自我注入是一个后备。对其他组件的常规依赖始终优先。从这个意义上来说，自我引用不参与常规候选人选择，因此尤其不是主要的。反过来，他们总是以最低优先级结束。实际上，应该仅将自我引用作为最后的手段（例如，通过bean的事务代理，在同一实例上调用其他方法）。在这种情况下，请考虑将受影响的方法分解为单独的委托bean。 或者，您可以使用@Resource，它可以通过其唯一名称获取返回到当前bean的代理。

*尝试在相同的配置类中，将`@Bean`方法返回的结果注入，实际上是一种自引用方案。要么在实际需要的方法签名中延迟解析此类引用（与配置类中的自动装配字段相对），要么将受影响的@Bean方法声明为静态，将其与包含的配置类实例及声明周期解藕。否则，仅在回退阶段考虑此类bean，而将其他配置类上的匹配bean标记为主要候选对象（如果可用）。*

`@Autowired`可以应用在字段、构造器、多参数方法中，在参数级别上使用限定注解来缩小范围。相反，`@Resource`只支持字段级别和只有一个参数的setter方法。因此，如果注入目标是构造函数或多参数方法，则应坚持使用限定符。

可以自定义限定符注解。如下：
```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Genre {

    String value();
}
```

然后，可以在自动装配的字段或参数上使用该限定符：
```
public class MovieRecommender {

    @Autowired
    @Genre("Action")
    private MovieCatalog actionCatalog;

    private MovieCatalog comedyCatalog;

    @Autowired
    public void setComedyCatalog(@Genre("Comedy") MovieCatalog comedyCatalog) {
        this.comedyCatalog = comedyCatalog;
    }

    // ...
}
```
接下来，可以提供bean定义的信息。可以在<bean/>标签中增加<qualifier/>子标签然后定义匹配自定义限定符的`type`和`value`。该类型与注释的全限定类名匹配。另外，如果没有名字冲突的风险，可以使用简短的类名。
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="Genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="example.Genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

在类路径扫描和组件管理中，可以查看在XML中提供限定元数据的另一种方案。

在某些情况下，不带value的注解可能就足够了。当注解服务于一个更通用的目的并且应用跨类型的依赖时，这非常有用。例如，，当互联网链接不可用时可能提供一个离线的目录。首先，定义一个简单的注解，像下面一样：

```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Offline {

}
```

在自动装配的字段或属性上增加注解：
```
public class MovieRecommender {

    @Autowired
    @Offline 
    private MovieCatalog offlineCatalog;

    // ...
}
```

现在，只需要定义限定符类型：
```
<bean class="example.SimpleMovieCatalog">
    <qualifier type="Offline"/> 
    <!-- inject any dependencies required by this bean -->
</bean>
```

Offline这个元素被指定为限定符。

自定义限定符接受除了简单值属性之外的代替属性。如果多个属性值被定义在一个字段或者参数上，bean定义必须与此类属性值匹配，才能成为自动装配的候选。例如：
```
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MovieQualifier {

    String genre();

    Format format();
}
```

在这个例子中，`Format`是一个枚举类：
```
public enum Format {
    VHS, DVD, BLURAY
}
```

这个类通过自定义限定符自动装配并且包含的属性有：`genre`和`format`：
```
public class MovieRecommender {

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Action")
    private MovieCatalog actionVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Comedy")
    private MovieCatalog comedyVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.DVD, genre="Action")
    private MovieCatalog actionDvdCatalog;

    @Autowired
    @MovieQualifier(format=Format.BLURAY, genre="Comedy")
    private MovieCatalog comedyBluRayCatalog;

    // ...
}
```

最终，bean定义应该包含匹配的限定值。这个例子表名可以定义bean元数据属性来代替<qualifier/>元素。如果有的话，<qualifier/>元素及其属性有限，但是，如果没有这样的限定符，自动装配机制就会回退到<meta/>标记中提供的值。
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Action"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Comedy"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="DVD"/>
        <meta key="genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="BLURAY"/>
        <meta key="genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

</beans>
```

### 1.9.5. 使用泛型作为自动装配限定符

除了`@Qualifier`注解，还可以使用Java泛型作为限定的隐含形式。例如，假如有如下的配置：
```
@Configuration
public class MyConfiguration {

    @Bean
    public StringStore stringStore() {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore() {
        return new IntegerStore();
    }
}
```

假设前面的bean实现了泛型接口，（也就是说实现了`Store<String>`和`Store<Integer>`），那么就可以使用`@Autowired`、接口`Store`和泛型作为限定：
```
@Autowired
private Store<String> s1; // <String> qualifier, injects the stringStore bean

@Autowired
private Store<Integer> s2; // <Integer> qualifier, injects the integerStore bean
```

泛型限定也支持装配list,`Map`实例和数组：
```
// Inject all Store beans as long as they have an <Integer> generic
// Store<String> beans will not appear in this list
@Autowired
private List<Store<Integer>> ;
```

### 1.9.6 使用`CustomAutowireConfigurer`

`CustomAutowireConfigurer`是一个`BeanFactoryPostProcessor`，它可以注册自定义的限定符注解类型，即使它们没有被Spring的`@Qualifier`注释。下面的例子展示了如何使用`CustomAutowireConfigurer`：

```
<bean id="customAutowireConfigurer"
        class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer">
    <property name="customQualifierTypes">
        <set>
            <value>example.CustomQualifier</value>
        </set>
    </property>
</bean>
```

`AutowireCandidateResolver`通过下面的方式来决定自动装配的候选者：
* 每个bean定义的`autowire-candidate`的值
* 任何在`<beans/>`元素应以的可用`default-autowire-candidates`
* 存在的`@Qualifier`注解和任何注册到`CustomAutowireConfigurer`的自定义注解

当多个bean限定作为自动装配的候选者，决定是一个主要的候选者的情况如下：如果在多个候选者中，有一个明确的bean定义并且他的`primary`属性是`true`，它就会被选择。

### 1.9.7 `@Resource`注入

Spring也支持使用JSR-250的`@Resource`(`javax.annotation.Resource`),可以注解到字段或者属性的setter方法上。这是一个Java EE的通用模式，例如，在JSF-mananged beans和JAX-WS endpoints。Spring对这种模式也支持的很好。

`@Resource`有一个name的属性。默认情况下，Spring拦截这个值并注入。有通过名字来注入点语义。
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource(name="myMovieFinder") 
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```
这里，通过`@Resource`注入了MovieFinder。

如果没有明确的指定名称，默认的名称是来自字段的名字后者方法。如果是字段，它将会获取字段的名字，如果是一个setter方法，他会获取bean属性的名字。下面的例子会获取bean名字为`movieFinder`来作为setter方法的注入：
```
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

*注解提供的名字会被`ApplicationContext`中的`CommonAnnotationBeanPostProcessor`感知，并且作为bean名字来解析。如果明确的配置了Spring的`SimpleJndiBeanFactory`,名字将通过JNDI来解析。但是，建议依赖默认的行为并且使用Spring的JNDI的查找能力来保留间接级别。*

在没有指定显示名称的情况下，使用`@Resource`的特殊情况，类似于`@Autowired`，`@Resource`查找主类型匹配，而不是特定的命名bean，并解析所有已知的可解析依赖：`BeanFactory`，`ApplicationContext`，`ResourceLoader`，`ApplicationEventPublisher`，`MessageSource`。

因此，在下面的例子中，`customerPreferenceDao`字段首先查找一个名叫"customerPreferenceDao"的bean，然后返回可匹配`CustomerPreferenceDao`类型的主要类型。
```
public class MovieRecommender {

    @Resource
    private CustomerPreferenceDao customerPreferenceDao;

    @Resource
    private ApplicationContext context; 

    public MovieRecommender() {
    }

    // ...
}
```
`context`是已知的可解析的依赖类型：`ApplicationContext`。

### 1.9.8. 使用`@Value`

`@Value`通常被用来注入外部属性:

```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name}") String catalog) {
        this.catalog = catalog;
    }
}
```

结合配置类使用：
```
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig { }
```
配置类文件：
```
catalog.name=MovieCatalog
```

在这个例子中，`catalog`参数和字段等于`MovieCatalog`的值。

Spring提供一个默认的松散的内嵌值解析器。它将尝试解析属性值，如果无法解析，则将属性名称（例如：`${catalog.name}`）作为值注入。如果要严格控制不存在的值，应该声明一个`PropertySourcesPlaceholderConfigurer`:
```
@Configuration
public class AppConfig {

     @Bean
     public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
           return new PropertySourcesPlaceholderConfigurer();
     }
}
```

*当使用JavaConfig配置了一个`PropertySourcesPlaceholderConfigurer`时，`@Bean`方法必须是静态的*

使用上面的配置，可以确保如果任何的`${}`占位符没有被解析，Spring初始化会失败。它支持自定义的占位符，可以使用方法`setPlaceholderPrefix`、`setPlaceholderSuffix`、`setValueSeparator`.

*Spring Boot 默认了一个`PropertySourcesPlaceholderConfigurer`，可以从`application.properties`和`application.yml`文件中提取属性。*

Spring提供内置的转换支持，允许简单类型自动转换（例如`Integer`或者`int`）。被逗号分割的值可以自动的转化为Spring
数组不需要额外的处理。

```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name:defaultCatalog}") String catalog) {
        this.catalog = catalog;
    }
}
```

Spring`BeanPostProcessor`在后台使用`ConversionService`，处理将`@Value`中的String值转换为目标类型的过程。如果想要提供自定义的类型转换支持，可以提供一个自定义的`ConversionService`实例：
```
@Configuration
public class AppConfig {

    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new MyCustomConverter());
        return conversionService;
    }
}
```

当`@Value`包含一个`SpEL`表达式，这个值将会在运行时自动计算：
```
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("#{systemProperties['user.catalog'] + 'Catalog' }") String catalog) {
        this.catalog = catalog;
    }
}
```

SpEL支持更复杂的数据结构:
```
@Component
public class MovieRecommender {

    private final Map<String, Integer> countOfMoviesPerCatalog;

    public MovieRecommender(
            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;
    }
}
```

### 1.9.9. 使用`@PostConstruct`和`@PreDestroy`

`CommonAnnotationBeanPostProcessor`不仅可以识别`@Resource`注解，而且可以识别JSR-250声明周期的注解:`javax.annotation.PostConstruct`和`javax.annotation.PreDestroy`。在Spring2.5以后引入，用来支持描述声明周期的回调机制，提供初始化回调和销毁回调。在下面的例子中，缓存在初始化时被预先填充，并在销毁时清除：
```
public class CachingMovieLister {

    @PostConstruct
    public void populateMovieCache() {
        // populates the movie cache upon initialization...
    }

    @PreDestroy
    public void clearMovieCache() {
        // clears the movie cache upon destruction...
    }
}
```

有关组合各种生命周期机制的效果的详细信息，请参考Combining Lifecycle Mechanisms。

*像`@Resource`，`@PostConstruct`，`@PreDestroy`这几个注解类型，是JDK6-8中的标准Java库的一部分。但是，整个`javax.annotation`包在JDK9中的核心Java模块被分离，并最终在JDK11中删除。如果需要通过Maven
获取`javax.annotation-api`，只需要像其他任和库一样将其添加到应用程序的类路径即可。*

## 1.10. 类路径扫描和托管组件 
在本章中，大多数的例子将使用XML来定义配置元数据，为Spring容器提供`BeanDefinition`。前面的部分（基于注解的容器配置）证实了怎样通过代码级的注解来提供大量的配置元数据。但是，即使在这些实例中，基本bean定义也已经在XML文件中明确定义了，而注解仅仅驱动依赖项的注入。本节介绍了通过扫描类路径来隐式检测候选组件。候选组件是与过滤条件匹配的类，并且是容器中注册了的bean定义。这消除了使用XML进行bean注册的需要。相反，可以使用注解（例如`@Component`），Aspect类表达式或者自定义的过滤条件来选择容器中注册的bean定义。

*从Spring3.0以后，Spring JavaConfig工程作为Spring框架的核心部分，提供了许多的特性。这允许用户使用Java而不是传统的XML来定义bean。*

### 1.10.1. `@Component`和刻板注解

`@Repository`注解是满足角色或仓库的标记（也成为数据访问对象或DAO）。该标记的用途包括自动翻译异常，详情在异常翻译中所述。

Spring提供刻板注解：`@Component`，`@Repository`和`@Controller`。`@Component`是任何Spring托管组件的通用组件。`@Repository`，`@Service`和`@Controller`是`@Component`的特例，在特定场景下使用（在持久层，服务层和表示层）。因此，可以对组件类使用`@Component`，但是，通过使用`@Repository`，`@Repository`或`@Controller`替代`@Component`。例如，这些刻板注解成为切入点的理想目标。这几个注解在Spring框架的未来版本中也承担了额外的语义。因此，如果对服务层来说，`@Component`和`@Service`相比，`@Service`会更好并且更清晰。类似的，如前所述，`@Repository`已经作为持久层中支持自动异常翻译的标记。

### 1.10.2. 使用元数据注解和组合注解

Spring提供的大多数注解可以在代码中被当做元数据注解使用。一个元数据注解可以应用另一个元数据注解。例如，`@Service`注解使用`@Component`进行注解，如下所示：
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component 
public @interface Service {

    // ...
}
```

`@Component`导致`@Service`被当做`@Component`来对待。

可以连接元注解来创建组合注解。例如，Spring MVC中的`@RestController`注解是`@Controller`和`@ResponseBody`的混合。

此外，组合注解允许自定义，可以选择从元注解中重新声明属性。当是希望空开元注解属性的子集时，此功能非常有用。例如，Spring的`@SessionScope`注解硬编码到了名称为`session`的作用域中，但是仍然允许自定义`proxyMode`。下面的例子列出了定义`SessionScope`注解：
```
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(WebApplicationContext.SCOPE_SESSION)
public @interface SessionScope {

    /**
     * Alias for {@link Scope#proxyMode}.
     * <p>Defaults to {@link ScopedProxyMode#TARGET_CLASS}.
     */
    @AliasFor(annotation = Scope.class)
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

}
```

可以使用`@SessionScope`并且不需要声明`proxyMode`：
```
@Service
@SessionScope
public class SessionScopedService {
    // ...
}
```

可以覆盖`proxyMode`，如下：
```
@Service
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)
public class SessionScopedUserService implements UserService {
    // ...
}
```

更多细节，参考Spring注解编程模型。

### 1.10.3. 自动侦测类，注册bean definitions

Spring可以自动检测刻板类并且将相应的`BeanDefinition`注册到`ApplicationContext`。例如，下面的两个类：
```
@Service
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    public SimpleMovieLister(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

```
@Repository
public class JpaMovieFinder implements MovieFinder {
    // implementation elided for clarity
}
```

为了自动检测和注入这些相应的beans，需要增加`@ComponentScan`在`@Configuration`的类中。`basePackages`属性是这两个类的共同的父package。（另外，可以指定一个逗号或者空白符分割的列表，包含每个类的父package）。
```
@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig  {
    // ...
}
```

```
为简便起见，前面的示例可能使用了注释的value属性（即@ComponentScan（“ org.example”））。
```

另外也可以使用XML:
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.example"/>

</beans>
```

*使用`<context:component-scan>`暗示开启`<context:annotation-config>`功能。当使用`<context:component-scan>`时，就不需要使用`<context:annotation-config>`了。*

*扫描类路径包需要在类路径中存在相应的目录条目。当使用Ant构建JARs时，确保不激活JAR任务的files-only开关。此外，在某些环境中，基于安全策略可能不会公开类路径目录-例如，JDK 1.7.0_45及更高版本上的独立应用程序（这需要在清单中设置“受信任的库”）。*

*在JDK9的模块路径上，Spring的类路径扫描通常可以正常进行。然而，请确保组件类已在模块信息描述符中导出。如果希望Spring在类中调用非公共成员，确保他们是“开放的”（也就是说，他们在模块信息描述符中使用了一个opens声明而不是exports声明）。*

此外，当使用组件扫描元素时，将隐式包含`AutowiredAnnotationBeanPostProcessor`和`CommonAnnotationBeanPostProcessor`。这意味着这两个组件会被自动侦测并装配到一起，而所有这些都不需要XML提供任何bean配置元数据。

*可以通过将注释配置属性包括为false来禁用AutowiredAnnotationBeanPostProcessor和CommonAnnotationBeanPostProcessor的注册。*

### 1.10.4. 使用过滤器自定义扫描

默认情况下，`@Component`, `@Repository`, `@Service`, `@Controller`, `@Configuration`或一个自身使用了`@Component`的自定义注释是唯一检测到的候选组件。但是，可以通过应用自定义过滤器来修改和扩展此行为。将它们添加为`@ComponentScan`批注的`includeFilters`或`excludeFilters`属性（或作为XML配置中`<context：component-scan>`元素的`<context：include-filter />`或`<context：exclude-filter />`子元素）。每个过滤器需要`type`和`expression`属性。下面的表格描述了过滤器选项：

|Filter Type|Example Expression	|Description|
|----|----|---|
|annotation (default)|`org.example.SomeAnnotation`|An annotation to be present or meta-present at the type level in target components.|
|assignable|`org.example.SomeClass`|A class (or interface) that the target components are assignable to (extend or implement).|
|aspectj|`org.example..*Service+`|An AspectJ type expression to be matched by the target components.|
|regex|`org\.example\.Default.*`|A regex expression to be matched by the target components' class names.|
|custom|`org.example.MyTypeFilter`|A custom implementation of the `org.springframework.core.type.TypeFilter` interface.|

下面的例子展示了配置忽略所有`@Repository`注解，并且使用`stub`repositories来代替：
```
@Configuration
@ComponentScan(basePackages = "org.example",
        includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Stub.*Repository"),
        excludeFilters = @Filter(Repository.class))
public class AppConfig {
    ...
}
```

XML中等效的配置：
```
<beans>
    <context:component-scan base-package="org.example">
        <context:include-filter type="regex"
                expression=".*Stub.*Repository"/>
        <context:exclude-filter type="annotation"
                expression="org.springframework.stereotype.Repository"/>
    </context:component-scan>
</beans>
```

*可以通过设置`useDefaultFilters=false`或`use-default-filters="false`来禁用默认的过滤器。这有效地禁用了对使用`@Component`，@Repository，`@Service`，`@Controller`，`@RestController`或`@Configuration`进行注释或元注释的类的自动检测。*

### 1.10.5在组件中定义bean元数据

Spring组件可以给容器提供bean定义元数据。您可以使用与@Configuration带注释的类中的Bean元数据定义相同的@Bean注释来完成此操作。下面的例子展示了如何这样做：
```
@Component
public class FactoryMethodComponent {

    @Bean
    @Qualifier("public")
    public TestBean publicInstance() {
        return new TestBean("publicInstance");
    }

    public void doWork() {
        // Component method implementation omitted
    }
}
```

这个类是Spring组件，在其`doWork()`中具有特定的应用程序代码。但是，它也提供了定义bean的工厂方法`publicInstance()`。`@Bean`注解标识了工厂方法和另一个bean定义的属性，例如限定值。也可以指定其他方法级别的注释，如:`@Scope`，`@Lazy`和自定义的限定注解。

*除了用于组件初始化的角色外，还可以将`Lazy`注解放在标有`@Autowired`和`@Inject`注入点上。在这种情况下，它导致了惰性解析代理的注入。*

如前所述，支持自动装配的字段和方法，并自动装配@Bean方法。以下示例显示了如何执行此操作：
```
@Component
public class FactoryMethodComponent {

    private static int i;

    @Bean
    @Qualifier("public")
    public TestBean publicInstance() {
        return new TestBean("publicInstance");
    }

    // use of a custom qualifier and autowiring of method parameters
    @Bean
    protected TestBean protectedInstance(
            @Qualifier("public") TestBean spouse,
            @Value("#{privateInstance.age}") String country) {
        TestBean tb = new TestBean("protectedInstance", 1);
        tb.setSpouse(spouse);
        tb.setCountry(country);
        return tb;
    }

    @Bean
    private TestBean privateInstance() {
        return new TestBean("privateInstance", i++);
    }

    @Bean
    @RequestScope
    public TestBean requestScopedInstance() {
        return new TestBean("requestScopedInstance", 3);
    }
}
```

该示例将`String`方法参数`country`自动装配到另一个名为`privateInstance`bean的`age`属性上。Spring表达式语言元素通过`#{<expression>}`定义属性。对于`@Value`注解，表达式解析程序已预先配置为解析表达式文本时查找bean名称。

Spring4.3以后，可以声明一个类型为`InjectionPoint`的工厂方法参数（或者他的子类:`DependencyDescriptor`）以访问触发当前bean创建的请求注入点。注意，这仅适用于实际创建bean实例，而不适用于注入现有实例。因此，此功能对于原型范围的bean最有意义。对于其他作用域，factory方法仅在给定作用域看到触发创建新bean实例的注入点（例如，触发创建惰性单例bean的依赖项）。在这种情况下，可以将提供的注入点元数据与语义一起使用。

```
@Component
public class FactoryMethodComponent {

    @Bean @Scope("prototype")
    public TestBean prototypeInstance(InjectionPoint injectionPoint) {
        return new TestBean("prototypeInstance for " + injectionPoint.getMember());
    }
}
```

常规Spring组件中的@Bean方法的处理方式与Spring @Configuration类中的@Bean方法不同。不同点在于`@Component`类没有使用CGLIB增强，无法拦截字段和方法的调用。CGLIB代理调用`@Configuration`类中`@Bean`方法中的方法或字段方法，用于创建Bean元数据作为协作者。这些方法不会被普通的Java语义调用，而是通过容器进行，以提供通常的Spring Bean声明周期管理和代理，即使通过`@Bean`方法的编程调用引用其他Bean也是如此。相反，在普通`@Component`类中的`@Bean`方法中调用方法或字段具有标准的Java语义，而无需特殊的CGLIB处理或其它约束。

*可以声明`@Bean`为静态方法，允许他们在无需创建包含配置的实例时被调用。在定义post-processor beans（例如，`BeanFactoryPostProcessor`和`BeanPostProcessor`）时，这有特殊的意义，因为这些在容器生命周期中尽早初始化并且应该避免触发配置的其他部分。*

*容器不会拦截调用静态的`@Bean`方法，甚至在`@Configuration`中（本章中前期描述的），由于技术限制：CGLIB子类仅可以覆盖非静态方法。结果，直接调用另一个`@Bean`方法具有标准的Java语义，从而导致直接从工厂方法本身直接返回一个独立的实例。*

*@Bean方法的Java语言可见性不会对Spring容器中的最终bean定义产生直接影响。您可以在非@Configuration类中自由声明自己的工厂方法，也可以在任何地方声明静态方法。但是，@ Configuration类中的常规@Bean方法必须是可重写的-即，不得将它们声明为private或final。*

*还可以在给定组件或配置类的基类上以及在由组件或配置类实现的接口中声明的Java 8默认方法上发现@Bean方法。这为组合复杂的配置安排提供了很大的灵活性，从Spring 4.2开始，通过Java 8默认方法甚至可以进行多重继承。*

*最后，一个类可以为同一个bean保留多个@Bean方法，这取决于在运行时可用的依赖关系，从而可以使用多个工厂方法。这与在其他配置方案中选择“最贪婪”的构造函数或工厂方法的算法相同：在构造时会选择具有最大可满足依赖性的变量，这类似于容器如何在多个@Autowired构造函数之间进行选择。*

### 1.10.6. 命名自动侦测组件

当组件被作为扫描进程的一部分时，它的bean名称是通过`BeanNameGenerator`策略扫描生成的。默认情况下，任何Spring刻板注解（@Component, @Repository, @Service, and @Controller）都包含一个`value`,从而为相应的bean提供bean名称定义。

如果这样的注释不包含名称值，或者不包含任何其他检测到的组件（例如，由自定义过滤器发现的组件），则缺省bean名称生成器将返回未大写的非限定类名称。例如，如果检测到以下组件类，则名称将为`myMovieLister`和`movieFinderImpl`：
```
@Service("myMovieLister")
public class SimpleMovieLister {
    // ...
}
```

```
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

如果不想依靠默认的命名策略，可以提供一个自定义的命名策略。首先，实现`BeanNameGenerator`接口，并且确保包含一个默认的无参构造器。然后，在配置扫描程序时提供完全限定的类名，如以下示例注释和Bean定义所示。

*如果由于多个自动检测到的组件具有相同的非限定类名而导致命名冲突（即名称相同但位于不同包中的类），您可能需要配置一个BeanNameGenerator，该名称默认为生成的Bean名称的完全限定的类名称。从Spring Framework 5.2.3开始，位于org.springframework.context.annotation包中的FullyQualifiedAnnotationBeanNameGenerator可以用于此目的。*

```
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
    // ...
}
```

```
<beans>
    <context:component-scan base-package="org.example"
        name-generator="org.example.MyNameGenerator" />
</beans>
```

通常，请考虑在其他组件可能对其进行显式引用时，使用指定名称。另一方面，只要容器负责自动装配，自动生成的名称就足够了。

### 1.10.7. 为自动侦测的组件提供作用域

与通常使用Spring管理的组件一样，默认的大多数自动侦测到的组件的作用域是`singleton`。但是，有时候需要通过`@Scope`注解来指定不同的作用域。可以为作用域注解提供一个名称，如下：
```
@Scope("prototype")
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

*`@Scope`注解仅在具体类上（对于注释组件）或者工厂方法（对于`@Bean`方法）上进行内省。与XML bean定义相反，没有bean定义继承的概念，并且在类级别的继承层次结构与元数据目的无关。*

更多web特定的作用域，例如"request"或"session"，查看Request, Session, Application, and WebSocket Scopes。与这些范围的内置注解一样，可以使用Spring的元注解来组合自己的作用域：例如，一个自定义的元注解`@Scope("prototype")`，可能也声明了自定义的scoped-proxy mode。

*提供用于解析作用域的自定义策略，而不是依赖于注解的方法，可以实现`ScopeMetadataResolver`接口。确保包含一个默认的无参构造器。然后，当扫描配置时，可以提供全限定类名，下面的配置是一个注解和一个bean的定义：*

```
@Configuration
@ComponentScan(basePackages = "org.example", scopeResolver = MyScopeResolver.class)
public class AppConfig {
    // ...
}
```

```
<beans>
    <context:component-scan base-package="org.example" scope-resolver="org.example.MyScopeResolver"/>
</beans>
```

当使用某些非单例作用域时，可能需要为作用域对象生成代理。原因在Scoped Beans as Dependencies中描述过。为此，在组件元素扫描时，可以使用scoped-proxy属性。这里有三个值：`no`，`interface`和`targetClass`.例如，下面是基于标准jdk动态代理的配置：
```
@Configuration
@ComponentScan(basePackages = "org.example", scopedProxy = ScopedProxyMode.INTERFACES)
public class AppConfig {
    // ...
}
```

```
<beans>
    <context:component-scan base-package="org.example" scoped-proxy="interfaces"/>
</beans>
```

### 1.10.8. 提供带有注解的限定符元数据

`@Qualifier`在1.9.4中讨论过。本章节将要展示当解析自动装配候选时，使用`@Qualifier`注解和自定义限定符来提供微调。因为这些例子是基于XML bean定义的，通过使用在XML中的`<bean>`元素中的`qualifier`或`meta`子元素，提供候选者的bean定义限定符元数据。当依赖classpath来完成组件自动扫描时，可以在候选类上提供类型级别的限定元数据。下面的三个例子展示了这种技术:
```
@Component
@Qualifier("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}
``` 

```
@Component
@Genre("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}
```

```
@Component
@Offline
public class CachingMovieCatalog implements MovieCatalog {
    // ...
}
```

*与大多数基于注解的替代方法一样，请记住，注解元数据绑定到类定义本身，XML的使用允许相同类型的多个bean提供限定元数据的辩题，因为该元数据是按实例而不是按类提供的。*

### 1.10.9 生成候选组件索引

虽然classpath扫描非常快，但可以通过在编译时创建静态候选列表来提高大型应用的启动性能。在这个模式下，所有作为组件扫描目标的所有模块都必须使用此机制 。

*已经存在的`@ComponentSan`或`<context:component-scan>`指令必须保留，以请求上下文扫描某些软件包中的候选对象。当`ApplicationContext`侦测到这些索引时，它会自动使用而不是扫描classpath。*

为了生成索引，需要向每个包含组件的模块添加附加依赖关系，这些组件是组件扫描指令的目标。下面的例子展示了如何使用maven来创建：
```
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context-indexer</artifactId>
        <version>5.3.1</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

生成过程将包含一个在jar文件中的`META-INF/spring.components`文件。

当使用IDE在这种模式下工作时，`spring-context-indexer`必须作为注解处理器被注册，来确保候选组件更新时，索引是最新的。

## 1.11. 使用JSR 330标准注解

Spring3.0以后，提供了JSR-330标准注解的支持（依赖注入）。这些注解的扫描方式与Spring注释的扫描方式相同。为了使用他们，需要引入相关的jar到classpath。

*如果使用Maven，这是[`javax.inject`仓库](https://repo1.maven.org/maven2/javax/inject/javax.inject/1/)，可以将下面的依赖加入到pom文件中：*
```
<dependency>
    <groupId>javax.inject</groupId>
    <artifactId>javax.inject</artifactId>
    <version>1</version>
</dependency>
```

### 1.11.1 使用`@Inject`和`@Named`来完成依赖注入

可以使用`@javax.inject.Inject`来代替`@Autowired`:

```
import javax.inject.Inject;

public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    public void listMovies() {
        this.movieFinder.findMovies(...);
        // ...
    }
}
```

和`@Autowired`一样，可以在字段级别、方法级别和构造器参数级别上使用`@Inject`。此外，可以将注入点作为`Provider`，从而允许按需访问范围较小的bean，或者通过Provider.get()调用来惰性访问其他bean:
```
import javax.inject.Inject;
import javax.inject.Provider;

public class SimpleMovieLister {

    private Provider<MovieFinder> movieFinder;

    @Inject
    public void setMovieFinder(Provider<MovieFinder> movieFinder) {
        this.movieFinder = movieFinder;
    }

    public void listMovies() {
        this.movieFinder.get().findMovies(...);
        // ...
    }
}
```

如果喜欢使用依赖限定符名，可以使用`@Named`注解：

```
import javax.inject.Inject;
import javax.inject.Named;

public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(@Named("main") MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

和`@Autowired`一样，`@Inject`也可以和`java.util.Optional`或`@Nullable`使用。在这里更加适用，因为`@Inject`没有`required`属性:

```
public class SimpleMovieLister {

    @Inject
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {
        // ...
    }
}
```

```
public class SimpleMovieLister {

    @Inject
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {
        // ...
    }
}
```

### 1.11.2. `@Named`和`@ManagedBean`:和`@Component`等效的注解

可以使用`@javax.inject.Named`或`javax.annotation.ManagedBean`来代替`@Component`：

```
import javax.inject.Inject;
import javax.inject.Named;

@Named("movieListener")  // @ManagedBean("movieListener") could be used as well
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

在没有指定组件名称的情况下使用@Component是很常见的。类似的方式使用@Named，如以下示例所示：

```
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

当使用@Named或@ManagedBean时，可以使用与使用Spring注释完全相同的方式来使用组件扫描，如以下示例所示：

```
@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig  {
    // ...
}
```

和`@Component`相反，JSR-330`@Named`和JSR-250`ManageBean`注解不能组合。应该使用Spring的刻板模型来构建自定义组件注解。

### 1.11.3 JSR-330注解的限制

当使用标准注解时，应该知道有一些重要的特性是不可用的，下面表格举例说明：

Table 6. Spring component model elements versus JSR-330 variants 

| Spring | javax.inject.* |javax.inject restrictions/comments |
| ---- | ----- | ----- |
| @Autowired|@Inject|@Inject has no 'required' attribute. Can be used with Java 8’s Optional instead.|
|@Component|@Named / @ManagedBean|JSR-330 does not provide a composable model, only a way to identify named components.|
|@Scope("singleton")|@Singleton|The JSR-330 default scope is like Spring’s prototype. However, in order to keep it consistent with Spring’s general defaults, a JSR-330 bean declared in the Spring container is a singleton by default. In order to use a scope other than singleton, you should use Spring’s @Scope annotation. javax.inject also provides a @Scope annotation. Nevertheless, this one is only intended to be used for creating your own annotations.|
|@Qualifier|@Qualifier / @Named|javax.inject.Qualifier is just a meta-annotation for building custom qualifiers. Concrete String qualifiers (like Spring’s @Qualifier with a value) can be associated through javax.inject.Named.|
|@Value|-|no equivalent
|@Required|-|no equivalent
|@Lazy|-|no equivalent
|ObjectFactory|Provider|javax.inject.Provider is a direct alternative to Spring’s ObjectFactory, only with a shorter get() method name. It can also be used in combination with Spring’s @Autowired or with non-annotated constructors and setter methods|

## 1.12. 基于Java容器的配置

本章覆盖了如何在Java代码中使用注解来配置Spring容器。它包括了如下的话题：
* 基本概念：`@Bean`和`@Configuration`
* 使用`AnnotationConfigApplicationContext`初始化Spring容器
* 使用`@Bean`注解
* 使用`@Configuration`注解
* 组成基于Java的配置
* Bean定义配置文件
* 抽象`PropertySource`
* 声明中的占位符解析

### 1.12.1. `@Bean`和`@Configuration`基本概念

Spring新的Java配置中支持的主要工件是`@Configuration`注解的类和`@Bean`注解的方法。

`@Bean`注解用来指一个方法实例化，配置和初始化对象要由Spring IoC容器来管理。和那些Spring的`<bean/>`配置相似，`@Bean`注解扮演了和`<bean/>`相同的角色。可以将`@Bean`注解和任何Spring `@Component`一起使用。但是，他们大多数是和`@Configuration`beans一起使用的。

带有`@Configuration`注解的类表名，它的主要目的是作为bean 定义的来源。此外，`@Configuration`类允许通过调用同一个类中的其他`@Bean`方法来定义Bean之间的依赖关系:

```
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyServiceImpl();
    }
}
```

前面的`AppConfig`等价于下面的Spring `<bean/>`配置：

```
<beans>
    <bean id="myService" class="com.acme.services.MyServiceImpl"/>
</beans>
```

**完整的`@Configuration`模式vs精简的`@Bean`模式**

*当在没有`@Configuration`注解的类中声明`@Bean`方法时，他们被称为以精简模式进行处理。在一个`@Component`或一个普通Java对象中声明bean方法，被认为是精简模式，其中包含类的主要目的不同，而`@Bean`方法在那里具有某种优势。例如，service组件可能暴露通过每个适用组件类上的其他`@Bean`方法将管理视图公开给容器。在这些场景下，`@Bean`方法是一种通用的工厂方法机制。*

*和完全`@Configuration`不同，精简的`@Bean`方法不能声明Bean之间的依赖关系。取而代之的是，他们在其包含组件的内部状态上进行操作，并根据需要声明的参数进行操作。因此，此类@Bean方法不应调用其他@Bean方法。每个这样的方法仅仅是针对特定bean引用的工厂方法，没有任何特殊的运行时语义。这里的积极作用是，不必在运行时应用CGLIB子类化，因此，在类设计方面没有任何限制（也就是说，包含的类可能是final等）。*

*在一般场景下，`@Bean`方法和`@Configuration`类一起使用，确保始终使用完全模式，并因此将跨方法引用重定向到容器的生命周期管理中。这防止了通过常规Java意外的调用相同的`@Bean`方法，并有助于减少在“精简”模式下运行时难以跟踪的细微错误。*

`@Bean`和`@Configuration`注解在接下来的章节中会深入讨论。首先，会介绍使用基于Java配置来创建Spring容器的各种方法。

### 1.12.2. 使用`AnnotationCinfigApplicationContext`实例化Spring容器

以下各节介绍了Spring 3.0中引入的`AnnotationConfigApplicationContext`。这种通用的`ApplicationContext`实现不仅能够接受`@Configuration`类作为输入，而且还可以接受普通的`@Component`类和带有JSR-330注解的类。

当`@Configuration`类作为输入时，`@Configuration`类本身作为bean定义被注册并且所有声明为`@Bean`方法的类也被注册为bean定义。

当提供`@Component`和JSR-330类时，他们作为bean定义被注册，并且假定在必要时这些类中使用了诸如`@Autowired`或`@Inject`之类的DI元数据。

**简单构建**

与实例化`ClassPathXmlApplicationContext`时将Spring XML文件用作输入的方式几乎相同，当初始化一个`ApplicationConfigApplicationContext`时，可以使用`@Configuration`类作为输入。这允许完全不使用XML来初始化Spring容器。

```
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

如前所述，`AnnotationConfigApplicationContext`不限于只和`@Configuration`类一起工作。任何`@Component`或JSR-330注解的类都可以作为构造函数的输入：

```
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(MyServiceImpl.class, Dependency1.class, Dependency2.class);
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

前面的例子假设了`MyServiceImpl`,`Dependency1`和`Dependency2`使用了Spring依赖注入的注解，例如`@Autowired`。

**通过使用`register(Class<?>...)`编程构建容器**

可以通过使用一个无参的构造器来实例化一个`AnnotationConfigApplicationContext`并且通过使用`register()`方法来配置。这种方式适合使用编程来构建`AnnotationConfigApplicationContext`:

```
public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(AppConfig.class, OtherConfig.class);
    ctx.register(AdditionalConfig.class);
    ctx.refresh();
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

**通过`scan(String...)`开启组件扫描**

为了开启组件扫描，可以在`@Configuration`类上加上注解：
```
@Configuration
@ComponentScan(basePackages = "com.acme") 
public class AppConfig  {
    ...
}
```

这个`@ComponentScan`注解开启了组件扫描。

*有经验的用户可能会感到和下面的XML定义的`context:`命名空间相似：*
```
<beans>
    <context:component-scan base-package="com.acme"/>
</beans>
```

在前面的例子中，`com.acme`包中会扫描任何带有`@Component`注解的类，并且这些类会当做容器中的Spring的bean定义。`AnnotationConfigApplication`暴露了`scan(String...)`方法来允许相同的组件扫描功能：
```
public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.acme");
    ctx.refresh();
    MyService myService = ctx.getBean(MyService.class);
}
```

记住`@Configuration`类使用`@Component`进行元注解，因此他们是组件扫描的候选对象。在前面的例子中，假设`AppConfig`和`com.acme`包被声明（或任何该包之下的），在对`scan()`的调用旗舰会将其自动拾取。根据`refresh()`，所有他的`@Bean`方法都将在容器内进行处理并且注册为bean定义。

**使用AnnotationConfigWebApplication初始化Web应用**

`AnnotationConfigWebApplicationContext`可提供`AnnotationConfigApplicationContext`的`WebApplicationContext`变体。当配置了Spring `ContextLoaderListener` servlet listener，Spring MVC `DispatcherServlet`等等，可以使用这个实现。下面的`web.xml`配置片段是一个典型的Spring MVC web 应用（注意，使用了`contextClass` context-param和init-param。）。
```
<web-app>
    <!-- Configure ContextLoaderListener to use AnnotationConfigWebApplicationContext
        instead of the default XmlWebApplicationContext -->
    <context-param>
        <param-name>contextClass</param-name>
        <param-value>
            org.springframework.web.context.support.AnnotationConfigWebApplicationContext
        </param-value>
    </context-param>

    <!-- Configuration locations must consist of one or more comma- or space-delimited
        fully-qualified @Configuration classes. Fully-qualified packages may also be
        specified for component-scanning -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>com.acme.AppConfig</param-value>
    </context-param>

    <!-- Bootstrap the root application context as usual using ContextLoaderListener -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <!-- Declare a Spring MVC DispatcherServlet as usual -->
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- Configure DispatcherServlet to use AnnotationConfigWebApplicationContext
            instead of the default XmlWebApplicationContext -->
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
        </init-param>
        <!-- Again, config locations must consist of one or more comma- or space-delimited
            and fully-qualified @Configuration classes -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>com.acme.web.MvcConfig</param-value>
        </init-param>
    </servlet>

    <!-- map all requests for /app/* to the dispatcher servlet -->
    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/app/*</url-pattern>
    </servlet-mapping>
</web-app>
```

**1.12.3. 使用`@Bean`注解**
`@Bean`是方法级别的注解并且和XML中的<bean/>元素类似。这个注解支持一些<bean/>提供的属性，例如`init-method`，`destroy-method`,`autowiring`,`name`。

可以在`@Configuration`或`@Component`注解的类中使用`@Bean`。

**声明一个bean**
您可以使用此方法在`ApplicationContext`中注册一个类型为方法返回值的bean定义。默认情况下，bean名字和方法名一致。下面展示了`@Bean`方法声明：
```
@Configuration
public class AppConfig {

    @Bean
    public TransferServiceImpl transferService() {
        return new TransferServiceImpl();
    }
}
```

前面的配置等价与下面的Spring XML文件：
```
<beans>
    <bean id="transferService" class="com.acme.TransferServiceImpl"/>
</beans>
```

这两个声明将一个bean命名为`transferService`,绑定了一个类型为`TransferServiceImpl`的对象实例：
```
transferService -> com.acme.TransferServiceImpl
```

可以将接口作为返回类型：
```
@Configuration
public class AppConfig {

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl();
    }
}
```

但是，这将高级类型预测的可见性限制为指定的接口类型（`TransferService`）。 然后，使用只对容器已知一次的完整类型（`TransferServiceImpl`），实例化受影响的单例bean。 非惰性单例bean根据其声明顺序实例化，因此您可能会看到不同的类型匹配结果，具体取决于另一个组件何时尝试按非声明类型进行匹配（例如`@Autowired` `TransferServiceImpl`，仅当`transferService` bean具有 被实例化）。

*如果通过声明的服务接口一致地引用类型，则@Bean返回类型可以安全地加入该设计决策。然而，对于实现多个接口的组件或他们的实现类型可能引用的组件，声明更具体的返回类型可能比较安全（至少与引用您的bean的注入点所要求的一样具体）。*

**Bean依赖**

一个`@Bean`注解的方法可以有任意多数量的参数来描述需要创建bean所需要的依赖。例如，如果`TransferService`需要一个`AccountRepository`,可以使用方法参数来实现该依赖关系：
```
@Configuration
public class AppConfig {

    @Bean
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}
```
解析机制与基于构造函数的依赖注入大致相同。

**接收生命周期回调**
任何被定义为`@Bean`注解的类支持常规的生命周期回调和使用JSR-250中的`@PostConstruct`、`@Predestory`注解。

常规的生命周期回调完全很好的支持。如果一个bean实现了`InitializingBean`、`DisposableBean`、或`Lifecycle`,他们各自的方法会被容器调用。

还完全支持标准的* Aware接口集（例如`BeanFactoryAware`，`BeanNameAware`，`MessageSourceAware`，`ApplicationContextAware`等）。

`@Bean`注解支持指定任意的初始化和销毁回调方法，可Spring XML配置中的`init-method`和`destory-method`属性相似：
```
public class BeanOne {

    public void init() {
        // initialization logic
    }
}

public class BeanTwo {

    public void cleanup() {
        // destruction logic
    }
}

@Configuration
public class AppConfig {

    @Bean(initMethod = "init")
    public BeanOne beanOne() {
        return new BeanOne();
    }

    @Bean(destroyMethod = "cleanup")
    public BeanTwo beanTwo() {
        return new BeanTwo();
    }
}
```

*默认情况下，Java配置定义的bean有一个公共的`close`或`shutdown`方法来通过销毁回调注册。如果有一个公共的`close`或`shutdown`方法并且不希望当容器关闭时回调，可以增加`@Bean(destoryMethod="")`来关闭默认的（推断）模式。*

*默认情况下，可能要对通过JNDI获取的资源执行此操作，因为其生命周期是在应用程序外部进行管理的。特别是，请确保始终对数据源执行此操作，因为这在Java EE应用程序服务器上是有问题的。*

*以下示例显示如何防止对数据源的自动销毁回调：*

```
@Bean(destroyMethod="")
public DataSource dataSource() throws NamingException {
    return (DataSource) jndiTemplate.lookup("MyDS");
}
```

*另外，对于@Bean方法，通常使用程序化JNDI查找，方法是使用Spring的JndiTemplate或JndiLocatorDelegate帮助器，或者直接使用JNDI InitialContext用法，而不使用JndiObjectFactoryBean变体（这将迫使您将返回类型声明为FactoryBean类型，而不是实际的类型。 目标类型，因此很难在打算引用此处提供的资源的其他@Bean方法中用于交叉引用调用。*

对于前面注释中的示例中的BeanOne，在构造期间直接调用init（）方法同样有效，如以下示例所示：
```
@Configuration
public class AppConfig {

    @Bean
    public BeanOne beanOne() {
        BeanOne beanOne = new BeanOne();
        beanOne.init();
        return beanOne;
    }

    // ...
}
```

*当您直接使用Java工作时，您可以对对象执行任何操作，而不必总是依赖于容器生命周期。*

**指定bean作用域**
Spring包含了`@Scope`注解，可以用来指定bean的作用域。

**使用`@Scope`注解**

可以在`@Bean`注解定义bean时，指定它的作用域。可以使用任何标准的作用域规范。

默认作用域是单例，但是可以通过覆盖`@Scope`注解：
```
@Configuration
public class MyConfiguration {

    @Bean
    @Scope("prototype")
    public Encryptor encryptor() {
        // ...
    }
}
```

**`@Scope`和`scoped-proxy`**

Spring提供了一种通过作用域代理处理作用域依赖性的便捷方法。最简单的方法是当使用XML配置时，用<aop:scoped-proxy/>元素创建一个代理。使用Java`@Scope`注解和它提供的`proxyMode`，与XML配置等效。默认下是没有代理的（ScopedProxyMode.NO）,但是，可以指定`ScopedProxyMode.TARGET_CLASS`或`ScopedProxyMode.INTERFACES`。

如果您使用Java从XML参考文档（请参阅作用域代理）将作用域代理示例移植到我们的@Bean，则它类似于以下内容：
```
// an HTTP Session-scoped bean exposed as a proxy
@Bean
@SessionScope
public UserPreferences userPreferences() {
    return new UserPreferences();
}

@Bean
public Service userService() {
    UserService service = new SimpleUserService();
    // a reference to the proxied userPreferences bean
    service.setUserPreferences(userPreferences());
    return service;
}
```

**自定义Bean命名**

默认情况下，配置类中，`@Bean`方法的名字会作为bean的名字。但是，可以使用name属性覆盖此功能，如以下示例所示：
```
@Configuration
public class AppConfig {

    @Bean(name = "myThing")
    public Thing thing() {
        return new Thing();
    }
}
```

**Bean别名**
和bean命名一样，有时候希望为单个bean提供多个名称，也成为别名。`@Bean`注解中的`name`属性接受一个字符串数组。下面的例子展示了如何设置一组别名：
```
@Configuration
public class AppConfig {

    @Bean({"dataSource", "subsystemA-dataSource", "subsystemB-dataSource"})
    public DataSource dataSource() {
        // instantiate, configure and return DataSource bean...
    }
}
```

**Bean描述**

有时候，提供更多bean的文本信息非常有用。当bean被暴露用来做监控时，特别有用。

为了给`@Bean`加入描述，可以使用`@Description`注解：
```
@Configuration
public class AppConfig {

    @Bean
    @Description("Provides a basic example of a bean")
    public Thing thing() {
        return new Thing();
    }
}
```

### 1.12.4. 使用`@Configuration`注解

`@Configuration`是一个类级别的注解，用来表名类是一个bean定义的来源。`@Configuration`类通过public `@Bean`注解的方法来声明beans。在`@Configuration`类中调用`@Bean`方法，也可以用于定义bean之间的依赖关系。

**注入bean间的依赖关系**

当bean彼此依赖时，表达这种依赖就像让一个bean方法调用另一个一样简单，如以下示例所示：
```
@Configuration
public class AppConfig {

    @Bean
    public BeanOne beanOne() {
        return new BeanOne(beanTwo());
    }

    @Bean
    public BeanTwo beanTwo() {
        return new BeanTwo();
    }
}
```

在前面的示例中，beanOne通过构造函数注入接收到beanTwo的引用。

*仅当在@Configuration类中声明@Bean方法时，此声明bean间依赖关系的方法才有效。您不能通过使用普通的@Component类来声明Bean间的依赖关系。*

**Lookup Method 注入**

如前所述，`look up method`是一个高级功能，但是很少被使用。在一个单例作用域的bean包含原型依赖时非常有用。将Java用于这种类型的配置为实现此模式提供了自然的方法。以下示例显示如何使用查找方法注入：

```
public abstract class CommandManager {
    public Object process(Object commandState) {
        // grab a new instance of the appropriate Command interface
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    // okay... but where is the implementation of this method?
    protected abstract Command createCommand();
}
```

通过使用Java配置，可以创建`Commandmanager`子类，覆盖抽象方法`createCommand()`，以便可以查找到一个新的command object。
```
@Bean
@Scope("prototype")
public AsyncCommand asyncCommand() {
    AsyncCommand command = new AsyncCommand();
    // inject dependencies here as required
    return command;
}

@Bean
public CommandManager commandManager() {
    // return new anonymous implementation of CommandManager with createCommand()
    // overridden to return a new prototype Command object
    return new CommandManager() {
        protected Command createCommand() {
            return asyncCommand();
        }
    }
}
```

**关于Java配置内部工作的更多细节**

思考下面的例子，那个被`@Bean`注解的方法会被调用两次：
```
@Configuration
public class AppConfig {

    @Bean
    public ClientService clientService1() {
        ClientServiceImpl clientService = new ClientServiceImpl();
        clientService.setClientDao(clientDao());
        return clientService;
    }

    @Bean
    public ClientService clientService2() {
        ClientServiceImpl clientService = new ClientServiceImpl();
        clientService.setClientDao(clientDao());
        return clientService;
    }

    @Bean
    public ClientDao clientDao() {
        return new ClientDaoImpl();
    }
}
```

`clientService1()`调用了一次，`clientService2()`调用了一次。虽然这个方法创建了一个新的`ClientDaoImpl`实例，可能通常会希望有两个实例（每个service包含一个）。这确实会有问题：在Spring中，默认情况下，实例化的bean的作用域是`singleton`。所有`@Configuration`类在启动时都使用CGLIB进行了子类化。在子类中，子方法在调用父方法并创建新实例之前，首先检查容器中是否有任何缓存（作用域）的bean。

*根据bean的范围，行为可能有所不同。我们在这里谈论单例。*

*从Spring 3.2开始，不再需要将CGLIB添加到您的类路径中，因为CGLIB类已经在org.springframework.cglib下重新打包并直接包含在spring-core JAR中。*

*由于CGLIB在启动时会动态添加功能，因此存在一些限制。尤其是，配置类不能为final。然而，在4.3以后，配置类允许任何构造器，包含使用`@Autowired`或单个非默认构造声明进行默认注入。*

*如果更喜欢避免任何CGLIB限制，考虑在非`@Configuration`类上声明`@Bean`方法（例如，用普通的`@Component`来代替）。`@Bean`方法之间的跨方法调用不在被拦截，因此，必须专门依赖那里的构造函数或方法级别的依赖项注入。*

### 1.12.5. 组成基于Java的配置

Spring的基于Java的配置特性可以组合注解，减少配置的复杂性。

**使用`@Import`注解**
多数Spring XML文件中的<import/>元素旨在模块化配置，`@Import`注解允许从另一个配置类中加载`@Bean`定义：
```
@Configuration
public class ConfigA {

    @Bean
    public A a() {
        return new A();
    }
}

@Configuration
@Import(ConfigA.class)
public class ConfigB {

    @Bean
    public B b() {
        return new B();
    }
}
```

现在，无需在实例化上下文时同时指定ConfigA.class和ClassB.class,只需显示提供ConfigB：
```
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigB.class);

    // now both beans A and B will be available...
    A a = ctx.getBean(A.class);
    B b = ctx.getBean(B.class);
}
```

这种方法简化了容器的实例化，只需要处理一个类，而不是要求在构造过程中记住潜在的大量`@Configuration`类。

*在Spring框架4.2以后，`@Import`也支持引用常规的component class，和`AnnotationConfigApplicationContext.register`方法。如果要通过一些配置类作为入口点来显示定义所有组件，从而避免组件扫描，则此功能特别有用。*

**在导入的`@Bean`定义上注入依赖**
前面的示例有效，但是过于简单。在多数特殊场景下，beans在配置类之间相互依赖。当使用XML时，不存在这个问题，因为不涉及编译器，并且，声明`ref="somBean"`并信任Spring在容器初始化期间进行处理。当使用`@Configuration`类时，Java编译器对配置模型施加了约束，因为对其他bean的引用必须是有效的Java语法。

幸好，解决这个问题很简单。和之前已经讨论的一样，`@Bean`方法可以有任意数量的参数来描述bean之间的依赖。思考下面的例子，更多`@Configuration`类的实际场景，每个都依赖于其他声明的bean:
```
@Configuration
public class ServiceConfig {

    @Bean
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

这里有另外的方法来达到相同的结果。记住`@Configuration`类最终只是容易的另一个bean：这意味着他们可以利用`@Autowired`和`@Value`注入，并且与任何其他bean相同的相同的其他功能。

*确保以这种方式注入的依赖项只是最简单的一种。`@Configuration`类在上下文的初始化过程中很早就被处理了，强制以这种方式注入依赖项可能会导致意外的早期初始化。如上所示，尽可能使用基于参数的注入。*

*另外，通过`@Bean`使用`BeanPostProcessor`和`BeanFactoryPostProcessor`定义时要特别小心。这些应该被声明为`statis @Bean`方法，而不触发其包含的配置类的实例化。另外，`@Autowired`和`@Value`可能不适用于配置类本身，因为可以早于AutowiredAnnotationBeanPostProcessor将其创建为Bean实例。*

下面的例子展示了一个bean如何被自动装配到另一个bean:
```
@Configuration
public class ServiceConfig {

    @Autowired
    private AccountRepository accountRepository;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    private final DataSource dataSource;

    public RepositoryConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

*在Spring4.3中，`@Configuration`类才支持构造器注入。还要注意，如果目标bean仅定义一个构造函数，则无需指定@Autowired。*

**易于导航的全限定导入bean**
在上述的场景中，使用`@Autowired`效果很好，并提供了所需的模块化，但是确定自动装配bean定义的确切位置仍然有些模棱两可。例如，作为`ServiceConfig`的开发人员，如何确切知道`@Autowired AccountRepository`的声明位置？它在代码中不明确。请记住，Spring Tools for Eclipse提供可以展示每个自动装配的渲染图。另外，Java IDE可以轻松的找到`AcountRepository`类的声明和使用，并快速显示返回该类型的`@Bean`方法的位置。

如果这种歧义是不可接受的，并且您希望从IDE内部直接从一个@Configuration类导航到另一个@Configuration类，请考虑自动装配配置类本身。以下示例显示了如何执行此操作：

```
@Configuration
public class ServiceConfig {

    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        // navigate 'through' the config class to the @Bean method!
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}
```  

在前面的情况下，完全定义了`AccountRepository`。但是，`ServiceConfig`和`RepositoryConfig`紧密耦合。这是权衡后的结果。通过使用基于接口或基于抽象类的`@Configuration`类，可以在某种程度上缓解这种紧密耦合：
```
@Configuration
public class ServiceConfig {

    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}

@Configuration
public interface RepositoryConfig {

    @Bean
    AccountRepository accountRepository();
}

@Configuration
public class DefaultRepositoryConfig implements RepositoryConfig {

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(...);
    }
}

@Configuration
@Import({ServiceConfig.class, DefaultRepositoryConfig.class})  // import the concrete config!
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return DataSource
    }

}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

现在，`ServiceConfig`与具体的`DefaultRepositoryConfig`松耦合，并且内置的IDE工具仍然有用：可以轻易获取`RepositoryConfig`实现的类型层次结构。以这种方式，导航@Configuration类及其依赖项与导航基于接口的代码的通常过程没有什么不同。

*如果想要影响某些bean的启动创建顺序，请考虑将一些声明为`@Lazy`（用于首次访问儿不是启动时创建）或像`@DependsOn`在某些其他bean上一样（确保在当前bean创建之前创建特定的其他bean，超出后的直接依赖项所暗示的范围）。*

**有条件的包含`@Configuration`类或`@Bean`方法**

根据某些系统状态，有条件的启用或禁用完整的`@Configuration`类甚至单个`@Bean`方法通常很有用。一个常见的例子是仅在Spring环境中启用特定配置文件时，才使用`@Profile`注解来激活bean。

`@Profile`注解实际上是通过更灵活的`@Conditional`注解来实现的。`@Conditional`注解只是在注册`@Bean`之前询问特定的`org.springframework.context.annotation.Condition`实现。

`Condition`接口的实现，提供了一个`matches(...)`方法并返回`true`或`false`。例如，下面的清淡展示了用于`@Profile`的实际`Condition`实现：
```
@Override
public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    // Read the @Profile annotation attributes
    MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
    if (attrs != null) {
        for (Object value : attrs.get("value")) {
            if (context.getEnvironment().acceptsProfiles(((String[]) value))) {
                return true;
            }
        }
        return false;
    }
    return true;
}
```

**结合Java和XML配置**
Spring的`@Configuration`类并非旨在100%完全代替Spring XML。某些工具（例如Spring XML名称空间）仍然是配置容器的理想方法。在方便或需要XML的情况下，您可以选择：例如，通过使用“以XML为中心”的方式实例化容器，`ClassPathXmlApplicationContext`或通过使用`AnnotationConfigApplicationContext`和`@ImportResource`注解来导入需要的XML。

**以XML为中心，使用`@Configuration`类**

最好从XML引导Spring容器，并以即席方式包括`@Configuration`类。例如，在使用Spring XML的大型现有代码库中，根据需要创建@Configuration类并从现有XML文件中将它们包含在内会变得更加容易。在本节的后面，我们将介绍在这种“以XML为中心”的情况下使用@Configuration类的选项。

**将`@Configuration`类声明为纯Spring `<bean />`元素**

记住`@Configuration`类最终是容器中的bean定义。在一系列例子中，创建一个名字为`AppConfig`的`@Configuration`类，并将其作为`<bean/>`定义包含在`system-test-config.xml中。`因为`<context：annotation-config/>`被开启，因此容器可以识别`@Configuration`注释并正确处理`AppConfig`中声明的`@Bean`方法:
```
@Configuration
public class AppConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(dataSource);
    }

    @Bean
    public TransferService transferService() {
        return new TransferService(accountRepository());
    }
}
```

```
<beans>
    <!-- enable processing of annotations such as @Autowired and @Configuration -->
    <context:annotation-config/>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

    <bean class="com.acme.AppConfig"/>

    <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
</beans>

jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
jdbc.username=sa
jdbc.password=

public static void main(String[] args) {
    ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/com/acme/system-test-config.xml");
    TransferService transferService = ctx.getBean(TransferService.class);
    // ...
}
```

*在`system-test-config.xml`文件中，`AppConfig <bean/>`没有声明`id`元素。然而，尽管这是可以被接受的，但是由于没有其他bean引用过它，因此不必要加上id，也不太可能通过名称从容器中显示获取。同样，`DataSource` bean只能按类型自动装配，因此也不严格要求显示的bean id。*


**使用<context:component-scan/>来拾取`@Configuration`类**

因为`@Configuration`是使用`@Component`进行元注解的，`@Configuration`注解类会自动的成为组件扫描候选者。像之前描述的情景一样，可以重新定义`system-test-config.xml`来进行组件扫描。注意，在这个例子中，不需要明确声明`<context:annotation-config/>，因为<context:component-scan/>`开启了相同的功能。

```
<beans>
    <!-- picks up and registers AppConfig as a bean definition -->
    <context:component-scan base-package="com.acme"/>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

    <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
</beans>
```

**在`@Configuration`类中使用`@ImportResource`导入XML**

在使用`@Configuration`类作为主要的容器配置机制中，可能仍然需要少量的XML配置。在那些场景中，可以使用`@ImportResource`并且定义需要的XML。使用这种方式来完成容器配置，可以保持最低限度的XML。下面的例子（包含configuration类，一个XML文件定义bean，一个properties文件和一个`main`类）展示了如何使用`@ImportResource`注解来完成以Java配置为中心来使用XML：
```
@Configuration
@ImportResource("classpath:/com/acme/properties-config.xml")
public class AppConfig {

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource(url, username, password);
    }
}

properties-config.xml
<beans>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>
</beans>

jdbc.properties
jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
jdbc.username=sa
jdbc.password=

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    TransferService transferService = ctx.getBean(TransferService.class);
    // ...
}
```

## 1.13. 抽象化的环境

`Environment`接口是继承在容器中的抽象形式，可以对应用程序环境的两个关键方面进行建模：`profiles`和`prorerties`。

一个profile是一个被命名的，具有逻辑分组的bean定义，如果提供的profile被激活，那么用它来注册到容器中。可以将bean分配给prifile，无论是以XML定义还是带有注解的定义。`Environment`对象的角色和profiles相关，用来觉得那些profiles是当前激活的，并且哪些是默认激活的。

Properties在大多数应用程序中扮演了重要的角色，它可能有多种来源：properties文件，JVM 系统properties，系统环境变量，JNDI，servlet上下文参数，可插拔的`Properties`对象，`Map`对象等等。环境对象与属性相关的作用是为用户提供方便的服务接口，用于配置属性源并从中解析属性。

### 1.13.1. Bean Definition Profiles

Bean Definition Profiles在核心容器内提供了一种机制，允许为不同环境中的不同bean定义提供注册。”environment“这个词，对不同的用户有不同的意义，并且这种特性对多数用例具有帮助性：

* 在开发中针对内存中的数据源进行工作，而不是在进行QA或生产时从JNDI查找相同的数据源。
* 仅在将应用程序部署到性能环境中时注册监视基础结构。
* 为客户A和客户B部署注册bean的自定义实现。

思考第一个用例，应用程序需要一个`DataSource`。在测试环境中，配置可能像下面一样组合：
```
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("my-schema.sql")
        .addScript("my-test-data.sql")
        .build();
}
```

现在，思考应用程序如何在QA或产品环境中部署，假设应用程序的数据源是通过在产品应用服务的JNDI目录上注册。`datasource` bean 现在看起来像下面的清单：
```
@Bean(destroyMethod="")
public DataSource dataSource() throws Exception {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
}
```

问题是，如何在这两种情况下切换配置？随着时间的推移，Spring用户已经设计出多种方法来完成这个任务，通常依赖于系统环境变量和包含`@{placeholder}`标记的XML`<import/>`语句组合，这些标记根据环境变量的值为正确的配置解析数据源。Bean Definition Profiles是一项容器核心功能，可以为此提供解决方案。

如果我们概括前面特定于环境的Bean定义示例中所示的用例，则最终需要在某些上下文中而不是在其他上下文中注册某些Bean定义。您可能会说您要在情况A中注册一个特定的bean定义配置文件，在情况B中注册一个不同的配置文件。我们首先更新配置以反映这种需求。

使用`@Profile`

`@Profile`注解可以指示一个或多个指定的配置文件处于激活状态时才有资格注册组件。使用前面的例子，可以重写`dataSource`配置：
```
@Configuration
@Profile("development")
public class StandaloneDataConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }
}
```

```
@Configuration
@Profile("production")
public class JndiDataConfig {

    @Bean(destroyMethod="")
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```

*之前提过，对于`@Bean`方法，通常编程JNDI查找，方法是使用Spring的JndiTemplate/JndiLocatorDetegate或前面的JNDI InitialContext用法，而不是JndiObjectFactory变体，这将迫使返回类型为FactoryBean的类型。*

profile可以包含一个简单的profile名称（例如，`production`）或一个profile表达式。一个profile表达式允许更复杂的profile逻辑来表示（例如，`production & us-east`）。下面是在profile中支持的表达式操作符：

* `!`:逻辑”非“含义
* `&`:逻辑”与“含义
* `|`:逻辑"或"含义

*不能在没有括号时，混合使用`&`和`|`操作符。例如`production & us-east | eu-central`是一个无效的表达式。正确的形式为`production & (us-east | eu-central)`。*

可以使用`@Profile`作为元注解，来创建自定义组合的注解。下面的例子定义了一个自定义的`@Production`注解，用来代替`@Profile("production")`:
```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("production")
public @interface Production {
}
```

*如果一个`@Configuration`类使用`@Profile`标记，除非一个或多个指定的配置文件处于活动状态，否则与该类关联的`@Bean`方法和`@Import`注解注解都会被绕过。如果一个`@Component`或`@Configuration`类被标记为`@Profile({"p1","p2"})`,除非profile `p1`或`p2`被激活，否则这个类不会被注册或处理。如果给定的配置文件以NOT运算符(!)为前缀，则仅在该配置文件为被激活时才注册带有注解的元素。例如，`@Profile({"p1","!p2"})`，如果profile `p1`被激活或`p2`没有被激活，才回发生注册。*

`@Profile`也可以在方法级别上声明为仅包含配置类的一个特定bean:
```
@Configuration
public class AppConfig {

    @Bean("dataSource")
    @Profile("development") 
    public DataSource standaloneDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }

    @Bean("dataSource")
    @Profile("production") 
    public DataSource jndiDataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```

* `standaloneDataSource`方法尽在`development`profile可用时有效
* `jndiDataSource`方法尽在`production`profile可用时有效

*通过@Bean方法上的@Profile，可能会出现特殊情况：对于具有相同Java方法名称的@Bean方法重载（类似于构造函数重载），需要在所有重载方法上声明`@Profile`条件。如果条件不一致，则仅重载方法中第一个声明的条件很重要。因此，`@Profile`不能用于选择具有另一个参数签名的重载方法。在创建时，同一个bean的所有工厂方法之间的解析都遵循Spring的构造函数解析算法。*

*如果想要定义不同profile条件的备用bean，通过使用`@Bean`的name属性，使用指向相同bean名称的不同Java方法名称，向前面的例子一样。如果参数签名都一样（例如，所有变体都具有无参工厂方法），则首先在有效Java类中表示这种排列的唯一方法。*

**XML Bean Definition Profiles**
在XML中，对应的是`<beans>`的`profile`属性。前面的示例配置可以用两个XML文件重写，如下所示：
```
<beans profile="development"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xsi:schemaLocation="...">

    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
</beans>
```

```
<beans profile="production"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
</beans>
```

为了避免拆分和嵌套`<beans>`元素，可采用如下示例：
```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <!-- other bean definitions -->

    <beans profile="development">
        <jdbc:embedded-database id="dataSource">
            <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
            <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
        </jdbc:embedded-database>
    </beans>

    <beans profile="production">
        <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    </beans>
</beans>
```

spring-bean.xsd已被限制为仅允许这些元素作为文件中的最后一个元素。这应该有助于提供灵活性，而不会引起XML文件混乱。

*XML对应项不支持前面描述的配置文件表达式。但是，可以使用！取消配置文件。操作员。也可以通过嵌套配置文件来应用逻辑“和”，如以下示例所示：*

```
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <!-- other bean definitions -->

    <beans profile="production">
        <beans profile="us-east">
            <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
        </beans>
    </beans>
</beans>
```
*在前面的例子中，如果`production`和`us-east`都被激活，`dataSource` bean才回被显示。*

**激活Profile**

现在更新配置，并且仍然需要指示Spring哪个profile被激活。如果现在立即开始简单的应用程序，可能会看到`NoSuchBeanDefinitionException`抛出，因为容器不能找到一个名字为`dataSource`的bean。

有多种途径激活profile。但是大多数最直接的方法是通过`ApplicationContext`获得的`Environment` API以编程方式进行。

```
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
ctx.getEnvironment().setActiveProfiles("development");
ctx.register(SomeConfig.class, StandaloneDataConfig.class, JndiDataConfig.class);
ctx.refresh();
```

除此之外，可以通过spring.profiles.active属性声明激活的profile，可以通过系统环境变量，JVM系统属性，`web.xml`中的servlet上下文参数甚至JNDI中的实体。集成测试中，在`spring-test`模块中可以使用`@ActiveProfiles`注解来激活。

注意，profiles不是非此即彼的命题。可以一次激活多个profiles，可以给`setActiveProfiles()`提供多个profile名字，它可以接受`String...`参数。
```
ctx.getEnvironment().setActiveProfiles("profile1", "profile2");
```

`spring.profiles.active`可以接受逗号分割的profile列表名称：
```
-Dspring.profiles.active="profile1,profile2"
```

**默认Profile**

默认profile表示该profile默认开启。思考下面的例子：
```
@Configuration
@Profile("default")
public class DefaultDataConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .build();
    }
}
```

如果没有profile激活，该`dataSource`会被创建。可以参考这种方式，提供为一个或更多的bean提供默认定义。如果有任何profile被开启，那么默认的profile就不会被应用。

您可以通过在`Environment`上使用`setDefaultProfiles（）`或通过声明使用`spring.profiles.default`属性来声明默认配置文件的名称。

### 1.13.2. `PropertySource`抽象

Spring的`Environment`抽象提供了可配置属性源层次结构上的搜索操作。
```
ApplicationContext ctx = new GenericApplicationContext();
Environment env = ctx.getEnvironment();
boolean containsMyProperty = env.containsProperty("my-property");
System.out.println("Does my environment contain the 'my-property' property? " + containsMyProperty);
```

在前面的片段中，看到了一种询问Spring是否为当前环境定义`my-property`属性的高级方法。为了回答这个问题，该`Enviroment`对象在一组`PropertySource`对象上进行搜索。一个`PropertySource`是一个key-value对的抽象，Spring的`StandardEnvironment`通过两个`PropertySource`对象来配置-一个代表一组JVM系统属性(System.getProperties())，一个代表一组系统环境变量属性(System.getenv())。

*这些默认的属性源存在于`StandardEnvironment`，在单应用中使用。`StandardEnvironment`填充了其他默认属性源，包括servlet配置和servlet上下文参数。它可以选择启用`JndiPropertySource`。*

具体来说，当使用`StandardEnvironment`时，如果`my-property`系统属性或`my-property`环境变量在运行时存在，调用`env.containsProperty("my-property")`会返回true。

*检索的执行是有层次结构的。默认情况下，系统属性优先于环境变量。所以，如果`my-property`属性在调用`env.getProperty("my-property")`时，两个地方都设置了值，那么系统属性值会优先返回。注意，这个属性值不会被合并，而是被前面的条目完全覆盖。*

*对于常用的`StandardServletEnvironment`，完整的层次机构如下，最高优先级条目位于最顶部：*
1. ServletConfig 参数(如果适用，例如在`DispatcherServlet`上下文的情况)
2. ServletContext参数(web.xml的context-param entries)
3. JNDI环境变量(`java:comp/env/` entries)
4. JVM系统属性(`-D`命令行参数)
5. JVM系统环境变量（操作系统环境变量）

最终要的是，整个机制是可配置的。可能有一个自定义的属性来源想要集成到搜索中。为了达到这个目的，需要实现和实例化自定义的`PropertySource`并且把他添加到当前`Environment`中的一组`PropertySourcds`中。
```
ConfigurableApplicationContext ctx = new GenericApplicationContext();
MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
sources.addFirst(new MyPropertySource());
```

在前面的代码中，`MyPropertySource`已经在搜索中被加入了最高的优先级。如果它包含一个`my-property`属性，这个属性就会被侦测和返回，从而支持任何其他`PropertySource`中的`my-property`属性。`MutablePropertySources`的API暴露了一些允许精确操作该组属性来源的方法。

### 1.13.3. 使用`@PropertySource`

对于增加一个`PropertySource`到Spring的`Environment`中，`@PropertySource`注解提供一种方便的可声明的机制。

给定一个名为`app.properties`的文件，其中包含`testbean.name=myTestBean`的key-value对，下面的`@Configuration`类使用`@PropertySource`这种方式来调用`testBean.getName()`，并返回`myTestBean`：

```
@Configuration
@PropertySource("classpath:/com/myco/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```

`PropertySource`资源位置存在的任何`${}`占位符都是根据该环境注册的一组属性源来解析的：
```
@Configuration
@PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```

假设`my.placeholder`代表已经注册的一个属性源（例如，系统属性或环境变量），这个占位符会被正确解析。否则，`default/path`会被默认使用。如果没有默认定义或属性不能被解析，会跑出`IllegalArgumentException`异常。

*根据Java8约定，`@PropertySource`注解可以重复。但是，所有的`@PropertySource`注解必须在同一个级别声明，可以直接在配置类上声明，也可以在同一自定义注解中声明为元注解。不建议将直接注解和元注解混合使用，因为直接注解会有效覆盖元注解。*

### 1.13.4. 声明中的占位符解析

从历史上看，元素占位符的值只能根据JVM系统属性或环境变量来解析。这种情况已经不存在了。因为`Environment`抽象是在整个容器中集成的，因此很容易通过它来路由占位符的解析。这意味着可以按照自己喜欢的任何方式来配置解析过程。可以改变搜索系统属性和环境变量的优先级，也可以完全删除他们。还可以根据需要将自己的属性源添加到组合中。

具体而言，一下语句无论在何处定义`customer`属性都有效，主要改属性在环境变量中可用：
```
<beans>
    <import resource="com/bank/service/${customer}-config.xml"/>
</beans>
```

## 1.14. 注册一个`LoadTimeWeaver`

`LoadTimeWeaver`是Spring用来将类加载到Java虚拟机时对其进行动态转换。

为了开启载入是织入，可以在一个`@Configuration`类上增加`@EnableLoadTimeWeaving`：
```
@Configuration
@EnableLoadTimeWeaving
public class AppConfig {
}
```

对于XML，还可以使用`context:load-time-weaver`元素:
```
<beans>
    <context:load-time-weaver/>
</beans>
```

为`ApplicationContext`配置后，该`ApplicationContext`中的任何bean都可以实现`LoadTimeWeaverAware`，从而接受对加载时weaver实例的引用。与Spring的JPA支持结合使用时，该功能特别有用，因为在进行JAP类转换时，可能需要加载时织入。有关更多详细信息，请查阅`LocalContainerEntityManagerFactoryBean` javadoc。有关AspectJ加载时编织的更多信息，请参见Spring框架中的AspectJ加载时编织。


## 1.15. `ApplicationContext`的其他功能

在介绍章节中说过，`org.springframework.beans.factory`包提供了管理和操作bean的基本功能，也包括以编程方式来管理和操作bean。`org.springframework.context`包增加了`ApplicationContext`接口，它扩展了`BeanFatory`接口，此外还扩展了其他接口，以提供应用程序框架方式的附加功能。许多人以完全声明的方式使用`ApplicationContext`，甚至没有以编程方式创建它，而是依靠诸如`Contextloader`之类的支持类来自动实例化`ApplicationContext`，作为Java EE Web应用程序正常启动过程的一部分。

为了以更加面向框架的方式增强`BeanFactory`的功能，上下文包还提供了一下功能：
* 通过`MessageSource`接口获取资源国际化的消息
* 通过`ResourceLoader`接口，获取资源，例如URLs和文件
* 通过`ApplicationEventPublisher`接口，将事件发布到实现`ApplicationListener`接口的bean。
* 通过`HierarchicalBeanFactory`接口，载入多个（就有层级关系的）context，让每个上下文只关注一个特定的层，例如web层应用。

### 1.15.1. 使用`MessageSource`进行国际化

`ApplicationContext`接口扩展了`MessageSource`,因此，提供了国际化的功能。Spring也提供了`HierarchicalMessageSource`接口，可以提供分层解析消息。这些接口一起提供了Spring影响消息解析的基础。这些接口定义的方法包括：

* `String getMessage(String code,Object[] args,String default,Locale loc)`：用来从`MessageSource`中获取消息的基本方法。如果找不到指定语言环境的消息时，会使用默认的消息。使用标准库提供的`MessageFormat`功能，传入的所有参数都将成为替换值。
* `String getMessage(String code,Object[] args,Locale loc)`:本质上来说和前面只有一个不同：没有指定的默认消息。如果消息没有找到，一个`NoSuchMessageException`会抛出。
* `String getMessage(MessageSourceResolvable resolvable,Locale locale)`:所有前述方法中使用的属性也都包装在名为`MessageSourceResolvable`的类中，可以将其与此方法一直使用。

当`ApplicationContext`加载后，它会在上下文中自动查找一个`MessageSource`的bean 定义。这个bean必须命名为`messageSource`。如果这样的bean被找到，对前面方法的所有调用都委托给消息源。如果没有消息源，`ApplicationContext`尝试查找包含同名bean的父对象。如果是这样，它将使用该bean作为`MessageSource`。如果没有找到任何消息源，一个空的`DelegatingMessageSource`会实例化，以便能够接受对上面定义的方法的调用。

Spring提供了两个`MessageSource`实现，`ResourceBundleMessageSource`和`StaticMessageSource`。他们都实现了`HirrarchicalMessageSource`以便进行嵌套消息传递。`StaticMessageSource`很少使用但是提供了可编程的方式来增加消息源。下面的例子展示了`ResourceBundleMessageSource`：

```
<beans>
    <bean id="messageSource"
            class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basenames">
            <list>
                <value>format</value>
                <value>exceptions</value>
                <value>windows</value>
            </list>
        </property>
    </bean>
</beans>
```

这个例子假设有3个资源包，分别是`format`，`exceptions`，`windows`，他们被定义在classpath下。解析消息的任何请求都通过JDK标准的`ResourceBundle`对象解析消息来处理。就本示例而言，假设上述两个资源包文件的内容如下：
```
# in format.properties
message=Alligators rock!
```

```
# in exceptions.properties
argument.required=The {0} argument is required.
```

接下来展示了一个运行`MessageSource`功能的程序。记住，所有`ApplicationContext`的实现也实现了`MessageSource`,所以可以转换为`MessageSource`接口。
```
public static void main(String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("message", null, "Default", Locale.ENGLISH);
    System.out.println(message);
}
```

上面的程序的输出结果是：
```
Alligators rock!
```

总结一下，`MessageSource`定义在了`beans.xml`的文件中，它在classpath的根路径下。`messageSource`定义通过其`basenames`属性引用了许多资源包。列表中传递给basenames属性的三个文件在类路径的根目录下以文件形式存在，分别称为`format.properties``exceptions.properties`和`windows.properties`。

下面的例子展示了传递给消息查找的参数。这些参数将转换为String对象，并插入到查找消息中的占位符中。
```
<beans>

    <!-- this MessageSource is being used in a web application -->
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basename" value="exceptions"/>
    </bean>

    <!-- lets inject the above MessageSource into this POJO -->
    <bean id="example" class="com.something.Example">
        <property name="messages" ref="messageSource"/>
    </bean>

</beans>
```

```
public class Example {

    private MessageSource messages;

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public void execute() {
        String message = this.messages.getMessage("argument.required",
            new Object [] {"userDao"}, "Required", Locale.ENGLISH);
        System.out.println(message);
    }
}
```

调用`execute()`方法的输出结果是：
```
The userDao argument is required.
```

关于国际化（”i18n“），Spring的各种`MessageSource`实现遵循与JDK`ResourceBundle`相同的语言环境解析和后备规则。简而言之，继续前面定义的示例，如果要针对英国(en-GB)语言环境解析消息，则将分别创建名为：`format_en_GB.properties`，`exceptions_en_GB.properties`
和`windows_en_GB.properties`的文件。

通常，语言环境解析由应用程序的周围环境管理。在下面的例子中，手动指定了针对其解析（英国）消息的语言环境：
```
# in exceptions_en_GB.properties
argument.required=Ebagum lad, the ''{0}'' argument is required, I say, required.
```

```
public static void main(final String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("argument.required",
        new Object [] {"userDao"}, "Required", Locale.UK);
    System.out.println(message);
}
```

上面程序的输出结果是：
```
Ebagum lad, the 'userDao' argument is required, I say, required.
```

也可以使用`MessageSourceAware`接口来请求任何`MessageSource`的引用。任何实现`MessageSourceAware`接口的bean定义都会在`ApplicationContext`的`MessageSource`一起注入。

*作为`ResourceBundleMessageSource的另一个代理方案，Spring提供一个`ReloadableResourceBundleMessageSource`类。它支持相同的资源包格式但是比基于JDK的`ResourceBundleMessageSource`实现更有弹性。特别的，它允许读取任何位置的文件（不仅仅是从classpath），并且支持重新加载资源包属性文件（同事在他们之间进行高效缓存）。*

### 1.15.2. 标准的和自定义事件

`ApplicationContext`中通过`ApplicationEvent`类和`ApplicationListener`接口来处理事件。如果在上下文中部署了实现`ApplicationListener`接口的bean，每次`ApplicationEvent`发布到`ApplicationContex`时，都会通知该bean。本质上，这是标准的观察者模式。

*Spring4.2以后，事件基础设施已经有了重要的提升，并且提供一个很好的注解模型能力来发布任何事件（也就是说，一个对象不再需要从`ApplicationEvent`上扩展）。当这样的对象被发布后，spring把它包装成一个事件来发布。*

下面的表格描述了Spirng提供的标准事件：

|事件|解释|
|---|---|
|`ContextRefreshedEvent`|当`ApplicationContext`被初始化或刷新（例如，通过`ConfigurableApplicationContext`接口使用`refresh()`方法）来发布。这里，初始化意味着所有bean被加载，post-processor beans被侦测到并且被激活，单例被预先实例化，并且`ApplicationContext`对象已经准备使用。只要context没有关闭，刷新可以触发多次，前提是所选的`ApplicationContext`实际上支持热刷新。例如，`XmlWebApplicationContext`支持热刷新，但是`GenericApplicationContext`不支持。|
|`ContextStartedEvent`|使用`ConfigurableApplicationContext`接口上的`start()`方法启动`ApplicationContext`时发布。这里，开始意味着所有的`Lifecycle`bean收到了一个明确的启动信号。通常，这个信号用来在明确的停止后重启bean，但是，它可能被用来启动那些没有配置自动启动的组件（例如，上位在初始化时启动的组件）。|
|`ContextStoppedEvent`|当调用`ConfigurableApplicationContext`接口上的`stop()`方法来关闭`ApplicationContext`时发布。这里，停止意味着所有`Lifecycle`的bean收到一个明确的停止信号。一个停止了的上下文可能通过`start()`调用来重启。
|`ContextClosedEvent`|当调用`ConfigurableApplicationContext`接口上的`close()`方法或一个JVM shutdown hook来关闭`ApplicationContext`时发布。这里，关闭意味着所有单例bean会被销毁。关闭上下文后，它将达到使用寿命并且无法刷新或重启。|
|`RequestHandledEvent`|一个特定的web事件，告诉所有bean HTTP请求已经得到服务。这个事件在请求完成后发布。这个事件只适用于那些使用了Spring的`DispatcherServlet`的web应用。|
|`ServletRequestHandledEvent`|`RequestHandledEvent`的子类，增加了特定的Servlet上下文信息。|

可以创建和发布自定义的事件。下面的例子展示了一个简单的类，它扩展自`ApplicationEvent`基类：
```
public class BlockedListEvent extends ApplicationEvent {

    private final String address;
    private final String content;

    public BlockedListEvent(Object source, String address, String content) {
        super(source);
        this.address = address;
        this.content = content;
    }

    // accessor and other methods...
}
```

为了发布自定义的`ApplicationEvent`，需要调用`ApplicationEventPublisher`上的`publishEvent()`方法。通常，在创建了一个实现了`ApplicationEventPublisherAware`的类并且把它作为Spring bean来注册后完成。下面的例子展示了这样的类：
```
public class EmailService implements ApplicationEventPublisherAware {

    private List<String> blockedList;
    private ApplicationEventPublisher publisher;

    public void setBlockedList(List<String> blockedList) {
        this.blockedList = blockedList;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void sendEmail(String address, String content) {
        if (blockedList.contains(address)) {
            publisher.publishEvent(new BlockedListEvent(this, address, content));
            return;
        }
        // send email...
    }
}
```

在配置时，Spring容器检测到`EmailService`实现了`ApplicationEventPublisherAware`并且自动调用`setApplicationEventPublisher()`。事实上，传入的参数是Spring
容器本身。用户可以通过`ApplicationEventPublisher`接口进行应用上下文的交互。

为了接收到自定义的`ApplicationEvent`，可以创建一个实现了`ApplicationListener`并且注册到Spring的bean。接下来的例子展示了这样的类:
```
public class BlockedListNotifier implements ApplicationListener<BlockedListEvent> {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    public void onApplicationEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```

注意`ApplicationListener`是通常使用自定义时间的类型进行参数化（上一个示例中的`BlockedListEvent`）。这意味着`onApplicationEvent()`方法可以保持类型安全，避免任何向下转换的需要。可以注册多个监听器，但是注意，默认情况下，时间监听器收到的时间是同步的。这意味着`publishEvent()`方法当所有坚挺着完成处理时间请求之前是阻塞的。这种同步和单线程的优点是，当监听器收到一个事件，如果有可用的事务上下文，它将在发布者的事务上下文中进行操作。如果需要其他的事件发布策略，可以参考Spring的`ApplicationEventMulticaster`接口和` SimpleApplicationEventMulticaster`实现来配置可选项。

下面的例子展示了定义bean来注册和配置上面的每个类：
```
<bean id="emailService" class="example.EmailService">
    <property name="blockedList">
        <list>
            <value>known.spammer@example.org</value>
            <value>known.hacker@example.org</value>
            <value>john.doe@example.org</value>
        </list>
    </property>
</bean>

<bean id="blockedListNotifier" class="example.BlockedListNotifier">
    <property name="notificationAddress" value="blockedlist@example.org"/>
</bean>
```

将所有内容放在一起，当调用`emailService`中的`sendMail()`方法时，如果有任何阻塞的email消息，则发布`BlockedListEvent`类型的自定义事件。这个`blockedListNotifier`bean作为`ApplicationListener`注册并且接收`BlockedListEvent`，此时它可以通知适当的参与者。

*Spring事件机制被设计为简单链接同一个应用上下文的Spring beans。然而，对于更复杂的企业集成需要，单独维护的Spring Integration项目为基于Spring编程模型构建轻量级的，面向模式的事件驱动架构提供了完整的支持。*

**基于注解的事件监听者**

在Spring4.2以后，可以在任何被管理的bean的public方法上使用`@EventListener`注解来注册一个事件监听者。`BlockedListNotifier`可以像下面的例子一样被重写：
```
public class BlockedListNotifier {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    @EventListener
    public void processBlockedListEvent(BlockedListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```

这个方法签名再次声明了需要监听的事件类型，但是，这次，它没有实现特定的监听者接口并且有灵活的名称。只要实际事件类型在其实现层次结构中解析为泛型参数，也可以通过泛型类型来缩小事件类型。

如果方法需要监听一些事件或如果想要定义完全没有参数的事件，事件类型可以被定义在注解本身上。下面展示了这样的例子：
```
@EventListener({ContextStartedEvent.class, ContextRefreshedEvent.class})
public void handleContextStart() {
    // ...
}
```

通过使用注解的`condition`属性，定义一个`SpEL表达式`，也可以增加额外的运行时过滤，该注解应匹配以针对特定事件实际调用该方法。

接下来的例子展示了监听者如何被重写并被调用，只要事件的属性`content`和`my-event`相等：
```
@EventListener(condition = "#blEvent.content == 'my-event'")
public void processBlockedListEvent(BlockedListEvent blockedListEvent) {
    // notify appropriate parties via notificationAddress...
}
```

每个SpEL表达式都会根据专用上下文进行评估。下面的表格列出了上下文可用的条目以便可以对条件事件的处理进行使用：

|Name|Location|Description|Example|
|---|---|---|---|
|Event|root object|实际的`ApplicationEvent`|`#root.event`或`event`|
|Arguments array|root object|参数（作为一个对象数组）用来调用方法|`#root.args`或`args`；`args[0]`来访问第一个参数等等。|
|Argument name|evaluation context|任何方法参数的名字。如果由于某种原因这些名称不可用（例如，因为因为在已编译的字节码中没有调试信息），也可以使用#a <#arg>语法使用单个参数，其中<#arg>代表参数索引（从0开始）。|`#blEvent`或`＃a0`（您也可以使用`＃p0`或`#p <#arg>`参数符号作为别名）|

请注意，即使方法签名实际上引用了已发布的任意对象，＃root.event也使您可以访问基础事件。

如果由于处理另一个事件而需要发布一个事件，则可以更改方法签名以返回应发布的事件，如以下示例所示：
```
@EventListener
public ListUpdateEvent handleBlockedListEvent(BlockedListEvent event) {
    // notify appropriate parties via notificationAddress and
    // then publish a ListUpdateEvent...
}
```

*这个特性在异步监听器中不支持*

这个新的方法为上述每个`BlockedListEvent`发布了一个新的`ListUpdateEvent`。如果需要发布一些事件，可以返回一个事件的`Collection`类型来代替。

**异步监听者**

如果想要一个特殊的监听者来处理异步事件，可以重用`@Async`。下面的例子展示了如何做：
```
@EventListener
@Async
public void processBlockedListEvent(BlockedListEvent event) {
    // BlockedListEvent is processed in a separate thread
}
```

使用异步事件，需要认识到下列的限制：
* 如果异步事件监听者抛出一个`Exception`，它不会传播给调用者。可以参考`AsyncUncaughtExceptionHandler`的更多细节。
* 异步事件监听者方法无法通过返回值来发布后续事件。如果需要将处理结果作为另一个事件发布，需要注入一个`ApplicationEventPublisher`来手动发布事件。

**监听者排序**
如果需要在另一个监听者之前调用一个监听者，可以在方法声明上增加`@Order`注解：
```
@EventListener
@Order(42)
public void processBlockedListEvent(BlockedListEvent event) {
    // notify appropriate parties via notificationAddress...
}
```

**泛型事件**

可以使用泛型来进一步定义事件的结构。思考使用一个`EntityCreatedEvent<T>`，其中，`T`是已创建的实例实体的类型。例如，可以创建如下的监听者来定义只接受`Person`的`EntityCreatedEvent`泛型。
```
@EventListener
public void onPersonCreated(EntityCreatedEvent<Person> event) {
    // ...
}
```

由于类型擦除，仅当触发的事件解析了事件监听器用来过滤的泛型参数（即类似`class PersonCreatedEvent extends EntityCreatedEvent<Person>{...}`）才生效。

在某些情况下，如果所有事件都遵循相同的结构，这可能会变得很乏味（就像前面示例中的事件一样）。在这些例子中，可以实现`ResolvableTypeProvider`来引导框架，使其超出运行时环境提供的范围:
```
public class EntityCreatedEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    public EntityCreatedEvent(T entity) {
        super(entity);
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getSource()));
    }
}
```

*这不仅适用于`ApplicationEvent`,而且适用于为事件发送的任何对象。*

### 1.15.3. 方便地访问底层资源

为了获得最佳用法和对应用程序上下文的理解，应该熟悉Spring的`Resource`抽象。

一个应用程序上下文是一个`ResourceLoader`，可以用来加载`Resource`对象。一个`Resource`本质上是JDK`java.net.URL`类的功能更丰富的版本。实际上，`Resource`的实现包装了一个合适的`java.net.URL`的实例。一个`Resource`几乎可以透明的从任何位置获取低级别资源，包括从classpath，文件系统，任何被标准URL描述的位置或其他途径。如果资源位置是简单的路径字符串并且没有任何前缀，则这些资源的来源是特定的并且适合于实际的应用程序上下文类型。

可以在应用程序上下文中配置一个实现了特殊回调接口的bean，`ResourceLoaderAware`，将在初始化时自动回调，而应用程序上下文本身作为`ResourceLoader`传入。还可以公开`Resource`类型的属性，用于访问静态资源。他们像其他属性一样注入其中。可以将那些`Resource`属性指定为简单的`String`路径，并在部署bean时，依靠从这些文本字符串到实际`Resource`对象的自动转换。提供给`ApplicationContext`构造函数的一个或多个位置路径实际上是资源字符串，并且按照特定的上下文实现以简单的形式对其进行适当的处理。例如，`ClassPathXmlApplicationContext`将简单的位置路径视为类路径位置。也可以使用带有特殊前缀的位置路径（资源字符串）来强制从类路径或URL中加载定义，而不管实际的上下文类型如何。

### 1.15.4. Application启动跟踪




