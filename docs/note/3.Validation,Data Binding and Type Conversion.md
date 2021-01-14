3. 验证，数据绑定和类型转换

考虑将验证作为业务逻辑有利有弊，并且Spring提供一个了一种验证（和数据绑定）设计，但并不排除其中任何一个。具体来说，验证不应该与Web层绑定，应该易于本地化，应该可以插入任何可用的验证器。考虑到这些问题，Spring提供了一个`Validator`合约，该合约在应用程序的每一层都是基础的并且非常有用。

数据绑定让用户输入动态绑定到应用程序的领域模型（或用于处理用户输入的任何对象）。Spring提供了命名恰当的`DataBinder`的功能。`Validator`和`DataBinder`组成了`validation`包，主要用于网络层，但也不局限于网络层。

在Spring框架中，`BeanWrapper`是一个基础概念，它用在了很多地方。但是，用户不需要直接使用`BeanWrapper`。但是，因为这是参考文档，因此可能需要进行一些解释。在本章中将会解释`BeanWrapper`，因为如果要使用它，那么在尝试数据绑定到对象最有可能使用它。

Spring的`DataBinder`和底层的`BeanWrapper`都使用`PropertyEditorSupport`实现来解析和格式化属性值。`PropertyEditor`和`PropertyEditorSupport`类是JavaBeans规范的一部分并且在本章中会进行解释。Spring 3引入了`core.convert`包，提供一个通用的类型转换工具，以及用于格式化UI字段值的高级"format"包。可以使用这些包来用作`PropertyEditorSupport`实现更简单的替代方案。本章还将对他们进行讨论。

Spring支持通过设置基础结构和Spring自己的`Validator`合约适配器来支持Java Bean验证。应用程序可以全局启用一次Bean验证，如`Java Bean Validation`中所述，并将其专用于所有验证需求。在web层，应用程序可以对每个`DataBinder`注册本地控制器的Spring `Validator`实例，如`Configuring a DataBinder`中描述的一样，这对于自定义验证逻辑插件很有用。

## 3.1. 通过Spring的`Validator`接口验证

Spring的`Validator`接口可以用来验证对象。`Validator`接口通过使用`Error`对象工作，以便在验证时，验证器可以将验证失败报告给`Errors`对象。

思考以下小型数据对象的例子：

```java
public class Person {

    private String name;
    private int age;

    // the usual getters and setters...
}
```

下面的例子通过实现`org.springframework.validation.Validator`接口的两个方法，来提供对`Person`类的验证行为：

- `supports(Class)`:该验证器可以验证提供的类的实例吗？

- `validate(Object,org.springframework.validation.Errors)`:验证给定的对象，如果验证发生错误，请使用给定的`Errors`对象注册那些错误。

实现一个`Validator`相当直接，特别是当了解Spring框架提供的`ValidationUtils`辅助类。下面的例子为`Person`实现了`Validator`实例：

```java
public class PersonValidator implements Validator {

    /**
     * This Validator validates only Person instances
     */
    public boolean supports(Class clazz) {
        return Person.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "name", "name.empty");
        Person p = (Person) obj;
        if (p.getAge() < 0) {
            e.rejectValue("age", "negativevalue");
        } else if (p.getAge() > 110) {
            e.rejectValue("age", "too.darn.old");
        }
    }
}
```

`ValidationUtils`类上的`static rejectIfEmpty(...)`方法用来拒绝如果`name`属性是`null`或空字符串。看一下`ValidationUtils`文档，除了前面例子展示的功能，还提供了那些功能。

虽然可以实现单个`Validator`类来验证丰富对象中的每个嵌套对象，但最好在其自己的`Validator`实现中封装对象的每个嵌套类的验证逻辑。一个”丰富“对象可能是由两个`String`属性和一个复杂的`Address`对象组合而成的`Customer`对象。`Address`对象可能独立于`Customer`对象使用，所以，实现了一个`AddressValidator`。如果希望`CustomerValidator`重用`AddressValidator`类中包含的逻辑，不需要复制粘贴，可以使用依赖注入或通过`CustomerValidator`实例化一个`AddressValidator`：

```java
public class CustomerValidator implements Validator {

    private final Validator addressValidator;

    public CustomerValidator(Validator addressValidator) {
        if (addressValidator == null) {
            throw new IllegalArgumentException("The supplied [Validator] is " +
                "required and must not be null.");
        }
        if (!addressValidator.supports(Address.class)) {
            throw new IllegalArgumentException("The supplied [Validator] must " +
                "support the validation of [Address] instances.");
        }
        this.addressValidator = addressValidator;
    }

    /**
     * This Validator validates Customer instances, and any subclasses of Customer too
     */
    public boolean supports(Class clazz) {
        return Customer.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "surname", "field.required");
        Customer customer = (Customer) target;
        try {
            errors.pushNestedPath("address");
            ValidationUtils.invokeValidator(this.addressValidator, customer.getAddress(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
```

验证错误会通过验证器报告给`Errors`对象。在Spring Web MVC的情况下，可以使用`<spring:bind/>`标签来检查错误消息，但是也可以自己检查`Errors`对象。更多方法请查看提供的javadoc。

## 3.2. 将代码解析为错误消息

