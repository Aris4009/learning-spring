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

* 切点(Pointcut):匹配连接点的谓语。通知(Advice)连接了一个切点表达式，并且在与该切点匹配的任何连接点处运行（例如，执行具有特定名称的方法）。切点表达式匹配连接点的概念，是AOP的核心，默认情况下Spring使用AspectJ切点表达式语言。

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

**支持的切点指示符**

Spring AOP支持如下切点指示符：

* `execution`：用来匹配方法执行连接点。这是主要的切点指示符。

* `within`：将匹配限制为某些类型内的连接点（使用Spring AOP时，在匹配类型内声明的方法的执行）

* `this`：将匹配限制为bean的引用（Spring AOP代理）是指定类型的实例的连接点（使用Spring AOP时执行的方法）。

* `target`：将匹配限制为目标对象（应用程序被带的对象）是指定类型的实例的连接点。

* `args`：将匹配限制为参数是指定类型的实例的连接点（使用Spring AOP时方法的执行）。

* `@target`：限制匹配的连接点，其中传递的实际参数的运行时类型是指定类型注解

* `@within`：限制匹配到具有指定注解的类型内的连接点（使用Spring AOP时，使用给定注释在类型中声明的方法的执行）。

* `@annotation`：将匹配限制为连接点的主题（在Spring AOP中运行的方法）具有指定注解的连接点。

<div">

<center>其他切点类型</center>
</div>

*完整的AspectJ切点语言支持额外的切点标识符，Spring不支持他们：`call`、`get`、`set`、`preinitialization`、`staticinitialization`、`initialization`、`handler`、`adviceexecution`、`withincode`、`cflow`、`cflowbelow`、`if`、`@this`、`@withincode`。在Spring AOP中使用这些标识符会导致IllegalArgumentException。*

*这些标识符在将来的版本可能会扩展至Spring AOP中以便支持更多的AspectJ切点标识符。*

因为Spring AOP限制匹配只能在方法执行的连接点，因此，前面对切点指示符的讨论所给出的定义比在AspectJ编程指南中所能找到的要窄。此外，AspectJ本身具有基于类型的语义，并且在执行连接点处，`this`和`target`都引用同一个对象：执行该方法的对象。Spring AOP是基于代理的系统，区分代理对象本身（绑定到此对象）和代理后面的目标对象（绑定到目标）。

*由于Spring AOP框架基于代理的特性，因此根据定义，不会拦截目标对象内的调用。对于JDK代理，仅拦截代理上的公共接口方法调用。使用CGLIB，将拦截代理上的public和protected方法调用（必要时甚至包括程序包可见的方法）。但是通过代理进行的常见交互应始终通过公开签名进行设计。*

*注意，切点定义通常与任何拦截方法匹配。如果一个切点严格意义上仅是公开的，即使在CGLIB代理方案中可能存在通过代理进行非公开的交互，也需要相应的定义切点。*

*如果需要拦截在目标类中包括方法调用甚至构造函数，请考虑使用Spring-driven的本地AspectJ织入，而不是基于代理的AOP框架。这构成了具有不同特性的AOP使用模式，因此，确保在做出决定之前先熟悉织入。*

Spring AOP还支持其他名为`bean`的PCD。使用PCD，可以将连接点的匹配限制为特定的命名Spring Bean或一组命名Spring Bean（使用通配符时）。bean PCD具有以下形式：

```java
bean(idOrNameOfBean)
```

`idOrNameOfBean`可以是任意Spring bean的名称。限制通配符支持使用`*`，因此，如果为Spring bean建立了一些命名约定，则可以编写一个bean PCD表达式来选择他们。与其他切入点指示符一样，bean PCD可以与`&&`、`||`、`!`一起使用。

*`bean`PCD仅在Spring AOP中支持，本地AspectJ织入不支持它。他是AspectJ定义的标准PCDS的扩展，因此，对于在@Aspect模型中声明的切面不适用。*

*`bean`PCD在实例级别（基于Spring bean名称概念）上运行，而不是仅在类级别（基于织入AOP限制）上运行。基于实例的切点指示符是基于代理的Spring AOP框架的特殊功能，并且与Spring bean工厂紧密集成，因此可以自然而直接地通过名称识别特定bean。*

**链接切点表达式**

可以使用&&,||,!来连接切点。也可以通过名字引用切点表达式。接下来的例子展示了3个切点表达式：

```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} 

@Pointcut("within(com.xyz.myapp.trading..*)")
private void inTrading() {} 

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} 
```

<mark>1</mark>如果方法执行连接点代表执行任何公共方法，`anyPublicOperation`将匹配它

<mark>2</mark>`isTrading`将匹配在trading模块中的方法执行

<mark>3</mark>`tradingOperation`将匹配在trading模块中的任何代表方法执行的公共方法。

最佳实践是从较小的命名组件中构建更复杂的切入点表达式，如先前所示。当按名称引用切入点时，将应用常规的Java可见性规则（您可以看到相同类型的私有切入点，层次结构中受保护的切入点，任何位置的公共切入点，等等）。可见性不影响切入点匹配。

**共享通用切点定义**

在使用企业应用程序时，开发人员通常希望从多个方面引用应用程序的模块和特定的操作集。建议为此定义一个`CommonPointcuts`切面来捕获公共切点表达式。

```java
package com.xyz.myapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class CommonPointcuts {

    /**
     * A join point is in the web layer if the method is defined
     * in a type in the com.xyz.myapp.web package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.web..*)")
    public void inWebLayer() {}

    /**
     * A join point is in the service layer if the method is defined
     * in a type in the com.xyz.myapp.service package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.service..*)")
    public void inServiceLayer() {}

    /**
     * A join point is in the data access layer if the method is defined
     * in a type in the com.xyz.myapp.dao package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.myapp.dao..*)")
    public void inDataAccessLayer() {}

    /**
     * A business service is the execution of any method defined on a service
     * interface. This definition assumes that interfaces are placed in the
     * "service" package, and that implementation types are in sub-packages.
     *
     * If you group service interfaces by functional area (for example,
     * in packages com.xyz.myapp.abc.service and com.xyz.myapp.def.service) then
     * the pointcut expression "execution(* com.xyz.myapp..service.*.*(..))"
     * could be used instead.
     *
     * Alternatively, you can write the expression using the 'bean'
     * PCD, like so "bean(*Service)". (This assumes that you have
     * named your Spring service beans in a consistent fashion.)
     */
    @Pointcut("execution(* com.xyz.myapp..service.*.*(..))")
    public void businessService() {}

    /**
     * A data access operation is the execution of any method defined on a
     * dao interface. This definition assumes that interfaces are placed in the
     * "dao" package, and that implementation types are in sub-packages.
     */
    @Pointcut("execution(* com.xyz.myapp.dao.*.*(..))")
    public void dataAccessOperation() {}

}
```

可以在需要切点表达式的任何地方引用这样的切面定义的切点。例如，要是service层具有事务功能，可以这样写：

```xml
<aop:config>
    <aop:advisor
        pointcut="com.xyz.myapp.CommonPointcuts.businessService()"
        advice-ref="tx-advice"/>
</aop:config>

<tx:advice id="tx-advice">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>
```

`<aop:config>`和`<aop:advisor>`元素`Schema-based AOP Support`中讨论。事务元素在事务管理章节中讨论。

**例子**

