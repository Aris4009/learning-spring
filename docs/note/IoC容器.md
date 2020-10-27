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