之前介绍了数据绑定和验证。这部分涵盖了验证错误对应的输出消息。在前面例子中，拒绝了名称和年龄字段。如果想要当拒绝字段发生时（"name"和"age"）通过使用`MessageSource`来输出错误消息，可以使用提供的错误代码。当调用（使用`ValidationUtils`类直接或者间接调用）`rejectValue`或其他`Errors`接口中的`reject`方法时，底层实现不仅注册用户传入的代码，而且还注册了许多其他错误代码。`MessageCodesResolver`来决定接口`Errors`的错误代码。默认情况下，使用`DefaultMessageCodesResolver`，它不仅通过用户给定代码注册了消息，而且注册了包含传递给reject方法的字段名称的消息。因此，如果通过使用`rejectValue("age","too.darn.old")`来拒绝，除了来自`too.darn.old`代码，Spring也注册了`too.darn.old.age`和`too.darn.old.age.int`（第一个包含了字段名，第二个包含了字段类型）。这样做是为了方便开发人员在定位错误时提供帮助。

更多`MessageCodesResolver`和默认策略可以在`MessageCOdesResolver`和`DefaultMessageCOdesResolver`的javadoc中找到。

## 3.3. Bean操作和`BeanWrapper`

`org.springframework.beans`包遵循JavaBeans 标准。一个JavaBean是一个带有默认无参构造器的类，并且遵循命名约定，在该约定中，例如，名为`bingoMadness`的属性将具有setter方法`setBingoMadness(..)`和getter方法`getterBingoMadness()`。更多JavaBeans和规范，可以参考`javabeans`。

在beans包中，一个非常重要的类是`BeanWrapper`接口和它的对应实现(BeanWrapperImpl)。正如javadoc中引用的，`BeanWrapper`提供了设置和获取属性值（单独或批量）的功能，获取属性描述符，并查询属性以确定他们是可读还是可写的。`BeanWrapper`也提供嵌套属性的支持，能够设置在子属性上的属性并且深度无限。`BeanWrapper`还支持添加标准JavaBeans `PropertyChangeListeners`和`VetoableChangeListeners`的功能，而无需在目标类中支持代码。最后，但并非不重要的一点是，`BeanWrapper`支持设置索引属性。`BeanWrapper`通常不直接由应用程序代码使用，而是由`DataBinder`和`BeanFactory`使用。

`BeanWrapper`的工作方式部分由其名称指示：它包装一个bean来对该bean执行操作，例如设置和检索属性。

### 3.3.1. 设置、获取、嵌套属性

设置和获取属性是通过`BeanWrapper`的`setPropertyValue`和`getPropertyValue`重载方法变体完成的。如果想要查看细节，请参考Javadoc。下面的表格展示了这些约定的实例：

**Examples of properties**

| Expression           | Explanation                                                                  |
| -------------------- | ---------------------------------------------------------------------------- |
| `name`               | 指与`getName()`、`isName()`、`setName(..)`方法对应的属性                                |
| account.name         | 指与`getAccount().setName()`、`getAccount().getName`方法对应的`account`属性的嵌套`name`属性 |
| account[2]           | 指被索引属性`account`的第三个元素。被索引属性可以是`array`，`list`或其他自然排序的集合                       |
| account[COMPANYNAME] | 指`account``Map`的索引为`COMPANYNAME`key的属性值                                      |

（如果不打算直接使用`BeanWrapper`，那么下面的内容就不那么重要。如果只使用`DataBinder`和`BeanFactory`和他们的默认实现，可以直接跳到`PropertyEditors`部分）

下面的两个示例类使用`BeanWrapper`来获取和设置属性：

```java
public class Company {

    private String name;
    private Employee managingDirector;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getManagingDirector() {
        return this.managingDirector;
    }

    public void setManagingDirector(Employee managingDirector) {
        this.managingDirector = managingDirector;
    }
}
```

```java
public class Employee {

    private String name;

    private float salary;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }
}
```

接下来的代码片段展示了如何获取和操纵`Companies`和`Employees`实例的一些属性：

```java
BeanWrapper company = new BeanWrapperImpl(new Company());
// setting the company name..
company.setPropertyValue("name", "Some Company Inc.");
// ... can also be done like this:
PropertyValue value = new PropertyValue("name", "Some Company Inc.");
company.setPropertyValue(value);

// ok, let's create the director and tie it to the company:
BeanWrapper jim = new BeanWrapperImpl(new Employee());
jim.setPropertyValue("name", "Jim Stravinsky");
company.setPropertyValue("managingDirector", jim.getWrappedInstance());

// retrieving the salary of the managingDirector through the company
Float salary = (Float) company.getPropertyValue("managingDirector.salary");
```

### 3.3.2. 内置的`PropertyEditor`实现

Spring使用一个`PropertyEditor`的概念来实现`Object`和`String`之间的转换。以不同于对象本身的方式表示属性可能很方便。例如，一个`Date`可以以人类可读的方式表示（使用字符串表示：`2007-14-09`），虽然仍然可以将人类可读的形式转换回原始日期（或者更好的是，将人类可读形式输入的任何日期转换回`Date`对象）。可以注册一个自定义的`java.beans.PropertyEditor`可以实现此行为。在`BeanWrapper`上或特定的IoC容器中注册自定义编辑器（如上一章所述），使他具备如何将属性转换为所需类型的能力。更多关于`PropertyEditor`，请参考[the javadoc of the java.beans package from Oracle](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html).

在Spring中使用属性编辑的几个示例：

- 使用`PropertyEditor`实现在bean上设置属性。当在XML文件中声明使用`String`作为bean的属性值时，Spring（如果相应属性的setter上具有`Class`参数）使用`ClassEditor`来视图把参数解析为一个`Class`对象。

- 通过使用多种`PropertyEditor`的实现，解析在Spring的MVC框架中的HTTP请求参数，可以在`CommandController`中的所有子类中手动绑定这些实现。