Spring AOP用户常常喜欢使用`execution`切点指示器。下面是execution表达式的格式：

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)?throws-pattern?)
```

除了returning type pattern外的所有部分（在前面片段中的`ret-type-pattern`），name pattern和parameters pattern是可选的。Returning type pattern决定了该方法的返回类型必须是为了使连接点匹配。`*`是最常用作returning type pattern的。它匹配了任意返回类型。一个全限定类型名称仅当方法返回指定类型时才匹配。Name parttenr皮；诶方法名。可以使用`*`通配符作为name pattern的一部分。如果特别声明了一个type parttern，在末尾添加`.`以将其连接到name pattern组件。Parameters pattern参数模式稍微复杂一些：`()`匹配一个无参的方法，`(..)`匹配任意数量的参数（0个或多个）。`(*)`pattern 匹配具有任何一个参数的方法。`(*,String)`匹配具有两个参数的方法。第一个是可以是任意类型，但是第二个参数必须是`String`。参考AspectJ编程指南中的`Language Semantics`以便获取更多信息。

下面的例子展示了一些通用切点表达式：

* 任何公共方法的执行：

```
execution(public * *(..))
```

* 任意以`set`为前缀的方法的执行

```
execution(* set*(..))
```

* `AccountService`接口中定义的任意方法的执行

```
execution(* com.xyz.service.AccountService.*(..))
```

* 任意`service`包中定义的方法的执行

```
execution(* com.xyz.service.*.*(..))
```

* 任意`service`包或子包中定义的方法

```
execution(* com.xyz.service..*.*(..))
```

* service包中的任意连接点(仅在Spring AOP中的方法执行)

```
within(com.xyz.service.*)
```

* service包或它的子包中的任意连接点（仅在Spring AOP中的方法执行）

```
within(com.xyz.service..*)
```

* 实现了`AccountService`接口的代理的任意连接点（仅在Spring AOP中的方法执行）

```
this(com.xyz.service.AccountService)
```

| 'this'通常以绑定形式使用。参考Declaring Advice，如何使在通知体中的代理对象可用。 |
| --------------------------------------------------- |

* 实现了`AccountService`接口的目标对象的任意连接点（仅在Spring AOP中的方法执行）

```
target(com.xyz.service.AccountService)
```

| 'target'通常以绑定形式使用。参考Declaring Advice，如何让在通知体中的目标对象可用。 |
| ----------------------------------------------------- |

* 任意采用单个参数并且在运行时传递的参数是可序列化的连接点（仅在Spring AOP中的方法执行）

```
args(java.io.Serializable)
```

| 'args'通常以绑定形式使用。参考Declaring Advice，如何在通知体中让方法参数可用。 |
| -------------------------------------------------- |

注意，这个例子中指定的切点不同于`execution(* *(java.io.Serializabale))`。args版本匹配的是在运行时传递的参数是可序列化的，execution版本匹配的是声明了一个`Serializable`类型参数的方法签名。

* 目标对象具有`@Transactional`注解的任意切点（仅在Spring AOP中的方法执行）

```
@target(org.springframework.transaction.annotation.Transactional)
```

| 通常以数据绑定形式使用'@target'。参考Declaring Advice，如何在通知体中让注解对象可用。 |
| ------------------------------------------------------- |

* 目标对象声明的类型具有`@Transactional`注解的任意连接点

```
@within(org.springframework.transaction.annotation.Transactional)
```

| '@within'通常是以绑定形式使用。参考Declaring Advice，如何在通知体中让注解对象可用。 |
| ------------------------------------------------------ |

* 执行的方法具有`@Transactional`注解的任意连接点

```
@annotation(org.springframework.transaction.annotation.Transactional)
```

| '@annotation'通常以绑定的形式使用。参考Declaring Advice，如何在通知体中让注解对象可用。 |
| ---------------------------------------------------------- |

* 有一个参数，并且传递的参数的运行时类型具有`@Classified`注解的任意连接点

```
@args(com.xyz.security.Classified)
```



| '@args'通常以绑定的形式使用。参考Declaring Advice，如何在通知体中让注解对象可用。 |
| ---------------------------------------------------- |

* Spring bean名称包含匹配通配符表达式`*Service`的任意切点。

```
bean(*Service)
```

**写出好的切点**

在编译期间，AspectJ处理切点以优化匹配性能。检查代码并且确定每个连接点是否匹配（静态或动态）指定的切点是一个昂贵的过程。（动态匹配意味着不能从静态分析中完全确定匹配，并且在代码中需要测试来决定在代码运行时的实际匹配）。首次遇到切点声明时，AspectJ将其重写为匹配过程的最佳形式。这意味着什么？基本上，切点以DNF（析取范式）重写，并且对切点的组件进行排序，以便首先检查那些比较廉价的组件。这意味着不需要担心了解各种切点指示器的性能并且可以在切点声明中以任何顺序提供他们。



但是，AspectJ只能使用所告诉的内容。为了获得作家的匹配性能，应该考虑他们视图达到的目标，并在定义中尽可能缩小匹配的搜索空间。现有的指示符属于一下三类之一：类别，作用域和上下文：

* 类别的指示符算则一个特殊类别的连接点：`execution`，`get`，`set`，`call`，`handler`。

* 作用域范围指示符选择一组感兴趣的（可能是多种类型）的连接点：`within`，`withincode`。

* 上下文指示符根据上下文（并可选的绑定）匹配：`this`，`target`，`annotation`。

好的切点应该首先包含至少两类（类别和作用域）。可以包含上下文指示器来匹配基于连接点上下文或绑定为了在通知中使用上下文。由于额外的处理和分析，只提供一种指示器或只有一种上下文指示器工作可能会影响织入性能（时间和内存的使用）。作用域指示器匹配非常快，并且使用他们的用法意味着AspectJ可以非常迅速地消除不需要进一步处理一组连接点。一个好的切点如果在可能的情况下应该总是包含一个。



### 5.4.4. 声明通知

通知与切点表达式关联，并且在切点匹配的方法之前、之后或周围运行。切点表达式可以是对命名切点的简单引用，也可以是直接声明的切点表达式。



**Before Advice**

通过使用`@Before`注解，在切面中声明一个前置通知：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }
}
```

如果直接使用切点表达式，可以将上面的例子重写为如下形式：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("execution(* com.xyz.myapp.dao.*.*(..))")
    public void doAccessCheck() {
        // ...
    }
}
```

**After Returning Advice**

当匹配的方法执行正常返回时，执行后置返回通知。可以使用`@AfterReturning`注解来声明：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

    @AfterReturning("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }
}
```



| 可以在同一个切面中拥有多个通知声明（以及其他成员）。在这些示例中，仅显示单个建议声明，以集中每个建议的效果。 |
| ------------------------------------------------------ |

有时，需要在通知体中访问实际返回值。可以使用`@AfterReturning`的形式绑定绑定返回值来访问：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

    @AfterReturning(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        returning="retVal")
    public void doAccessCheck(Object retVal) {
        // ...
    }
}
```

在`returning`属性中使用的名称必须对应通知方法中的参数。当方法执行返回时，返回值作为相应参数值传入通知方法。一个`returning`语句也限制匹配那些返回值为指定类型的方法执行（在这个例子中，`Object`匹配任何返回类型）。



注意，after returning advice使用时，不可能返回完全不同的引用。



**After Throwing Advice**

当匹配方法执行抛出异常时，抛出异常后通知会运行。可以通过使用`@AfterThrowing`注解来声明：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doRecoveryActions() {
        // ...
    }
}
```

