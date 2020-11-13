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