Spring具有许多内置的`PropertyEditor`实现。他们都被放置在`org.springframework.beans.propertyeditors`包中。默认情况下，大多数（但不是全部，如下表所示）由`BeanWrapperImpl`注册。如果可以通过某种方式配置属性编辑器，则仍然可以注册自己的变体来覆盖默认的。下面的表格描述了Spring提供的 `PropertyEditor`多种实现：

| Class                     | Explanation                                                                                                                                      |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `ByteArrayPropertyEditor` | 字节数组编辑器。将字符串转换为相应的字节表示。默认通过`BeanWrapperImpl`注册。                                                                                                  |
| `ClassEditor`             | 把字符串解析为其表示的实际类或把实际的类转换为相应的字符串。当没有找到类时，会抛出`IllegalArgumentException`。默认情况下，通过`BeanWrapperImpl`注册。                                                 |
| `CustomeBooleanEditor`    | 对`Boolean`属性的可定制的属性编辑器。默认情况下，通过`BeanWrapperImp`注册，但是可以通过注册一个自定义的编辑器的实例的注册，来覆盖它。                                                                  |
| `CustomeCollectionEditor` | 集合框架的属性编辑器，可以转换任意`Collection`为指定的目标`Collection`类型。                                                                                               |
| `CustomDateEditor`        | 对`java.util.Date`的可定制的属性编辑器，支持自定义`DateFormat`。默认情况下不注册。需要用户根据需要来注册。                                                                              |
| `CustomNumberEditor`      | 对于任何`Number`子类的可定制的属性编辑器，例如`Integer`，`Long`，`Float`，或`Double`。默认情况下，通过`BeanWrapperImpl`注册，但是可以通过自定义的编辑器实例来覆盖。                                    |
| `FileEditor`              | 解析字符串到`java.io.File`对象。默认情况下，通过`BeanWrapperImpl`注册。                                                                                              |
| `InputStreamEditor`       | 单向属性编辑器，可以接受字符串并产生（通过中间的`ResourceEditor`和`Resource`）一个`InputStream`，以便将`InputStream`属性直接设置为字符串。注意默认用法不会关闭`InputStream`。默认下通过`BeanWrapperImpl`注册。 |
| `LocaleEditor`            | 可以解析字符串到`Locale`对象或者解析`Locale`到字符串（字符串的格式为`[country][variant]`,与`Locale`的`toString()`方法相同）。默认情况下，通过`BeanWrapperImpl`注册。                          |
| `PatternEditor`           | 可以解析字符串到`java.util.regex.Pattern`对象，反之亦然。                                                                                                        |
| `PropertiesEditor`        | 转换字符串（使用`java.util.Properties`类的javadoc中定义的格式进行格式化)到`Properties`对象。默认情况下，通过`BeanWrapperImpl`注册。                                                  |
| `StringTrimmerEditor`     | 修剪字符串的属性编辑器。允许将空字符串转换为`null`。默认不注册，需要用户自己注册。                                                                                                     |
| `URLEditor`               | 可以解析表示URL的字符串到实际的`URL`对象。默认情况下，通过`BeanWrapperImpl`注册。                                                                                            |

Spring使用`java.beans.PropertyEditorManager`来设置可能需要的属性编辑器查找路径。这个查找路径包括`sun.bean.editors`,它包含了`Font`，`Color`和大多数原始类型的`PropertyEditor`实现。还要注意，如果JavaBeans基础结构与他们的类在同一包中并且该类具有相同的名称，并附加了Editor，则标准JavaBeans基础结构会自动发现`PropertyEditor`类（无需显示注册他们）。例如，可能具有以下类和包结构，足以识别`SomethingEditor`类并将其作为`Something`类型的属性的`PropertyEditor`。

```
com
  chank
    pop
      Something
      SomethingEditor // the PropertyEditor for the Something class
```

注意，也可以使用标准的`BeanInfo`JavaBeans机制。下面的例子使用`BeanInfo`机制来将一个或更多`PropertyEditor`实例显示注册到关联类的属性：

```
com
  chank
    pop
      Something
      SomethingBeanInfo // the BeanInfo for the Something class
```

所引用的`SomethingBeanInfo`类的以下Java源代码将`CustomNumberEditor`与`Something`类的age属性相关联：

```java
public class SomethingBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyEditor numberPE = new CustomNumberEditor(Integer.class, true);
            PropertyDescriptor ageDescriptor = new PropertyDescriptor("age", Something.class) {
                public PropertyEditor createPropertyEditor(Object bean) {
                    return numberPE;
                };
            };
            return new PropertyDescriptor[] { ageDescriptor };
        }
        catch (IntrospectionException ex) {
            throw new Error(ex.toString());
        }
    }
}
```

**注册其他自定义`PropertyEditor`实现**

当作为字符串来设置bean属性时，Spring的IoC容器最终使用标准JavaBeans的`PropertyEditor`实现来将那些字符串转换为复杂类型的属性。Spring预先注册许多自定义的`PropertyEditor`实现（例如，将一个使用字符串表示的类名转换为一个`Class`对象）。另外，Java的标准JavaBeans`PropertyEditor`查找机制允许适当的命名类的`PropertyEditor`，并将与提供支持的类放在同一包中，以便可以自动找到它。

如果需要注册另外的自定义`PropertyEditors`，则可以使用几种机制。最手动的方法（通常使用起来不方便并且不建议使用）是使用`ConfigurableBeanFactory`接口的`registerCustomEditor()`方法，假设有`BeanFactory`的引用。另一种机制（稍微方便一些）是使用一个名为`CustomEditorConfigurer`的特定的bean factory post-processor。尽管可以通过`BeanFactory`的实现使用bean factory post-processors，`CustomEditorConfigurer`有一个嵌套属性设置，所以强烈建议通过`ApplicationContext`来使用它，可以在其中以类似于其他任何bean的方式部署它，并可以在其中自动检测和应用它。