通常，仅当抛出指定异常时，通知才回运行并且在通知体中需要访问抛出的异常。可以使用`throwing`属性来限制匹配（使用`Throwable`作为异常类型）并且将抛出异常绑定到通知参数中：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing(
        pointcut="com.xyz.myapp.CommonPointcuts.dataAccessOperation()",
        throwing="ex")
    public void doRecoveryActions(DataAccessException ex) {
        // ...
    }
}
```

在`throwing`属性中使用的名称必须和通知方法中的参数对应。当方法执行存在抛出异常时，异常作为相应的参数值传递到通知方法中。一个`throwing`语句也限制了仅匹配那些抛出指定异常的方法执行（`DataAccessException`）。



| 注意，`AfterThrowing`不能标识一个通用异常处理的回调。特别是，`AfterThrowing`通知方法仅支持从连接点中获取异常（用户声明的目标方法），不能从伴随的`@After/@AfterReturning`方法中获取。 |
| --------------------------------------------------------------------------------------------------------------------- |

**After (Finally) Advice**

当匹配方法执行时，后置（最终）通知运行。通过使用`@After`注解来声明。后置通知必须准备处理正常和异常返回条件。通常用来释放资源和类似的目的：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;

@Aspect
public class AfterFinallyExample {

    @After("com.xyz.myapp.CommonPointcuts.dataAccessOperation()")
    public void doReleaseLock() {
        // ...
    }
}
```



| 注意，AspectJ中的`@After`注解定义为`after finally advice`，与在try-catch语句中的finally块相似。对于任何结果，正常返回或连接点抛出的异常（用户声明的目标方法），它会被调用，与`@AfterReturning`相比，只能应用于成功的正常返回。 |
| -------------------------------------------------------------------------------------------------------------------------------------------------- |

**Around Advice**

最后一类通知是环绕通知。环绕通知”环绕“一个匹配的方法执行。它能同事在方法运行的前后执行，并且可以确定何时，如何以及是否真正运行该方法。环绕通知常用来以线程安全的方式（例如，启动和停止计时器），需要在方法执行的前后共享状态。要使用最小符合要求的通知类型，也就是说，如果before advice能满足需求，就不要使用around advice。



环绕通知通过使用`@Around`注解来声明。通知方法的第一个参数必须是`ProceedingJoinPoint`类型。使用通知体，调用`ProceedingJoinPoint`上的`proceed()`会使底层方法运行。这个`proceed`方法也可以传递一个`Object[]`。当方法继续执行时，数组中的值用来当做方法执行的参数。



| 当使用`Object[]`调用时，`proceed`的行为与通过AspectJ编译器编译后的环绕通知的`proceed`行为有些不同。对于使用传统AspectJ语言编写的环绕通知，给`proceed`传递的参数数量必须与传递给环绕通知的参数数量相匹配（不是底层连接点接收的参数数量），并且，在指定参数位置中传递给proceed的值会取代在连接点的原始值（如果现在做没有意义，也不用担心）。Spring采取的方法更简单，并且更适合其基于代理的仅执行的语义。如果编译为Spring编写的@AspectJ切面，并在AspectJ边奇艺和weaver中使用参数进行处理，则只需要意识到这种差异即可。这里有一种可以写出100%同时匹配Spring AOP和AspectJ的方法，在`following section on advice parameters`章节中。 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |



