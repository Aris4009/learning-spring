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