注意，所有的bean工厂和应用程序上下文自动使用一系列内置的属性编辑器，尽管使用`BeanWrapper`来处理属性转换。标准的`BeanWrapper`注册的属性编辑器在上一节里被列出。另外，`ApplicationContexts`还以适合特定应用程序上下文类型的方式重写或添加其他编辑器，以处理资源查找。

标准JavaBeans`PropertyEditor`实例用来将表示为字符串的属性值转换为实际的复杂类型的属性。可以使用`CustomEditorConfigurer`，一个bean factory post-processor，来方便地在`ApplicationContext`上对附加的`PropertyEditor`实例增加支持。

考虑下面的例子，定义了一个名为`ExoticType`的用户类和另一个名为`DependsOnExoticType`，并且需要设置`ExoticType`属性：

```java
package example;

public class ExoticType {

    private String name;

    public ExoticType(String name) {
        this.name = name;
    }
}

public class DependsOnExoticType {

    private ExoticType type;

    public void setType(ExoticType type) {
        this.type = type;
    }
}
```

正确设置之后，希望能够将type属性分配为字符串，`PropertyEditor`会将其转换为实际的`ExoticType`实例。一下bean定义显示了如何建立这种关系：

```xml
<bean id="sample" class="example.DependsOnExoticType">
    <property name="type" value="aNameForExoticType"/>
</bean>
```

`PropertyEditor`实现看起来和下面的例子类似：

```java
// converts string representation to ExoticType object
package example;

public class ExoticTypeEditor extends PropertyEditorSupport {

    public void setAsText(String text) {
        setValue(new ExoticType(text.toUpperCase()));
    }
}
```

最终，下面的例子展示了如何使用`CustomEditorConfigurer`通过`ApplicationContext`注册一个新的`PropertyEditor`,然后在需要的时候使用：

```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="customEditors">
        <map>
            <entry key="example.ExoticType" value="example.ExoticTypeEditor"/>
        </map>
    </property>
</bean>
```

**使用`PropertyEditorRegistrar`**

另一种机制是通过Spring容器来创建注册的属性编辑器。并且使用`PropertyEditorRegistrar`。当需要使用相同属性编辑器在不同场景下，特别有用。可以编写一个对应的祖册器，并且在每个情况下重用它。`PropertyEditorRegistrar`实例和一个名为`PropertyEditorRegistry`的接口、一个通过Spring`BeanWrapper`(和`DataBinder`)实现的接口一起结合使用。`PropertyEditorRegistrar`实例当和具有`setPropertyEditorRegistrars(..)方法的名为``CustomEditorConfigurer`结合使用特别方便。以这种方式添加到`CustomEditorConfigurer`中的`PropertyEditorRegistrar`实例可以轻松地与`DataBinder`和Spring MVC 控制器共享。而且，它避免了在自定义编辑器上进行同步的需要：`PropertyEditorRegistrar`应该每次创建bean时尝试创建新的`PropertyEditor`实例。

下面的例子展示了如何创建自己的`PropertyEditorRegistrar`实现：

```java
package com.foo.editors.spring;

public final class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {

        // it is expected that new PropertyEditor instances are created
        registry.registerCustomEditor(ExoticType.class, new ExoticTypeEditor());

        // you could register as many custom property editors as are required here...
    }
}
```

另请参见`org.springframework.beans.support.ResourceEditorRegistrar`以获取示例`PropertyEditorRegistrar`实现。注意，在实现`registerCustomEditors(..)`方法时，它是如何创建每个属性编辑器的新实例的。

下面的例子展示了如何配置一个`CustomerEditorConfigurer`并且注入一个`CustomPropertyEditorRegistrar`实例：

```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="propertyEditorRegistrars">
        <list>
            <ref bean="customPropertyEditorRegistrar"/>
        </list>
    </property>
</bean>

<bean id="customPropertyEditorRegistrar"
    class="com.foo.editors.spring.CustomPropertyEditorRegistrar"/>
```

最终（对于使用Spring的MVC框架的用户而言，与本章重点有所不同），将`PropertyEditorRegistrars`与数据绑定控制器（例如`SimpleFormController`）结合使用会非常方便。下面的示例在`initBinder(..)`方法的实现中使用`PropertyEditorRegistrar`：

```java
public final class RegisterUserController extends SimpleFormController {

    private final PropertyEditorRegistrar customPropertyEditorRegistrar;

    public RegisterUserController(PropertyEditorRegistrar propertyEditorRegistrar) {
        this.customPropertyEditorRegistrar = propertyEditorRegistrar;
    }

    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        this.customPropertyEditorRegistrar.registerCustomEditors(binder);
    }

    // other methods to do with registering a User
}
```

这种样式的`PropertyEditor`注册可以导致代码简洁（`initBinder(..)`的实现只有一行长），并且可以将通用的`PropertyEditor`注册代码封装在一个类中，然后根据需要在许多Controller之间共享。

## 3.4. Spring类型转换

Spring 3中引入了`core.convert`包，提供了一个通用类型转换系统。这个系统定义了一个SPI来实现类型转换的逻辑并且在运行时通过API来执行类型转换。在Spring容器中，可以使用这个系统来作为`PropertyEditor`实现的替代方法，以将外部的bean属性值字符串转换为所需的属性类型。可以在应用程序中的任何需要类型转换的地方使用公共API。

### 3.4.1. 转换器SPI

SPI实现类型转换逻辑很简单，且类型严格，如以下接口定义所示：

```java
package org.springframework.core.convert.converter;

public interface Converter<S, T> {

    T convert(S source);
}
```