下面的例子展示了如何使用环绕通知：

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class AroundExample {

    @Around("com.xyz.myapp.CommonPointcuts.businessService()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        // start stopwatch
        Object retVal = pjp.proceed();
        // stop stopwatch
        return retVal;
    }
}
```

通过环绕通知返回的值，是方法的调用者看到的返回值，例如，一个简单的缓存切面可以从缓存中返回值，如果没有，则调用`proceed()`。注意，`proceed`可能被调用一次、多次或完全不被调用。这些都是合法的。



**通知参数**

Spring提供了完整类型的通知，这意味着可以在通知签名中（前面我看看到的returning和throwing例子）需要的地方声明参数，而不是一直使用`Object[]`。将在本节的后面部分介绍如对于通知体，何使用参数和其他上下文值。先来看一下如何编写通用通知，以了解当前通知中的通知方法。



**访问当前`JoinPoint`切点**

任何声明的通知方法，`org.aspectj.lang.JoinPoint`类型的参数总是它的第一个参数（注意，环绕通知要求声明的第一个参数类型是`ProceedingJoinPoint`，它是`JoinPoint`的子类。`JoinPoint`接口提供了大量有用的方法）：

* `getArgs()`：返回方法参数。

* `getThis()`：返回代理对象。

* `getTarget()`：返回目标对象。

* `getSignature()`：返回被通知方法的描述。

* `toString()`：打印有用的被通知方法的描述。



**给通知传递参数**

已经准备好如何绑定返回值或异常值（使用after returning和after throwing advice）。为了让通知体中的参数值可用，可以使用`args`的绑定形式。在参数表达式中，如果使用参数名代替类型名称，那么当通知被调用时，相应的参数值会作为参数进行传递。一个例子应该让这更清楚。假设希望使用`Account`对象作为第一个参数通知DAO操作执行，需要在通知体中访问这个account：

```java
@Before("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
public void validateAccount(Account account) {
    // ...
}
```

这个表达式中的`arg(account,...)`部分有两个目的。首先，它限制了仅匹配那些至少有一个参数的方法执行并且传递的参数是`Account`实例。第二，通过`account`参数，让实际`Account`对象可用于通知中。



另一种编写这种声明切点的方法是，当匹配一个连接点时，提供一个`Account`对象值，然后从通知中引用命名的切点：

```java
@Pointcut("com.xyz.myapp.CommonPointcuts.dataAccessOperation() && args(account,..)")
private void accountDataAccessOperation(Account account) {}

@Before("accountDataAccessOperation(account)")
public void validateAccount(Account account) {
    // ...
}
```

更多细节可参考AspectJ编程指南。



代理对象`(this)`，目标对象`(target)`和注解`(@within,@target,@annotation,@args)`可以以相似形式进行绑定。下面两个例子展示了如何使用一个`@Auditable`注解来匹配方法注解的执行，并且提取审核代码：



第一个例子定义了`@Auditable`注解：

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditCode value();
}
```

第二个例子展示了匹配`@Auditable`方法执行的通知：

```java
@Before("com.xyz.lib.Pointcuts.anyPublicMethod() && @annotation(auditable)")
public void audit(Auditable auditable) {
    AuditCode code = auditable.value();
    // ...
}
```



**通知参数和泛型**

Spring AOP可以处理在类种声明的泛型和方法中的泛型参数。假设有一个泛型类如下所示：

```java
public interface Sample<T> {
    void sampleGenericMethod(T param);
    void sampleGenericCollectionMethod(Collection<T> param);
}
```

可以通过在要拦截方法的参数类型中键入通知参数，将方法类型限制为某些参数类型：

```java
@Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
public void beforeSampleMethod(MyType param) {
    // Advice implementation
}
```

<mark>这种方法杜宇泛型集合来说是无效的，所以不能向下面的例子一样定义一个切点：</mark>

```java
@Before("execution(* ..Sample+.sampleGenericCollectionMethod(*)) && args(param)")
public void beforeSampleMethod(Collection<MyType> param) {
    // Advice implementation
}
```

为了让它有效，不得不检查集合中的每个元素，这是不合理的，因为无法决定如何处理`null`值。为了达到相似的目的，必须将参数键入`Collection<?>`作为参数，并手动检查参数类型。



**确定参数名字**

绑定在通知调用中的参数依靠切点表达式中的使用的名称与切点方法签名中声明的参数进行匹配。通过Java反射策略无法获得参数名称，因此Spring AOP使用以下策略来确定参数名称：

* 如果用户明确指定了参数名称，那就使用这个指定的参数名称。通知和切点注解都有一个可选择的`argNames`属性，可以用来指定注解方法的参数名称。这些参数名称在运行时可用，下面的例子展示了如何使用`argNames`属性：

```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code and bean
}
```

如果第一个参数是`JoinPoint`，`ProceedingJoinPoint`，或`JoinPoint.StaticPart`其中的一种，可以从`argNames`属性的值中忽略参数的名称。例如，如果修改之前的通知来获取连接点对象，`argNames`属性不需要包含它：

```java
@Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
        argNames="bean,auditable")
public void audit(JoinPoint jp, Object bean, Auditable auditable) {
    AuditCode code = auditable.value();
    // ... use code, bean, and jp
}
```

对于`JoinPoint`，`PorceedingJoinPoint`和`JoinPoint.StaticPart`类型的第一个参数给与的特殊处理对于不手机任何其他连接点上下文的通知实例特别方便。在这些情况下，可以省略`argNames`属性。例如，下面的例子不需要声明`argNames`属性：

```java
@Before("com.xyz.lib.Pointcuts.anyPublicMethod()")
public void audit(JoinPoint jp) {
    // ... use jp
}
```



* 使用`argNames`属性有一点笨拙，所以，如果`argNames`属性没有被指定，Spring AOP查看该类的调试信息并且尝试从本地变量表中决定参数名。只要已使用调试信息（至少是'-g:vars'）编译了类，这个信息就会存在。使用此标志进行编译的后果是：(1)代码更容易理解（反向工程师），(2)类文件的大小略大（通常无关紧要），(3)编译器未应用删除未使用的局部变量的优化。换句话说，通过启用此标志，应该不会遇到任何困难。



| 如果通过AspectJ编译器（ajc）编译`@AspectJ`，甚至不需要调试信息，不需要增加`argNames`属性，因为编译器保留了所需的信息。 |
| -------------------------------------------------------------------------- |

* 如果编译的代码没有所需的调试信息，Spring AOP尝试推断绑定变量与参数的配对（例如，如果仅有一个变量绑定到切点表达式，并且通知方法仅接收一个参数，则配对很明显）。指定可用信息，如果变量的绑定不明确，会抛出`AmbiguousBindingException`。

* 如果上面的策略都失败了，就会抛出`IllegalArgumentException`。



**处理参数**

前面提到过，将描述如何编写一个在Spring AOP和AspectJ中始终有效的`proceed`参数调用。解决方案是确保通知签名按顺序绑定每个方法参数：

```java
@Around("execution(List<Account> find*(..)) && " +
        "com.xyz.myapp.CommonPointcuts.inDataAccessLayer() && " +
        "args(accountHolderNamePattern)")
public Object preProcessQueryPattern(ProceedingJoinPoint pjp,
        String accountHolderNamePattern) throws Throwable {
    String newPattern = preProcess(accountHolderNamePattern);
    return pjp.proceed(new Object[] {newPattern});
}
```

在许多情况下，无论如何都要进行此绑定（如上面的例子展示的一样）。



**通知排序**

当多个通知都想在相同的连接点运行时，将会发生什么？Spring AOP遵循与AspectJ相同的优先级规则来确定通知执行的顺序。优先级最高的通知在”进入时“首先运行（因此，两个指定的before advice，其中一个高优先级的会首先运行）。从连接点”离开时“，优先级的通知最后运行（因此，两个指定的after advice，高优先级的将会最后运行）。



当定义在不同切面中的两个通知都需要在相同的切点运行时，除非指定，否则执行顺序是未定义的。可以通过指定优先级来控制执行顺序。可以通过常规的Spring方法在切面类中实现`org.springframework.core.Ordered`接口或使用`@Order`注解来完成。指定的两个切面，切面从`Ordered.getOrder()`（或注解值）返回的教小的值具有较高的优先级。



| 从概念上讲，特定切面的每种不同类型的通知可直接应用于连接点。作为结果，`@AfterThrowing`通知不支持从伴随的`@After`/`@AfterReturning`方法中接收异常。<br/>5.2.7以后的Spring框架，在相同的`@Aspect`类中定义的需要在统一连接点上运行的通知方法，将根据通知类型从高到底的优先级进行分配：`@Around`，`@Before`，`@After`，`@AfterReturning`，`@AfterThrowing`。注意，在相同切面的任何`@AfterReturning`或`@AfterThrowing`通知方法之后，都会有效地调用`@After`通知方法，对于`@After`来说，它遵循了AspectJ的"after finally advice"语义。<br/><br/>当在相同的`@Aspect`类中定义的两个相同类型的通知（例如，两个`@After`通知方法）都需要在相同的切点上运行时，排序是未定义的（因为没有办法通过反射对编译好的类获取源代码声明的顺序）。考虑将此类通知方法分解为每个@Aspect类中的每个连接点的一个通知方法，或者将通知碎片重构为单独的@Aspect类，您可以在切面级别通过Ordered或@Order排序这些类。 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |

### 5.4.5. Introductions（引入）

引入（在AspectJ中被声明为inner-type）使切面可以声明被通知对象实现指定接口，并且代表这些对象提供该接口的实现。



通过使用`@DeclareParents`注解来创建一个introduction。这个注解用来声明匹配那些具有新的父类型。例如，指定一个名为`UsageTracked`接口，并且该接口的实现命名为`DefaultUsageTracked`，下面的切面声明了服务接口的所有实现者也实现了`UsageTracked`接口（例如，通过JMX进行统计）：

```java
@Aspect
public class UsageTracking {

    @DeclareParents(value="com.xzy.myapp.service.*+", defaultImpl=DefaultUsageTracked.class)
    public static UsageTracked mixin;

    @Before("com.xyz.myapp.CommonPointcuts.businessService() && this(usageTracked)")
    public void recordUsage(UsageTracked usageTracked) {
        usageTracked.incrementUseCount();
    }

}
```

通过带注解的字段类型来决定接口的实现。`@DeclareParents`注解中的属性`value`是一个AspectJ类型模式。任何匹配类型的bean都实现`UsageTracked`接口。注意，在前面的before advice例子中，service beans可以直接用作`UsageTracked`接口的实现。如果通过编程来访问一个bean，则应该编写一下内容：

```java
UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
```



### 5.4.5. 切面实例化模型

| 这是一个高级主题。如果刚刚开始使用AOP，可以跳过这部分。 |
| ----------------------------- |

默认情况下，应用程序上下文中每个切面只有一个实例。AspectJ调用这个单例实例化的模型。可以用不同的生命周期来定义切面。Spring支持切面的`perthis`和`pertarget`实例化模型；`percflow`，`percflowbelow`和`pertypedwithin`目前不支持。



通过在`@Aspect`注解上定义`perthis`语句声明一个`perthis`切面。思考下面的例子：

```java
@Aspect("perthis(com.xyz.myapp.CommonPointcuts.businessService())")
public class MyAspect {

    private int someState;

    @Before("com.xyz.myapp.CommonPointcuts.businessService()")
    public void recordServiceUsage() {
        // ...
    }
}
```

在上面的例子中，`perthis`语句的作用是对于每个唯一的servie对象，会创建一个切面实例，用来执行业务服务（每个唯一的对象在切点表达式匹配的连接点上绑定到`this`）。切面实例是在服务对象上的方法首次调用时被创建。当服务对象超出范围时，切面也超出了范围。在切面实例被创建之前，任何通知都不会运行。一旦创建了切面实例，其中声明的通知就会在匹配的连接点上运行，但是仅当服务对象是与此切面相关联的对象。参考AspectJ编程指南来获取`per`语句的更多信息。



`pertarget`实例模型的工作与`perthis`相似，但是它在匹配的连接点上为每个唯一的目标对象创建一个切面实例。



### 5.4.7. 一个AOP的例子

业务逻辑执行服务有时候由于并发问题可能会失败（例如，死锁）。如果重试该操作，则下次尝试可能会成功。对于那些在合适的条件下进行重试的业务逻辑服务来说（不需要返回用户那里来解决冲突的幂等操作），希望透明的重试该操作，以避免客户端看到`PessimisticLockingFailureException`。这是一个明显跨越服务层中的多个服务的需求，因此，非常适合通过一个方面来实现。



因为想要重试该操作，需要使用环绕通知以便可以多次调用`proceed`。下面的例子展示了基本切面的实现：

```java
@Aspect
public class ConcurrentOperationExecutor implements Ordered {

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int order = 1;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Around("com.xyz.myapp.CommonPointcuts.businessService()")
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        PessimisticLockingFailureException lockFailureException;
        do {
            numAttempts++;
            try {
                return pjp.proceed();
            }
            catch(PessimisticLockingFailureException ex) {
                lockFailureException = ex;
            }
        } while(numAttempts <= this.maxRetries);
        throw lockFailureException;
    }
}
```



注意，这个切面实现了`Ordered`接口，因此可以将切面的优先级设置为高于事务通知的优先级（每次重试都希望有新的事务）。`maxRetries`和`order`属性都是通过Spring配置的。主要的操作发生在`doConcurrentOperation`的环绕通知中。注意，目前对每个`businessService()`应用了重试逻辑。尝试处理并且如果发生`PessimisticLockingFailureExcpetion`时，继续尝试，除非耗尽了所有的重试次数。



相应的Spring配置如下：

```xml
<aop:aspectj-autoproxy/>

<bean id="concurrentOperationExecutor" class="com.xyz.myapp.service.impl.ConcurrentOperationExecutor">
    <property name="maxRetries" value="3"/>
    <property name="order" value="100"/>
</bean>
```



尽在幂等操作下进行重试，所以重新定义一个幂等的切面注解`@Idempontent`：

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    // marker annotation
}
```

然后使用注解来注释服务操作的实现。对切面的更改是只重试幂等操作，这涉及细化切入点表达式，以便只有`@Idempotent`操作匹配，如下所示:

```java
@Around("com.xyz.myapp.CommonPointcuts.businessService() && " +
        "@annotation(com.xyz.myapp.service.Idempotent)")
