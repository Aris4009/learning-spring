# 6. Spring AOP APIs

前面的章节描述了通过使用@AspectJ和基于schema的切面定义为Spring提供AOP支持。在这个章节中，将要塔伦底层的Spring AOP APIs。对于普通的应用程序，建议通过使用Spring AOP的AspectJ切点来描述之前的章节。

## 6.1. Spring中的切点API

本章描述了Spring如何处理重要的切点概念

### 6.1.1. 概念

Spring的切点模型是切点可独立于通知类型来达到重用目的。可以在不同的切点上使用相同的切点。



`org.springframework.aop.Pointcut`接口是中心接口，用来将通知定向到特定的类和方法上。完整的接口如下：

```java
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();

}
```

`Pointcut`接口分为两部分，目的是允许重用类和方法匹配部分，细粒度控制组合操作（例如与另一个方法匹配器执行`union`操作）。



`ClassFilter`接口用来将切点限制为指定的一组目标类。如果`matches()`方法总是返回true，所有目标类将被匹配。下面列出了这个接口的定义：

```java
public interface ClassFilter {

    boolean matches(Class clazz);
}
```

`MethodMatcher`接口通常更重要。这个接口的完整定义如下：

```java
public interface MethodMatcher {

    boolean matches(Method m, Class targetClass);

    boolean isRuntime();

    boolean matches(Method m, Class targetClass, Object[] args);
}
```

`matches(Medhod,Class)`方法用来测试切点是否与目标类上的指定方法匹配。当AOP代理被创建时，可以执行此评估以避免对每个方法调用进行测试。如果两个参数的`matches`方法对于指定的方法返回`true`，并且`isRuntime()`方法返回`true`，在每个方法调用时，三个参数的matches会被调用。这让切点可以在目标通知开始之前立即查看传递的方法调用的参数。



大多数`MethodMatcher`实现是静态的，意味着他们的`isRuntime()`方法返回`false`。在这个例子中，三个参数的`matches`方法永远不会被调用。



*如果可能，尝试让切点变为静态的，当AOP代理被创建时，允许AOP框架缓存切点的评估结果。*



### 6.1.2. 切点操作

Spring支持在切点上进行操作（联合和交叉）。



联合意味着方法匹配任意一个切点。交叉意味着方法匹配所有切点。联合通常来说更有用。可以通过使用`org.springframework.aop.support.Pointcuts`类中的静态方法或通过使用`OmposablePointcut`类中的方法来组合切点。但是，使用AspectJ切点表达式通常来说是更简单的途径。



### 6.1.3. AspectJ切点表达式

从Spring2.0以后，`org.springframework.aop.aspectj.AspectJExpressionPointcut`是Spring使用的最重要的切点类型。这是一个使用AspectJ支持的库来解析AspectJ切点表达式字符串的切点。



### 6.1.4. 方便的切点实现

Spring提供一些方便的切点实现。可以直接使用他们；其他的则打算在特定于应用程序的切入点中子类化。



**静态切点**

静态切点是基于方法和目标类的，并且不能挈带方法的参数。静态切点满足大多数的用途，并且最好。当方法第一次被调用时，Spring仅评估一次静态切点。在那以后，对于每个方法的调用，不再需要评估切点。



本节的其余部分描述了Spring附带的一些静态切点实现。



**正则表达式切点**

指定静态切点的一种明显方法是正则表达式。除了Spring框架之外，还有几个AOP框架使之成为可能。

`org.springframework.aop.support.JdkRegexpMethodPointcut`是一个通用的正则表达式切点，用于在JDK中支持正则表达式。



使用`JdkRegexpMethodPointcut`类，可以提供一个匹配字符串列表。如果他们中的任何一个配匹配，切点评估为`true`。（因此，最终的切点模式等效于联合）。

下面是如何使用`JdkRegexpMethodPointcut`的例子：

```java
<bean id="settersAndAbsquatulatePointcut"
        class="org.springframework.aop.support.JdkRegexpMethodPointcut">
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```



Spring提供了一个方便的类名叫`RegexpMethodPointcutAdvisor`，让用户可以引用一个`Advice`（记住，这个`Advice`可以是一个拦截器、before advice,throw advice、或其他）。在幕后，Spring使用`JdkRegexpMethodPointcut`。使用`RegexpMethodPointcutAdvisor`简化了连接，因为一个bean同时封装了切入点和通知，如下面的示例所示:

```xml
<bean id="settersAndAbsquatulateAdvisor"
        class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
    <property name="advice">
        <ref bean="beanNameOfAopAllianceInterceptor"/>
    </property>
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```

可以对任何通知类型使用`RegexpMethodPointcutAdvisor`。



**属性驱动切点**

静态切点的一个重要类型是元数据驱动的切点。这将使用元数据属性的值（通常是源码级别的元数据）。



**动态切点**

动态切点比静态切点更昂贵。他们考虑了方法参数以及静态信息。这意味着他们必须对每个方法的调用进行评估并且由于参数不同，因此无法缓存结果。



主要的例子是`control flow`切点。



**Control Flow Pointcuts 控制流切点**

Spring控制流切点与AspectJ`cflow`切点概念上相似，尽管比AspectJ功能弱。（目前没有办法指定切点在与另一个切点匹配的连接点下运行）。控制流切点匹配当前调用栈。例如，如果连接点是通过在`com.mycompany.web`包中的方法或通过`SomeCaller`类调用的，则可能会触发。通过使用`org.springframework.aop.support.ControlFlowPointcut`类来指定控制流切点。

| 控制流切点与其他动态切点相比，在运行时评估成本高得多。在Java1.4中，它是其他动态切点花费成本的5倍。 |
| ----------------------------------------------------- |



### 6.1.5. 切点超类

Spring提供了有用的切点超类来帮助实现自定义的切点。



因为静态切点最有用，应该尽可能子类化`StaticMethodMatcherPointcut`。这要求仅实现一个抽象方法（尽管可以覆盖其他方法来自定义行为）。下面的例子展示了如何子类化`StaticMethodMatcherPointcut`：

```java
class TestStaticPointcut extends StaticMethodMatcherPointcut {

    public boolean matches(Method m, Class targetClass) {
        // return true if custom criteria match
    }
}
```



### 6.1.6. 自定义切点

因为在Spring AOP中的切点是Java类而不是语言功能（如AspectJ），可以声明自定义切点，无论是静态的还是动态的。Spring中的自定义切点可以是任意复杂的。然而，建议尽可能使用AspectJ切点表达式语言。

| Spring后续的版本可能通过JAD来支持意义切点-例如，更改目标对象中的实例变量的所有方法。 |
| ----------------------------------------------- |