为了创建自己的转换器，需要实现`Conterter`接口，`S`表示需要转换的类型，`T`表示转换后的类型。如果一个集合或数组`S`需要转换为一个集合或数组`T`，还需要注册一个委托集合或数组转换器（默认情况下`DefaultConversionService`会这样做），则可以透明的应用此类转换器。

对于每次调用`convert(S)`,原始参数保证不能为null。用户的`Converter`在转换失败时可能抛出任何未检查异常。尤其是，它应该抛出一个IllegalArgumentException来报告无效的原始值。小心确保`Converter`的实现是线程安全的。

在`core.convert.support`保重提供了一些方便的转换器实现。这些转换器包含了从字符串到数字和其他通用类型。下面列出了`StringToInteger`类，一个典型的`Converter`实现：

```java
package org.springframework.core.convert.support;

final class StringToInteger implements Converter<String, Integer> {

    public Integer convert(String source) {
        return Integer.valueOf(source);
    }
}
```

### 3.4.2. 使用`ConverterFactory`

当需要集中整个类层次结构的转换逻辑时（例如，从`String`转换为`Enum`对象时），可以实现`ConverterFactory`：

```java
package org.springframework.core.convert.converter;

public interface ConverterFactory<S, R> {

    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
```

S表示需要转换的类型，R是基本类型，用来定义可以转换的类的范围。然后，`getConverter(Class targetType)`中的T，表示它是R的子类。

思考`StringToEnumConverterFactory`的例子：

```java
package org.springframework.core.convert.support;

final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter(targetType);
    }

    private final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        public T convert(String source) {
            return (T) Enum.valueOf(this.enumType, source.trim());
        }
    }
}
```

### 3.4.3. 使用`GenericConverter`

如果需要一个复杂`Converter`实现，考虑使用`GenericConverter`接口。与`Converter`相比，它更灵活，但是签名的类型不是很严格，一个`GenericConverter`支持多个源和目标类型的转换。此外，当实现逻辑转换时，`GenericConverter`可以提供可用的源字段和目标字段上下文。这种上下文使类型转换由字段注释或字段签名的通用信息来驱动。下面列出了`GenericConverter`接口的定义：

```java
package org.springframework.core.convert.converter;

public interface GenericConverter {

    public Set<ConvertiblePair> getConvertibleTypes();

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
}
```

要实现`GenericConverter`，请让`getConvettibleTypes()`返回支持的源-目标类型对。然后实现`convert(Object source,TypeDescriptor,TypeDescriptor)`的转换逻辑。源`TypeDescriptor`提供持有被转换值的访问。目标`TypeDescriptor`提供可以访问要设置转换值的目标字段。

一个好的`GenericConverter`可以在Java数组和集合之间进行转换。`ArrayToCollectionConverter`会对声明目标集合类型的字段进行内省，以解析集合的元素类型。这样一来，源数组中的每个元素就可以在转换之前转换为集合元素类型。

*因为`GenericConverter`*是一个更富在的SPI接口，所以仅当需要时再使用它。在需要基本类型转换时，请使用`Converter`或`ConverterFactory`。

**使用`ConditionalGenericConverter`**

有时，希望尽在满足特定条件时才运行`Converter`。例如，可能希望目标字段包含特定注解时，或在目标类上定义了特定方法（例如一个`static valueOf`方法）时，才运行`Converter`。`ConditionalGenericConverter`是联合了`GenericConverter`和`ConditionalConverter`接口，可以自定义这样的匹配条件：

```java
public interface ConditionalConverter {

    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}

public interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {
}
```

`ConditionalGenericConverter`的一个很好的例子是`EntityConverter`，它在持久实体标识符和实体引用之间进行转换。仅当目标实体类型声明了一个静态的finder方法（例如，`findAccount(Long)`），`EntityConverter`才能进行转换。可以在实现`matcher(TypeDescriptor,TypeDescriptor)`中，执行一个finder方法的检查。

### 3.4.4. `ConversionService`API

`ConversionService`定义了统一的API，在运行时执行类型转换。转换器通常在外观接口后运行：

```java
package org.springframework.core.convert;

public interface ConversionService {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);

    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

}
```

多数`ConversionService`的实现也实现了提供了转换器注册的SPI的`ConverterRegistry`。在内部，`ConversionService`实现委托它的注册转换器来承担转换逻辑。

在包`core.convert.support`中，提供了一个健壮的`ConversionService`实现。`GenericConversionService`是适用于大多数环境的通用实现。`ConversionServiceFactory`提供一个创建通用`ConversionService`配置的工厂。

### 3.4.5. 配置`ConversionService`

`ConversionService`是一个无状态对象，在应用程序启动时被实例化，并且在多线程之间可以共享。在Spring应用程序中，通常为每个Spring容器(或`ApplicationContext`)配置一个`ConversionService`实例。Spring获取`ConversionService`并且在需要类型转换时通过框架执行。可以在任何bean中注入和直接调用`ConversionService`。

*如果在Spring中没有注册`ConversionService`，原始的`PropertyEditor`系统会被使用*。

为了通过Spring注册一个默认的`ConversionService`，需要在bean定义中增加`id`为`conversionService`的属性：

```xml
<bean id="conversionService"
    class="org.springframework.context.support.ConversionServiceFactoryBean"/>
```

一个默认的`ConversionService`可以在strings,numbers,enums,collections,maps,和其他自定义类型之间进行转换。为了在自定义转换器中支持或覆盖默认转换器，可以设置`converter`属性。属性值可以任意实现`Converter`，`ConverterFactory`，`GenericConverter`。