public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
    // ...
}
```

## 5.5. 基于AOP的支持

如果更喜欢基于XML的格式，Spring也提供了使用`aop`命名空间标签来定义对切面的支持。支持与使用@AspectJ样式时具有完全相同的切点表达式和通知类型。因此，本章节将主要关注语法，并引导读者理解编写的切点表达式和通知参数的绑定。



为了使用aop命名空间标签，需要导入spring-aop schema。



在Spring配置中，所有切面和advisor元素必须放置在`<aop:config>`元素中（在应用程序上下文配置中，可以有多个`<aop:config>`元素）。一个`<aop:config>`元素可以包含切点，advisor和切面元素（注意，他们必须按顺序声明）。



| `<aop:config>`风格的配置大量使用了Spring的自动代理机制。如果已经通过使用`BeanNameAutoProxyCreator`或其他相似的方法来使用显示的自动代理，则可能会导致问题（例如，未织入的通知）。推荐的做法是要么使用`<aop:config>`，要么使用`AutoProxyCreator`，并且不要混合使用。 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |

### 5.5.1. 声明一个切面

当使用schema支持时，一个切面是一个常规的Java对象，它作为bean被定义到Spring应用程序上下文中。状态和行为记录在对象的字段和方法中，切点和通知信息记录在XML中。



通过使用`<aop:aspect>`元素，可以声明一个切面，并且通过`<ref>`属性来引用该bean：

```xml
<aop:config>
    <aop:aspect id="myAspect" ref="aBean">
        ...
    </aop:aspect>
</aop:config>

<bean id="aBean" class="...">
    ...
</bean>
```

支持切面的bean当然可以像其他任何Spring bean一样进行配置并注入依赖。



### 5.5.2. 声明切点

可以在`<aop:config>`元素中声明一个命名的切点，让切点定义可以跨多个切面和advisors来被共享。



一个切点代表在service层中任何业务的执行：

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="execution(* com.xyz.myapp.service.*.*(..))"/>

</aop:config>
```

注意，切点表达式本身使用与支持`@AspectJ`的AspectJ切点表达式相同的语言。如果使用基于声明风格的schema，可以引用切点表达式中定义的命名切点。另一种定义上述切点的方式如下：

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="com.xyz.myapp.CommonPointcuts.businessService()"/>

</aop:config>
```



假设有一个`CommonPointcuts`切面，然后在切面中声明一个切点与声明到顶级切点类似：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..))"/>

        ...
    </aop:aspect>

</aop:config>
```

与`@AspectJ`切面类似，通过收集连接点上下文来声明使用基于schema的定义样式声明的切点可以收集连接点上下文。例如，如下切点收集了连接点上下文的`this`对象并将它传递给通知：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..)) && this(service)"/>

        <aop:before pointcut-ref="businessService" method="monitor"/>

        ...
    </aop:aspect>

</aop:config>

```



通知必须通过包含匹配名称的参数来声明以接收收集到的连接点上下文，如下所示:

```java
public void monitor(Object service) {
    // ...
}
```

当连接切点子表达式时，在XML文档中使用`&amp;&amp;`是非常别扭的，所以可以使用`and`，`or`和`not`关键字来代替`&amp;&amp;`，`||`，和`!`。例如，前面的切点可以以下面这种更好的方式来表达：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..)) and this(service)"/>

        <aop:before pointcut-ref="businessService" method="monitor"/>

        ...
    </aop:aspect>
</aop:config>
```

注意，以这种方式定义的切点是由它们的XML id引用的，不能作为命名的切点来形成复合切点。因此，这种命名切点支持比@AspectJ样式所提供的更受限制。



### 5.5.5. 声明通知

基于schema的AOP与`@AspectJ`风格具有相同的5中通知类型，并且他们具有相同的语义。



**Before Advice**

Before advice在匹配的方法执行钱运行。它通过使用`<aop:before>`元素，声明在`<aop:aspect>`内部：

```xml
<aop:aspect id="beforeExample" ref="aBean">

    <aop:before
        pointcut-ref="dataAccessOperation"
        method="doAccessCheck"/>

    ...

</aop:aspect>
```

这里，`dataAccessOperation`是定义在顶级(`<aop:config>`)中定义的切点`id`。可以替换`pointcut-ref`来定义内联切点，使用`pointcut`属性来替换`pointcut-ref`：

```xml
<aop:aspect id="beforeExample" ref="aBean">

    <aop:before
        pointcut="execution(* com.xyz.myapp.dao.*.*(..))"
        method="doAccessCheck"/>

    ...
</aop:aspect>
```

正如在@AspectJ样式的讨论中指出的那样，使用命名切点可以显著提高代码的可读性。



`method`属性标识了可以提供通知体的方法。必须为包含通知的切面元素所引用的bean定义此方法。在数据访问操作执行前（通过切点表达式匹配的方法执行连接点），在切面上的`doAccessCheck`方法会被调用。



**After Returning Advice**

当匹配的方法执行正常完成时，after returning advice会运行。它的声明与before advice类似：

```xml
<aop:aspect id="afterReturningExample" ref="aBean">

    <aop:after-returning
        pointcut-ref="dataAccessOperation"
        method="doAccessCheck"/>

    ...
</aop:aspect>
```

像`@AspectJ`风格一样，可以通过通知体获取返回值。为了达到此目的，使用`returning`属性来指定应该传递的返回值的参数名称：

```xml
<aop:aspect id="afterReturningExample" ref="aBean">

    <aop:after-returning
        pointcut-ref="dataAccessOperation"
        returning="retVal"
        method="doAccessCheck"/>

    ...
</aop:aspect>
```

`doAccessCheck`方法必须声明一个名为`retVal`的参数。这个参数的类型约束匹配方式与使用`@AfterReturning`描述相同：

```java
public void doAccessCheck(Object retVal) {...
```



**After Throwing Advice**

当匹配的方法执行存在一个可抛出的异常时，after throwing advice会运行：

