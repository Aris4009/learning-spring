# 5. 使用Spring进行面向切面编程

*注：可以参考`AspectJ`的文档来更好的理解Spring AOP。[AspectJ](https://www.eclipse.org/aspectj/doc/released/progguide/starting.html)*

面向切面编程（AOP）提供了另一种思考程序结构的方式来补充面向对象编程（OOP）。面向对象中模块化的关键单位是类，而在AOP中，模块化单位是一个切面。切面使关注点模块化（例如事务管理），支持跨多种类型和对象的关注点。（这些关注点在AOP术语中通常被成为“横切”关注）。

Spring中一个关键的组件是AOP框架。虽然Spring IoC容器不依赖AOP（意味着如果不想使用AOP，就不需要），AOP对Spring IoC进行了补充，以提供功能非常强大的中间件解决方案。

**具有AspectJ切点的Spring AOP**

*Spring 通过使用基于schema的方式或`@AspectJ 注解样式`提供了简单强大的方式来编写自定义切面。这两种方式提供了完全类型化的通知，并使用了Aspectj切点语言，同时仍使用Spring AOP进行编织。*

*本章要讨论基于schema和基于`@AspectJ`的AOP支持。下一章将讨论底层AOP支持*

在Spring框架中，AOP被用来：

* 提供声明式的企业级服务。此类服务中最重要的是声明式事务管理`declarative transaction management`。

* 让用户实现自定义切面，通过AOP补充对OOP的使用。

*如果您只对通用声明性服务或其他预包装的声明性中间件服务（例如池）感兴趣，则无需直接使用Spring AOP，并且可以跳过本章的大部分内容。*

## 5.1. AOP概念

首先定义一些主要的AOP概念和术语。这些术语不是特定于Spring的。AOP的术语并不特别直观。而且，如果使用Spring自己的术语，将会更加令人困惑。

* 切面(Aspect):横切多个类的模块化关注点。事务管理器是企业级Java应用程序中横切关注的一个很好的例子。在Spring AOP中，切面通过使用常规类（基于schema的方法）或使用`@Aspect`(`@AspectJ`样式)注解来实现的。

* 连接点(Join point):程序执行过程中的一个点，例如方法的执行或异常的处理。在Spring AOP中，一个连接点总是表示一个方法的执行。

* 通知(Advice):切面在特定连接点采取的操作。不同类型的通知包括：**around**,**before**,**after**。包括Spring在内的许多AOP框架，都将通知当做拦截器，并在连接点周围维护一系列拦截器。

* 切点(Pointcut):匹配连接点的谓语。通知连接了一个切点表达式，并且在与该切点匹配的任何连接点处运行（例如，执行具有特定名称的方法）。切点表达式匹配连接点的概念，是AOP的核心，默认情况下Spring使用AspectJ切点表达式语言。

* 引入(Introduction):代表声明其他方法或字段。Spring AOP让用户可以针对任何通知对象，引入新的接口（并且和一个对应的实现）。例如，可以使用引入使bean实现`IsModified`接口。（一个引入在AspectJ社区中被称为inter-type声明）。

* 目标对象(Target object):通过一个或多个切面来通知的对象。也称为“通知对象”。因为Spring AOP是通过使用运行时代理实现的，这个对象通常是代理对象。

* AOP代理(AOP proxy):一个为实现切面约定并通过AOP框架创建的对象（通知方法执行等）。在Spring框架中，一个AOP代理是JDK动态代理或CGLIB代理。

* 织入(Weaving):将切面与其他应用程序类型或对象连接来创建通知的对象。可以在编译时期，加载时期或运行时完成。Spring AOP和其他纯Java AOP框架一样，在运行时期执行织入。

Spring AOP包含如下类型的通知：

* 前置通知(Before advice):在连接点无法阻止执行流前进之前的通知（除非它发生异常）。

* 正常返回的后置通知(After returning advice):在连接点正常完成运行后返回的通知（例如，一个没有抛出异常的返回方法）。

* 抛出异常的后置通知(After throwing advice):在连接点因方法抛出异常而退出后返回的通知。

* 后置(最终)通知(After advice):无论连接点的退出方式是什么，都会运行的通知（正常或异常返回）。

* 环绕通知(Around advice):包围连接点的通知，如方法调用。这是一种非常强大的通知。环绕通知可以在方法调用前后执行自定义行为。它还负责选择是返回连接点还是通过返回其自身的返回值或引发异常来简化建议的方法。

环绕通知是最通用的通知。由于Spring AOP与AspectJ一样，提供了全方位的通知类型，因此建议使用功能最小的通知类型，以实现所需的行为。例如，如果只需要通过一个方法的返回值来更新缓存，最好使用after returning advice而不是around advice，尽管around advice也能完成相同的事情。使用最具体的通知类型可以提供更简单的编程模型，并减少出错的可能性。例如，不需要在用于around advice的连接点上调用`proceed()`方法，因此，调用它不会失败。

所有通知参数是静态类型的，因此可以使用适当类型的通知参数（例如，方法执行的返回值类型），而不是对象数组。

切点匹配连接点的概念是AOP的关键，它与仅提供拦截功能的旧技术有所不同。切点使通知的目标独立于面向对象的层次结构。例如，可以提供声明性事务管理的环绕通知，应用于横切的多个对象（例如服务层中的所有业务操作）的一组方法。

## 5.2. Spring AOP的能力和目标

Spring AOP是纯Java实现的。不需要特殊的编译过程。Spring不需要控制类加载器的层次结构，因此适合在一个servet容器或application服务器上使用。

Spring AOP目前仅支持方法执行的连接点（通知在Spring beans上执行方法）。字段拦截没有实现，虽然增加字段拦截不需要破坏核心Spring AOP的APIs。如果需要通知访问字段和更新连接点，考虑使用诸如AspectJ之类的语言。

Spring AOP的AOP方法与大多数其他AOP框架不同。目的不是提供最完整的AOP实现（尽管Spring AOP相当强大）。相反，其目的是在AOP实现和Spring IoC之间提供紧密的集成，以帮助解决企业应用程序中的常见问题。

因此，例如，Spring框架的AOP功能通常结合Spring IoC容器一起使用。通过使用常规bean定义语法来配置切面（尽管允许强大的“自动代理功能”）。这是和其他AOP实现最关键的不同之处。使用Spring AOP不能轻松或高效地完成某些事情，例如通知非常细粒度的对象（通常是领域对象）。这种情况下，AspectJ是最佳选择。但是，经验告诉我们Spring AOP对于企业级Java应用程序中遇到的大多数问题都有一个绝佳的解决方案。

Spring AOP与AspectJ不是竞争关系的AOP解决方案。相信诸如Spring AOP这样基于代理的AOP框架和成熟的诸如AspectJ这样的框架都是有价值的，并且他们是互补的而不是竞争的。Spring无缝地将Spring AOP和IoC与AspectJ集成在一起，以在基于Spring的一致应用程序架构中支持AOP的所有使用。这种集成不会影响Spring AOP或AOP Alliance API。Spring AOP保持向后兼容。鱼贯Spring AOP API的讨论，请参见下一章。

> Spring框架的核心宗旨之一是非侵入性。这样的想法是，不应该强迫用户将特定于框架的类和接口引入业务或领域模型中。但是，在某些地方，Spring框架确实为用户提供了特定于Spring框架的依赖项引入代码库的选项。提供此类选项的理由是，在某些情况下，以这种方式阅读和编码某些特定功能可能会更容易。但是，Spring框架（几乎）总是为用户提供选择：可以自由地就哪个选项最合适的特定用例或场景做出明知道决定。

> 与本章相关的是选择哪种AOP框架（以及哪种AOP风格）。可以选择AspectJ，Spring AOP或两者都选。也可以选择`@AspectJ`风格的注解方法或Spring XML配置风格的方法。实际上，本章开头介绍`@AspectJ`风格的方法并不是暗示Spring小组和Spring XML配置风格相比，更偏`@AspectJ`。

## 5.3. AOP代理

Spring AOP的AOP代理默认使用基于JDK动态代理。这使得可以代理任何接口（或接口集合）。

Spring AOP也可以使用CGLIB代理。如果需要代理类，就必须使用它。默认情况下，如果一个业务逻辑对象没有实现一个接口，那么就会使用CGLIB。由于对接口而不是对类进行编程是一种好习惯，因此业务类通常实现一个或多个业务接口。在某些情况下（可能极少发生），当需要通知未在接口上声明的方法或需要将代理对象作为具体类型传递给方法时，可以强制使用CGLIB。

掌握Spring AOP是基于代理的这一事实非常重要。有关完全了解此实现细节含义的详细信息，请参阅AOP代理。

## 5.4. @AspectJ支持

`@AspectJ`是一种将切面声明为带有注解的常规Java类。@AspectJ风格是AspectJ项目在AspectJ 5版本中引入的。Spring使用AspectJ提供的用于切点解析和匹配的库来解释与AspectJ 5相同的注释。AOP在运行时仍然是纯Spring AOP，并且不依赖于AspectJ编译器或织入器。

> 使用AspectJ编译器和织入器可以使用完整的AspectJ语言。

### 5.4.1. 开启@AspectJ支持

为了在Spring配置中使用@AspectJ切面，需要开启基于@AspectJ切面的Spring AOP配置支持，并且基于这些切面是否建议对bean进行自动代理。通过自动代理，意味着如果Spring确定一个或多个通知使用bean，它将自动为该bean生成一个代理以拦截方法调用，并确保按需进行通知。

 @Aspect可以支持基于XML的配置或Java风格的配置。在任何一种情况下，都需要确保AspectJ的`aspectjweaver.jar`库在应用程序的classpath下。该库在AspectJ发行版的lib目录下或从Maven Central存储库中都可以获得。

**通过Java配置，开启对@AspectJ的支持**

要通过Java`@Configuration`开启@AspectJ支持，需要增加`@EnableAspectJAutoProxy`注解：

```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

}
```

**通过XML配置，开启对@AspectJ的支持**

要通过基于XML配置开启@AspectJ支持，需要使用`aop:aspectj-autoproxy`元素：

```xml
<aop:aspectj-autoproxy/>
```

这里假设使用基于XML Schema的配置。参考`the AOP schema`来查看如何导入`aop`命名空间的标签。

### 5.4.2. 声明一个切面

通过开启@AspectJ的支持，Spring会自动检测到在应用程序上下文中使用@AspectJ切面（具有`@Aspect`注解）定义的bean，并用于配置Spring AOP。下面展示了一个无用的切面的最小定义：

第一个例子展示了在应用程序上下文中的常规bean定义，该定义指向具有`@Aspect`注解的bean类：

```xml
<bean id="myAspect" class="org.xyz.NotVeryUsefulAspect">
    <!-- configure properties of the aspect here -->
</bean>
```

第二个例子展示了`NotVeryUsefulAspect`类定义，通过使用`org.aspectj.lang.annotation.Aspect`注解：

```java
package org.xyz;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NotVeryUsefulAspect {

}
```

切面（带有`@Aspect`注解的类）可以像其他类一样有方法和字段。他们也可以包含切点，通知和引入声明（类型间声明）。

> **通过组件扫描自动检测切面**
> 
> 可以在Spring XML配置或通过classpath扫描将常规的bean注册为切面。但是，请注意，`@Aspect`注解不足以在classpath中进行自动检测。为了达到这个目的，需要增加一个`@Component`注解（或者一个复合Spring组件扫描程序规定的自定义的样板注解）。

> **其他方面的建议？**
> 
> 在Spring AOP中，切面本身不能成为其他切面的通知目标。一个类上的`@Aspect`注解标记了它是一个切面，因此，将其从自动代理中排除。

### 5.4.3. 声明切点

切点决定了感兴趣的连接点，因此，使用户可以控制通知在何时运行。Spring AOP只支持Spring beans的方法执行连接点，所以，可以认为一个切点是匹配在Spring beans上执行方法。一个切点声明包含了两个部分：一个包含名称和任意参数的签名和一个切点表达式。在@AspectJ注解风格的AOP中，一个切点签名是通过常规方法定义来提供的，切点表达式通过使用`@Pointcut`注解来表示（用作切点签名的方法必须具有`void`返回类型）。

一个例子可能会清晰的表示切点签名和切点表达式。下面的例子定义了一个名为`anyOldTransfer`的切点，用来匹配任何方法名为`transfer`的切点表达式：

```java
@Pointcut("execution(* transfer(..))") // the pointcut expression
private void anyOldTransfer() {} // the pointcut signature
```

切点表达式形成的值`@Pointcut`是一个常规AspectJ 5的切点表达式。对于Aspect的切点语言，请参考`AspectJ Programming Guide`或一本关于AspectJ的书籍。
