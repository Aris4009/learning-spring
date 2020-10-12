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