```xml
<aop:aspect id="afterThrowingExample" ref="aBean">

    <aop:after-throwing
        pointcut-ref="dataAccessOperation"
        method="doRecoveryActions"/>

    ...
</aop:aspect>
```

与`@AspectJ`风格类似，可以在通知体中获取抛出的异常。为了达到此目的，使用`throwing`属性来指定应该传递的异常参数的名字：

```xml
<aop:aspect id="afterThrowingExample" ref="aBean">

    <aop:after-throwing
        pointcut-ref="dataAccessOperation"
        throwing="dataAccessEx"
        method="doRecoveryActions"/>

    ...
</aop:aspect>
```

方法`doRecoverActions`必须声明一个名为`dataAccessEx`的参数。这个参数限制匹配的类型与`@AfterThrowing`描述的相同：

```java
public void doRecoveryActions(DataAccessException dataAccessEx) {...
```



**After(Finally) Advice**

无论匹配的方法执行结果怎样，after(finally) advice都会运行。可以通过使用`after`元素来声明它：

```xml
<aop:aspect id="afterFinallyExample" ref="aBean">

    <aop:after
        pointcut-ref="dataAccessOperation"
        method="doReleaseLock"/>

    ...
</aop:aspect>
```



**Around Advice**

最后一种通知是around advice。它运行在匹配的方法的“周围”。它有机会同事在方法运行的前后来工作并决定何时、如何、甚至根本不运行该方法。环绕通知常被用来以线程安全的方式在方法执行前后共享状态（例如，启动和停止计时器）。总是要使用最小的通知类型来满足需求。不要在可以使用before advice就可以完成工作的情况下，使用环绕通知。



通过使用`aop:around`元素来声明环绕通知。环绕通知的第一个参数必须是`ProceedingJoinPoint`类型。在通知体内，可以调用`ProceedingJoinPoint`上的`proceed()`方法来运行底层方法。`proceed`方法也可以传递一个`Object[]`对象。当需要调用时，数组的值会用作方法执行的参数:

```xml
<aop:aspect id="aroundExample" ref="aBean">

    <aop:around
        pointcut-ref="businessService"
        method="doBasicProfiling"/>

    ...
</aop:aspect>
```

`doBasicProfiling`通知与`@AspectJ`例子极其相似：

```java
public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    // start stopwatch
    Object retVal = pjp.proceed();
    // stop stopwatch
    return retVal;
}
```



**通知参数**

基于schema声明的样式支持完全类型化的通知，与`@AspectJ`支持的方式相同-通过依靠通知方法参数的名字来匹配切点参数。如果希望明确指定通知方法的参数名（不依赖于前面描述的检测策略），可以通过使用通知元素中的`arg-names`属性，它和注解通知的`argNames`属性一样：

```xml
<aop:before
    pointcut="com.xyz.lib.Pointcuts.anyPublicMethod() and @annotation(auditable)"
    method="audit"
    arg-names="auditable"/>
```

属性`arg-names`接受逗号分割的参数列明。



以下基于XSD的方法中涉及程度稍高的示例显示了一些与一些强类型参数结合使用的建议：

```java
package x.y.service;

public interface PersonService {

    Person getPerson(String personName, int age);
}

public class DefaultPersonService implements PersonService {

    public Person getPerson(String name, int age) {
        return new Person(name, age);
    }
}
```

接下来是切面。注意，`profile(..)`实际接收大量的强类型参数，第一个是用于继续进行方法调用的连接点。此参数的存在表明profile（..）将被用作通知：

```java
package x.y;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;

public class SimpleProfiler {

    public Object profile(ProceedingJoinPoint call, String name, int age) throws Throwable {
        StopWatch clock = new StopWatch("Profiling for '" + name + "' and '" + age + "'");
        try {
            clock.start(call.toShortString());
            return call.proceed();
        } finally {
            clock.stop();
            System.out.println(clock.prettyPrint());
        }
    }
}
```

最后，以下示例XML配置影响了指定连接点的上述通知的执行：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- this is the object that will be proxied by Spring's AOP infrastructure -->
    <bean id="personService" class="x.y.service.DefaultPersonService"/>

    <!-- this is the actual advice itself -->
    <bean id="profiler" class="x.y.SimpleProfiler"/>

    <aop:config>
        <aop:aspect ref="profiler">

            <aop:pointcut id="theExecutionOfSomePersonServiceMethod"
                expression="execution(* x.y.service.PersonService.getPerson(String,int))
                and args(name, age)"/>

            <aop:around pointcut-ref="theExecutionOfSomePersonServiceMethod"
                method="profile"/>

        </aop:aspect>
    </aop:config>

</beans>
```



思考下面的驱动脚本：

```java
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import x.y.service.PersonService;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        BeanFactory ctx = new ClassPathXmlApplicationContext("x/y/plain.xml");
        PersonService person = (PersonService) ctx.getBean("personService");
        person.getPerson("Pengo", 12);
    }
}
```

有了这样的Boot类，将在标准输出上获得类似于以下内容的输出:

| StopWatch 'Profiling for 'Pengo' and '12'': running time (millis) = 0<br/>-----------------------------------------<br/>ms     %     Task name<br/>-----------------------------------------<br/>00000  ?  execution(getFoo) |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |



**Advice Ordering**

当多个通知需要运行在相同的连接点时，在`Advice Ordering`中描述了排序的规则。切面之间的优先级是通过在`<aop:aspect>`元素中的`order`属性或在bean上增加`@Order`注解或通过bean实现`Ordered`接口来实现的。



| 对比定义在`@Aspect`类中的通知方法的优先级，当定义在相同`<aop:aspect>`元素中的两个通知都需要在相同的连接点运行时，优先级由通知元素在封闭的`<aop:aspect>`元素中声明的顺序决定，从最高优先级到最低优先级。<br/>例如，指定的`around`通知和`before`通知定义在相同的`<aop:aspect>元素中，并应用于相同的连接点，为了确保``around`通知具有更高的优先级，`<aop:around>`元素必须声明在`<aop:before>`元素之前。<br/>作为一般经验，如果有多个通知定义在相同的`<aop:aspect>`元素中并应用与相同的连接点，考虑将这样的通知方法分解为每个`<aop:aspect>`元素中的每个连接点的一个通知方法，或者将通知的片段重构为单独的`<aop:aspect>`元素，可以在切面级别上对这些元素进行排序。 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |



### 5.5.4. Introductions（引入）

引入（在AspectJ中为类型间声明）让切面以声明通知的对象实现指定接口，并代表那些对象提供该接口的实现。



可以通过使用在`aop:aspect`中的`aop:declare-parents`元素来使用引入。可以使用`aop:declare-parents`元素来声明匹配的类型拥有新的父类。例如，指定一个名为`UsageTracked`接口并且该接口的实现为`DefaultUsageTracked`，下面的切面声明了服务接口的所有实现，也实现了`UsageTracked`接口。

```xml
<aop:aspect id="usageTrackerAspect" ref="usageTracking">

    <aop:declare-parents
        types-matching="com.xzy.myapp.service.*+"
        implement-interface="com.xyz.myapp.service.tracking.UsageTracked"
        default-impl="com.xyz.myapp.service.tracking.DefaultUsageTracked"/>

    <aop:before
        pointcut="com.xyz.myapp.CommonPointcuts.businessService()
            and this(usageTracked)"
            method="recordUsage"/>