```xml
<bean id="conversionService"
        class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
        <set>
            <bean class="example.MyCustomConverter"/>
        </set>
    </property>
</bean>
```

在Spring MVC 应用程序中，通常也使用`ConversionService`。参考Spring MVC章节中的`Conversion and Formatting`。

在某些情况下，可能希望在转换过程中应用格式设置。有关使用，参考`FormatterRegistry SPI`的详细信息。

### 3.4.6. 使用`ConversionService`编程

结合`ConversionService`实例编程，可以注入一个引用到任何其他的bean中。

```java
@Service
public class MyService {

    public MyService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void doIt() {
        this.conversionService.convert(...)
    }
}
```

对于大多数情况，可以使用指定`targetType`的`convert`方法，但是，对于更富在的类型，它不会生效，例如参数化元素的集合。例如，如果想要从`Integer`类型的`List`转换为`String`类型的`List`，需要提供源类型和目标类型的正式定义。

幸运的是，`TypeDescriptor`提供多种选择来是操作变得简单明了。

```java
DefaultConversionService cs = new DefaultConversionService();

List<Integer> input = ...
cs.convert(input,
    TypeDescriptor.forObject(input), // List<Integer> type descriptor
    TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
```

注意，`DefaultConversionService`自动注册适用于大多数环境的转换器。包括集合转换器，scalar转换器，基本的`Object`-to-`String`转换器。可以使用`DefaultConversionService`类上的静态`addDefaultConverters`方法在任何`ConverterRegistry`中注册相同的转换器。

值类型的转换器可重用于数组和集合，因此，假设标准集合处理适当，则无需创建特定的转换器即可将S的集合转换为T的集合。

## 3.5. Spring字段格式化

前面的部分讨论了通用目的类型转换系统。它提供了一个统一的`ConversionService`API和强类型的`Converter`SPI，用于实现从一种类型到另一种类型的转换逻辑。Spring容器使用这个系统来绑定bean属性值。此外，SpEL和`DataBinder`使用这个系统来绑定字段值。例如，当SpEL需要强制将`Short`转换为`Long`来完成一个`expression.setValue(Object bean,Object value)`时，`core.conver`系统将强制执行。

现在考虑一个典型用户环境的类型转换需求，例如一个web或桌面程序。在这些环境中，通常从`String`转换为支持支持客户端回发过程，以及返回`String`以支持视图呈现过程。此外，经常需要本地化`String`值。更通用的`core.convert` `Converter`SPI不能直接满足此类格式要求。为了慢去他们，Spring3以后引入了一个方便的`Formatter`SPI，它为客户端提供了一个简单健壮的`PropertyEditor`的另一种实现。

通常情况下，当需要实现通用目的的类型转换逻辑时，例如在`java.util.Date`和`Long`之间进行转换，可以使用`Converter`SPI。当需要结合客户端环境（例如一个web应用程序）并且需要解析和打印本地化字段值时，可以使用`Formatter`SPI。`ConversionService`为这两种SPIs提供了统一的类型转换API。

### 3.5.1. `Formatter`SPI

`Formatter`SPI用来实现非常简单的字段格式化逻辑和严格类型。下面展示了`Formatter`接口的定义：

```java
package org.springframework.format;

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```

`Formatter`从`Printer`和`Parse`构建快接口扩展。下面列出了这两个接口的定义：

```java
public interface Printer<T> {

    String print(T fieldValue, Locale locale);
}
```

```java
import java.text.ParseException;

public interface Parser<T> {

    T parse(String clientValue, Locale locale) throws ParseException;
}
```

为了创建自定义的`Formatter`，需要实现`Formatter`接口。参数类型`T`表示要格式化的对象类型，例如`java.util.Date`。为了在本地客户端显示，实现`print()`用来打印`T`的实例。实现`parse()`操作，以从客户端语言环境返回的格式化表示形式解析`T`的实例。如果解析失败，`Formatter`应该抛出一个`ParseException`或一个`IllegalArgumentException`。确保`Formatter`实现是线程安全的。

`format`子包提供一系列方便的`Formatter`实现。`number`包提供了`NumberStyleFormatter`，`CurrencyStyleFormatter`,`PercentStyleFormatter`来格式化`Number`对象。`datetime`包提供的一个`DateFormatter`来格式化`java.util.Date`对象。

下面是`DateFormatter`的例子：

```java
package org.springframework.format.datetime;

public final class DateFormatter implements Formatter<Date> {

    private String pattern;

    public DateFormatter(String pattern) {
        this.pattern = pattern;
    }

    public String print(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return getDateFormat(locale).format(date);
    }

    public Date parse(String formatted, Locale locale) throws ParseException {
        if (formatted.length() == 0) {
            return null;
        }
        return getDateFormat(locale).parse(formatted);
    }

    protected DateFormat getDateFormat(Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(this.pattern, locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}
```

Spring团队欢迎社区驱动的`Formatter`贡献。

### 3.5.2. 注解驱动的格式化

可以通过字段属性或注解配置字段格式化。为了绑定注解到`Formatter`上，需要实现`AnnotationFormatterFactory`。下面展示了`AnnotationFormatterFactory`接口的定义：

```java
package org.springframework.format;

public interface AnnotationFormatterFactory<A extends Annotation> {

    Set<Class<?>> getFieldTypes();

    Printer<?> getPrinter(A annotation, Class<?> fieldType);

    Parser<?> getParser(A annotation, Class<?> fieldType);
}
```

要创建一个实现：参数化A是需要链接格式化逻辑的注解类型字段-例如，`org.springframework.format.annotation.DateTimeFormat`。`getFieldTypes()`返回使用注解字段的类型。`getPrinter()`返回一个`Printer`来打印注解字段的值。`getParser()`返回一个`Parser`来解析注解字段的`clientValue`。