</aop:aspect>
```

支持usageTracking bean的类将包含以下方法：

```java
public void recordUsage(UsageTracked usageTracked) {
    usageTracked.incrementUseCount();
}
```

通过`implement-interface`属性来决定要实现的接口。属性`type-matching`的值是一个切面类型模式。任何匹配类型的bean实现了`UsageTracked`接口。也就是说，在前面例子中的before advice，服务bean可以直接被当做`UsageTrarcked`接口的实现来使用：

```java
UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
```



### 5.5.5. 切面实例模型

单例模型是基于schema定义的切面唯一支持的模型。在未来的版本中，其他实例模型可能会被支持。



### 5.5.6. Advisors(顾问)

概念Advisors来自定义在Spring中的AOP，在AspectJ中没有直接相等的概念。一个Advisor就像一个小的独立切面，只有一个通知。通知本身由bean表示，并且必须实现Spring通知类型中描述的通知接口之一。Advisors可以利用AspectJ切点表达式。



通过`<aop:advisor>`元素来支持advisor的概念。它通常与事务通知结合使用，它具有自己的命名空间：

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="execution(* com.xyz.myapp.service.*.*(..))"/>

    <aop:advisor
        pointcut-ref="businessService"
        advice-ref="tx-advice"/>

</aop:config>

<tx:advice id="tx-advice">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>
```

像前面的例子一样，可以使用`pointcut`属性替换`pointcut-ref`来定义内联切点表达式。



为了定义advisor的优先级，可以使用`order`属性来定义advisor中的`Ordered`值。



### 5.5.7.  一个AOP Schema的例子

本章将展示如何在并发锁失败时重试的例子。

```java
public class ConcurrentOperationExecutor implements Ordered {

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int order = 1;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        PessimisticLockingFailureException lockFailureException;
        do {
            numAttempts++;
            try {
                return pjp.proceed();
            }
            catch(PessimisticLockingFailureException ex) {
                lockFailureException = ex;
            }
        } while(numAttempts <= this.maxRetries);
        throw lockFailureException;
    }
}
```

注意，切面实现了`Ordered`接口，以便可以设置切面的优先级高于事务通知（希望一个每次重试的时候都是一个新的事务）。属性`maxRetries`和`order`都可以通过Spring进行配置。主要的动作发生在`doConcurrentOperation`环绕通知方法。

| 该类与@AspectJ示例中使用的类相同，但是删除了注释。 |
| ----------------------------- |

相应的XML配置如下：

```xml
<aop:config>

    <aop:aspect id="concurrentOperationRetry" ref="concurrentOperationExecutor">

        <aop:pointcut id="idempotentOperation"
            expression="execution(* com.xyz.myapp.service.*.*(..))"/>

        <aop:around
            pointcut-ref="idempotentOperation"
            method="doConcurrentOperation"/>

    </aop:aspect>

</aop:config>

<bean id="concurrentOperationExecutor"
    class="com.xyz.myapp.service.impl.ConcurrentOperationExecutor">
        <property name="maxRetries" value="3"/>
        <property name="order" value="100"/>
</bean>
```

注意，目前假设所有服务都是幂等的。如果不是这样，我们可以通过引入幂等注解并使用该注解来注释服务操作的实现来改进切面，使其仅重试真正的幂等操作，如下面的示例所示:

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    // marker annotation
}
```

切面的更改仅重试幂等操作涉及到改进切入点表达式，以便仅@Idempotent操作匹配，如下所示：

```xml
<aop:pointcut id="idempotentOperation"
        expression="execution(* com.xyz.myapp.service.*.*(..)) and
        @annotation(com.xyz.myapp.service.Idempotent)"/>
```



## 5.6. 选择AOP声明方式

一旦确定了切面是实现给定需求的最佳方法，那么如何决定是使用Spring AOP还是使用AspectJ，是使用切面语言(代码)风格、@AspectJ注释风格还是使用Spring XML风格呢?这些决定受到许多因素的影响，包括应用程序需求、开发工具和团队对AOP的熟悉程度。



### 5.6.1. Spring AOP还是完整的AspectJ?

使用最简单的方法即可。Spring AOP比使用完整的AspectJ更简单，没有在开发和构建过程中引入AspectJ编译器/织入器。如果仅仅需要在Spring beans上通知操作的执行，Spring AOP值正确的选择。如果需要通过Spring容器通知那些未被管理的对象（例如领域对象），需要使用AspectJ。如果希望通知初简单方法执行以外的连接点（例如，字段的get/set连接点等等），就需要使用AspectJ。



当使用AspectJ时，需要选择AspectJ语言语法或@AspectJ注解风格。如果没有使用Java 5+，name可以选择使用代码样式。如果在设计中，切面扮演了重要的角色，并且能够使用Eclipse中的AspectJ Development Tools(AJDT)插件，AspectJ语言语法是首选。 它更干净、更简单，因为语言是专门为编写切面而设计的。 如果不使用Eclipse，或者只有少数切面在的应用程序中并且没有发挥主要作用，可能想要考虑使用@AspectJ风格，在IDE中坚持常规的Java编译，并在构建脚本中添加一个切面编织阶段。



### 5.6.2. 对于Spring AOP，使用@AspectJ还是XML

如果选择使用Spring AOP，可以选择@AspectJ或XML风格。这里有多种这种的考虑。



XML样式对已经存在的Spring用户来说非常熟悉，并且得到了真正的POJO支持。当使用AOP作为工具来配置企业级服务时，XML是一个好的选择（一个很好的测试是是否将切点表达式视为配置的一部分，而可能想独立更改）。使用XML样式，可以说从配置中可以更清楚地了解系统中存在哪些切面。



XML风格有两个缺点。第一，它没有完全将要解决的需求的实现封装在一个地方。DRY原则说，系统中的任何知识都应该有一个单一、明确、权威的表示形式。当使用XML风格时，关于如何实现需求的知识分散在支持Bean类的声明和配置文件中的XML中。当使用@AspectJ风格时，此信息将封装在一个模块中：切面。第二，XML风格在表达能力上受到更多限制：仅支持单例切面的实例化模型，并且无法组合以XML声明的命名切点。例如，在@AspectJ风格中，可以写出下面的代码：

```java
@Pointcut("execution(* get*())")
public void propertyAccess() {}

@Pointcut("execution(org.xyz.Account+ *(..))")
public void operationReturningAnAccount() {}

@Pointcut("propertyAccess() && operationReturningAnAccount()")
public void accountPropertyAccess() {}
```

在XML中，可以声明两个切点：

```xml
<aop:pointcut id="propertyAccess"
        expression="execution(* get*())"/>

<aop:pointcut id="operationReturningAnAccount"
        expression="execution(org.xyz.Account+ *(..))"/>
```

XML方法的缺点是不能通过组合这些定义来定义accountPropertyAccess切点。



@Aspect风格支持附加的实例化模型和更丰富的切点组合。它的有点是保持切面作为模块化单元。另一个有点是Spring AOP和AspectJ都可以理解@AspectJ切面（并因此使用）。所以，如果以后决定需要AspectJ的功能来实现其他需求，可以轻松的迁移到经典的AspectJ设置。总而言之，Spring团队在自定义方面更喜欢@AspectJ样式，而不是简单地配置企业服务。



## 5.7. 混合Aspect类型

通过使用自动代理支持、模式定义的`<aop:aspect>`切面、`<aop:advisor>`声明的顾问，甚至在同一配置中使用其他风格的代理和拦截器，完全有可能混合使用@AspectJ风格的切面。所有这些都是通过使用相同的底层支持机制实现的，可以毫无困难地共存。



## 5.8. 代理机制

Spring AOP使用JDK动态代理或CGLIB来创建指定目标对象的代理。JDK动态代理包含在JDK中，CGLIB代理是一个通用的开源库（被重新打包到`spring-core`）中。



如果要代理的目标对象至少实现了一个接口，JDK动态代理会被使用。目标类型实现的所有接口都会被代理。如果目标对象没有实现任何接口，就会使用CGLIB创建代理。



如果想要强制使用CGLIB代理（例如，为目标对象代理每个定义的方法，不仅仅是实现了接口的那些方法），就可以这样做。然而，应该考虑下面的问题：

* 使用CGLIB，不建议使用`final`方法，运行时生成的子类不会覆盖这些方法。

* Spring 4.0以后，代理对象的构造函数不会被调用两次，因为CGLIB代理实例已经通过Objenesis来创建。仅当JVM不允许绕过构造函数时，才可以从Spring的AOP支持中看到两次调用和相应的调用日志条目。

为了强制使用CGLIB代理，可以设置`<aop:config>`元素中的`proxy-target-class`属性为true：

```xml
<aop:config proxy-target-class="true">
    <!-- other beans defined here... -->