下面的例子是绑定了`@NumberFormat`注解的`AnnotationFormatterFactory`的实现，用来指定数字样式或模式：

```java
public final class NumberFormatAnnotationFormatterFactory
        implements AnnotationFormatterFactory<NumberFormat> {

    public Set<Class<?>> getFieldTypes() {
        return new HashSet<Class<?>>(asList(new Class<?>[] {
            Short.class, Integer.class, Long.class, Float.class,
            Double.class, BigDecimal.class, BigInteger.class }));
    }

    public Printer<Number> getPrinter(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    public Parser<Number> getParser(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    private Formatter<Number> configureFormatterFrom(NumberFormat annotation, Class<?> fieldType) {
        if (!annotation.pattern().isEmpty()) {
            return new NumberStyleFormatter(annotation.pattern());
        } else {
            Style style = annotation.style();
            if (style == Style.PERCENT) {
                return new PercentStyleFormatter();
            } else if (style == Style.CURRENCY) {
                return new CurrencyStyleFormatter();
            } else {
                return new NumberStyleFormatter();
            }
        }
    }
}
```

为了触发格式化，可以在字段上使用`@NumberFormat`：

```java
public class MyModel {

    @NumberFormat(style=Style.CURRENCY)
    private BigDecimal decimal;
}
```

**格式化注解API**

在`org.springframework.format.annotation`包中已经存在了一个方便的注解。可以使用`@NumberFormat`来格式化`Number`字段，例如`Double`和`Long`，`@DateTimeFormat`来格式化`java.util.Date`，`java.util.Calendar`，`Long`以及JSR-310`java.time`。

下面的例子使用`@DateTimeFormat`来格式化`java.util.Date`作为ISO日期（yyyy-MM-dd）：

```java
public class MyModel {

    @DateTimeFormat(iso=ISO.DATE)
    private Date date;
}
```

### 3.5.3. `FormatterRegistry`SPI

`FormatterRegistry`是一个注册了的格式化和转换器的SPI。`FormattingConversionService`实现了`FormatterRegistry`来匹配大多数环境。通过编程或声明来配置多种Spring bean。例如，通过使用`FormattingConversionServiceFactoryBean`。因为这个实现也实现了`ConversionService`，可以直接使用`DataBinder`和SpEL来直接配置。

下面列出了`FormatterRegistry`SPI：

```java
package org.springframework.format;

public interface FormatterRegistry extends ConverterRegistry {

    void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);

    void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);

    void addFormatterForFieldType(Formatter<?> formatter);

    void addFormatterForAnnotation(AnnotationFormatterFactory<?> factory);
}
```

像之前展示的，可以注册通过字段类型或注解来注册格式化器。

`FormatterRegistry` SPI让用户配置中心化的格式化规则，而不是在controllers之间重复这些配置。例如，可能想要强制所有日期字段被格式化成，或通过指定注解来格式化。通过共享`FormatterRegistry`，可以只定义一次规则，在任何需要格式化的时候应用他们。

### 3.5.4. `FormatterRegistrar` SPI

`FormatterRegistrar`是一个通过`FormatterRegistry`用来注册格式化器和专户亲戚的SPI。下面列出了他的接口定义：

```java
package org.springframework.format;

public interface FormatterRegistrar {

    void registerFormatters(FormatterRegistry registry);
}
```

当为给定的转换类别注册多种相关的转换器和格式化器时，`FormatterRegistrar`非常有用，例如日志格式化。在声明式注册不足的情况下，它也非常有用-例如，当一个格式化器需要在不同于其自身`<T>`的特定字段类型下进行索引时，或者在注册`Printer/Parser`对时。下一部分提供了转换器和格式化注册的更多信息。

### 3.5.5. 在Spring MVC中配置格式化

查看Spring MVC章节中的`Conversion and Formatting`。

## 3.6. 配置全局日期和时间格式化

默认情况下，使用`DateFormat.SHORT`样式从字符串转换未使用`@DateTimeFormat`注释的日期和时间字段。如果愿意，可以通过定义自己全局的格式来更改此设置。

为此，确保Spring没有注册默认的格式化器。相反，可以借助一下手法注册格式化器：

- `org.springframework.format.datetime.standard.DateTimeFormatterRegistra`

- `org.springframework.format.datetime.DateFormatterRegistrar`

例如，下面注册了一个全局`yyyyMMdd`：

```java
@Configuration
public class AppConfig {

    @Bean
    public FormattingConversionService conversionService() {

        // Use the DefaultFormattingConversionService but do not register defaults
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

        // Ensure @NumberFormat is still supported
        conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

        // Register JSR-310 date conversion with a specific global format
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        // Register date conversion with a specific global format
        DateFormatterRegistrar registrar = new DateFormatterRegistrar();
        registrar.setFormatter(new DateFormatter("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        return conversionService;
    }
}
```

如果更喜欢XML配置，可以使用`FormattingConversionServiceFactoryBean`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd>

    <bean id="conversionService" class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="registerDefaultFormatters" value="false" />
        <property name="formatters">
            <set>
                <bean class="org.springframework.format.number.NumberFormatAnnotationFormatterFactory" />
            </set>
        </property>
        <property name="formatterRegistrars">
            <set>
                <bean class="org.springframework.format.datetime.standard.DateTimeFormatterRegistrar">
                    <property name="dateFormatter">
                        <bean class="org.springframework.format.datetime.standard.DateTimeFormatterFactoryBean">
                            <property name="pattern" value="yyyyMMdd"/>
                        </bean>
                    </property>
                </bean>
            </set>
        </property>
    </bean>