</aop:config>
```

当使用@AspectJ自动代理支持时，为了强制使用CGLIB代理，可以设置`<aop:aspectj-autoproxy>`中的`target-class`属性为`true`：

```xml
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

| 多个`<aop:config/>`部分在运行时折叠成一个统一的自动代理创建器，它应用任何`<aop:config/>`部分（通常来自不同的XML bean 定义文件）指定的最强的代理设置。这也适用于`<tx:annotation-driven/>`和`<aop:aspectj-autoproxy/>`元素。<br/><br/>为了更清晰，在`<tx:annotation-driven/>`上使用`<proxy-target-class="true">`或在`<aop:config/>`元素上使用会强制对所有三个元素使用CGLIB代理。 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |

### 5.8.1. 理解AOP代理

Spring AOP是基于代理的。在编写自己的切面或使用Spring框架随附的任何基于Spring AOP的切面之前，掌握最后一条语句实际含义的语义至关重要。



思考第一个情景，如下面的代码所示，在这里一个普通的，未经过代理的，无特殊要求的直接对象引用：

```java
public class SimplePojo implements Pojo {

    public void foo() {
        // this next method invocation is a direct call on the 'this' reference
        this.bar();
    }

    public void bar() {
        // some logic...
    }
}
```

如果在一个对象引用上调用一个方法，这个方法会直接在该对象引用上调用，如下图和清单所示:

![](https://raw.githubusercontent.com/Aris4009/attachment/main/20210104164413.png)



```java
public class Main {

    public static void main(String[] args) {
        Pojo pojo = new SimplePojo();
        // this is a direct method call on the 'pojo' reference
        pojo.foo();
    }
}
```

稍微做一下改变，客户端代码拥有一个代理的引用。思考如下的图示和代码片段

![](https://raw.githubusercontent.com/Aris4009/attachment/main/20210104164251.png)



```java
public class Main {

    public static void main(String[] args) {
        ProxyFactory factory = new ProxyFactory(new SimplePojo());
        factory.addInterface(Pojo.class);
        factory.addAdvice(new RetryAdvice());

        Pojo pojo = (Pojo) factory.getProxy();
        // this is a method call on the proxy!
        pojo.foo();
    }
}
```

理解这里的关键点是，`main(...)`方法中的客户端代码引用了一个代理。这意味着该对象引用上的方法调用是对代理的调用。结果是，代理可以委派给与该特定方法调用的相关的所有拦截器（通知）。然而，一旦调用最终到达目标对象(在本例中是SimplePojo引用)，它可能对自身进行的任何方法调用，例如this.bar()或this.foo()，都将针对该引用而不是代理调用。这具有重要的意义。它意味着自身调用不会导致与方法调用相关的通知得到运行的机会。



那应该怎么办呢？最好的办法是，重构代码，以免发生自调用。这缺失需要做一些工作，但这是做好的，侵入性最小的方法。下面的方法绝对是可怕的。可以将类中的逻辑与Spring AOP绑定在一起:

```java
public class SimplePojo implements Pojo {

    public void foo() {
        // this works, but... gah!
        ((Pojo) AopContext.currentProxy()).bar();
    }

    public void bar() {
        // some logic...
    }
}
```

这完全将代码与Spring AOP耦合在一起，并且使类本身意识到它是在AOP上下文中使用的，这与AOP截然不同。当代理被创建时，它要求一些附加的配置：

```java
public class Main {

    public static void main(String[] args) {
        ProxyFactory factory = new ProxyFactory(new SimplePojo());
        factory.addInterface(Pojo.class);
        factory.addAdvice(new RetryAdvice());
        factory.setExposeProxy(true);

        Pojo pojo = (Pojo) factory.getProxy();
        // this is a method call on the proxy!
        pojo.foo();
    }
}
```

最后，必须指出，AspectJ没有此自调用问题，因为它不是基于代理的AOP框架。



## 5.9. 编程方式创建@AspectJ代理

除了通过使用`<aop:config>`或`<aop:aspectj-autoproxy>`在配置中声明切面，也可以通过编程的方式来创建代理以便通知目标对象。更多完整的细节，可以参考Spring的AOP API。这里，主要关注通过使用@AspectJ切面来自动创建代理的能力。



可以使用`org.springframework.aop.aspectj.annotation.AspectJProxyFactory`类来为一个或多个@AspectJ切面通知的目标对象创建代理。这是一个非常简单的用法：

```java
// create a factory that can generate a proxy for the given target object
AspectJProxyFactory factory = new AspectJProxyFactory(targetObject);

// add an aspect, the class must be an @AspectJ aspect
// you can call this as many times as you need with different aspects
factory.addAspect(SecurityManager.class);

// you can also add existing aspect instances, the type of the object supplied must be an @AspectJ aspect
factory.addAspect(usageTracker);

// now get the proxy object...
MyInterfaceType proxy = factory.getProxy();
```



## 5.10. 通过Spring Applications使用AspectJ

到目前为止，本章介绍的所有内容都是纯Spring AOP。如果需求超出了Spring AOP单独提供的功能，将研究如何使用AspectJ编译器或织入器来代替Spring AOP。



Spring附带了一个小的AspectJ切面库，在发行版中可以单独使用`spring-aspects.jar`。需要将它添加到classpath中以便使用切面功能。`使用AspectJ通过Spring来注入领域对象`和`AspectJ的其他Spring切面`讨论了这个库的内容并且展示了如何使用它。`通过Spring IoC来配置AspectJ切面`讨论了如何依赖注入使用了AspectJ编译器编织的AspectJ切面。最终，`在Spring框架中通过AspectJ载入织入`介绍了使用AspectJ的Spring应用程序的加载时编织。



### 5.10.1. 使用AspectJ来依赖注入Spring中的领域对象

在应用程序上下文中，Spring容器负责实例化和配置beans定义。指定应用配置的bean

定义名称，可能会要求bean工厂来配置一个预先存在的对象。`spring-aspects.jar`包含了一个注解驱动的切面，利用此功能允许依赖注入任何对象。该功能旨在用于任何容器的控制范围之外创建的对象。领域对象通常属于此类，因为他们通常是通过数据库查询的结果，使用`new`运算符或ORM工具以编程方式创建。



注解`@Configurable`将一个类标记为合格的Spring驱动配置。在这个简单的例子中，可以将其纯粹用作标记注释：

```java
package com.xyz.myapp.domain;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class Account {
    // ...
}
```

当以这种方式作为标记接口使用时，Spring通过使用与完全限定类型名称(com.xyz.myapp.domain.Account)相同的bean定义(通常是原型作用域)来配置注释类型(在本例中是Account)的新实例。由于bean的默认名称是其类型的全限定名，因此，声明原型定义的便捷方法是省略`id`属性：

```xml
<bean class="com.xyz.myapp.domain.Account" scope="prototype">
    <property name="fundsTransferService" ref="fundsTransferService"/>
</bean>
```

如果要明确指定原型bean definition的名称来使用，可以直接在注解上定义名称：

```java
package com.xyz.myapp.domain;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable("account")
public class Account {
    // ...
}
```

Spring现在寻找一个名为account的bean定义，并将其用作配置新Account实例的定义。