</beans>
```

注意，在Web应用程序中配置日期和时间格式时，还有其他注意事项。可以参考`WebMVC Conversion and Formatting`或`WebFlux Conversion and Formatting`。

## 3.7. Java Bean 验证

Spring提供`Java Bean Validation`API的支持：

### 3.7.1. Bean 验证概述

Bean验证通过约束Java应用程序的声明和元数据，提供了一个通用的验证方式。为了使用它，通过声明验证约束来注解领域模型属性，然后在运行时强制执行。这里有内置的约束，也可以自定义自己的约束。

思考如下的例子，一个简单的`PersonForm`模型，带有两个属性：

```java
public class PersonForm {
    private String name;
    private int age;
}
```

然后像下面一样，声明约束：

```java
public class PersonForm {

    @NotNull
    @Size(max=64)
    private String name;

    @Min(0)
    private int age;
}
```

一个bean校验器验证基于声明约束的实例。请参考通用`Bean Validation`的API。对于特定验证，参考`Hibernate Validator`文档。为了学习如何为Spring bean提供验证设置，请继续阅读。

### 3.7.2. 配置一个Bean Validation Provider

Spring对Bean Validation API提供了完全的支持，包括将bean验证提供程序作为Spring Bean进行引导。在应用程序中，在需要的地方，注入一个`javax.validation.ValidatorFactory`或`javax.validation.Validator`。

可以使用`LocalValidatorFactoryBean`来配置一个作为Spring bean的默认校验器：

```java
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class AppConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
```

在上面的例子中，通过使用默认引导机制，来触发初始化bean验证。一个Bean Validation提供这，例如Hibernate校验器，希望在classpath中存在并且它会被自动检测。

**注入一个校验器**

`LocalValidatorFactoryBean`实现了`javax.validation.ValidatorFactory`和`javax.validation.Validator`，以及Spring的`org.springframework.validation.Validator`。可以在需要校验逻辑的地方注入接口的引用。

如果更喜欢直接使用Bean Validation API，可以注入一个`javax.validation.Validator`：

```java
import javax.validation.Validator;

@Service
public class MyService {

    @Autowired
    private Validator validator;
}
```

如果需要Spring Validation API，可以注入一个`org.springframework.validation.Validator`引用：

```java
import org.springframework.validation.Validator;

@Service
public class MyService {

    @Autowired
    private Validator validator;
}
```

**配置自定义约束**

每个校验器约束包含两部分：

- `@Constraint`注解用来声明约束和它的配置属性

- 一个`javax.validation.ConstraintValidator`接口的实现，来约束行为

要将声明与实现相关联，每个`@Constraint`注解引用一个相应的`ConstraintValidator`实现类。在运行时，在领域模型中遇到约束注解时，`ConstraintValidatorFactory`实例化被引用的实现。

默认情况下，`LocalValidatorFactoryBean`配置一个`SpringConstraintValidatorFactory`，使用Spring来创建`ConstraintValiator`实例。让自定义的`ConstraintValidators`像其他任何Spring bean一样从依赖注入中受益。

下面的例子展示了一个自定义`@Constraint`声明，后面跟着一个关联的`ConstraintValidator`实现，改实现使用Spring进行依赖注入：

```java
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=MyConstraintValidator.class)
public @interface MyConstraint {
}
```

```java
import javax.validation.ConstraintValidator;

public class MyConstraintValidator implements ConstraintValidator {

    @Autowired;
    private Foo aDependency;

    // ...
}
```

**Spring驱动方法验证**

通过Bean Validation 1.1,可以集成方法验证特性，在Spring上下文中，通过定义`MethodValidationPostProcessor`：

```java
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class AppConfig {

    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```

为了有资格进行Spring驱动的方法验证，所有目标类都必须使用Spring的`@Validated`注解，该注解也可以选择声明要使用的验证组。参考`MethodValidationPostProcessor`，来设置Hibernate Validator和Bean Validation 1.1 Provider的细节。

*方法验证依赖在目标类上的AOP 代理，即接口方法上的JDK动态代理或CGLIB代理。代理的使用存在某些限制，参考`Understanding AOP Proxies`。此外，请记住在代理类上始终使用方法和访问器(getter)，字段的直接访问将不起作用。*

**附加的配置选项**

默认`LocalValidatorFactoryBean`满足大多数场景。对于Bean Validation结构体，这里有很多可选的配置项，从消息插入到遍历解决。参考`LacalValidatorFactoryBean`文档获取更多信息。

### 3.7.3. 配置一个`DataBinder`

Spring3以后，可以通过`Validator`配置一个`DataBinder`。一旦配置了，可以通过调用`binder.validate()`来调用`Validator`。任何验证`Errors`会自动被加入到`BindingResult`。

下面的例子展示了如何通过编程，使用`DataBinder`在绑定目标对象后，来调用验证逻辑。

```java
Foo target = new Foo();
DataBinder binder = new DataBinder(target);
binder.setValidator(new FooValidator());

// bind to the target object
binder.bind(propertyValues);

// validate the target object
binder.validate();

// get BindingResult that includes any validation errors
BindingResult results = binder.getBindingResult();
```

可以通过`dataBinder.addValidators`和`dataBinder.replaceValidators`为一个`DataBinder`配置多个`Validator`实例。当将全局配置的bean验证与`DataBinder`实例上本地配置的`Spring Validator`结合使用时，这很有用。请参阅`Spring MVC Validation Configuration`。

### 3.7.4. Spring MVC 3 验证

请参阅在Spring MVC 章节中的`Validation`。

# 4. Spring 表达式语言（略）
