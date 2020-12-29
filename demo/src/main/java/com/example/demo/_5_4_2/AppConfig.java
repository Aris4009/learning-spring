package com.example.demo._5_4_2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import com.example.demo.gson.JSON;

@Component
@Configurable
@EnableAspectJAutoProxy
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException {
		int code = 1;
		AnnotationConfigApplicationContext context = init(code);
		if (context == null) {
			return;
		}
//		printAbstractBeanFactoryMethod(context, "transformedBeanName", new Object[] { "&&haha" });
//		printBeanDefinitions(context);
//		printDefaultEnvironmentBeans(context);
//		printIgnoreDependencyInterface(context);
//		log.info("{}", context.getClassLoader());
//		log.info("{}", context.getDefaultListableBeanFactory());
//		log.info(context.getDisplayName());
//		log.info("{}", context.isActive());
//		log.info("{}", new Date(context.getStartupDate()).toString());
//		log.info("{}", Arrays.toString(context.getBeanDefinitionNames()));
		Service service = context.getBean("service", Service.class);
		log.info("{}", service.getClass().getName());
		service.sayNothing();
		context.close();
		log.info("{}", context.isActive());
	}

	public static AnnotationConfigApplicationContext init(int code) {
		if (code == 1) {
			// 这种方式可以生效
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
			context.register(AppConfig.class, Service.class, ServiceAspect.class);
			context.refresh();
			return context;
		} else if (code == 2) {
			// 这种方式不生效？不知为何？
			// 原因为没有加载AppConfig.class类
			// 通过打印beanDefinitionNames，没有将appConfig注册到beanDefinition。因为没有加@Component
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
					"com/example/demo/_5_4_2");
			return context;
		} else if (code == 3) {
			// 这种方式可以生效，需要在AppConfig上加入包扫描注解@ComponentScan
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
			return context;
		} else if (code == 4) {
			// 这种方式可以生效，不需要加包扫描注解@ComponentScan,这种方式与第一种方式一样，只不过第一种方式是通过手工调用的
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class,
					Service.class, ServiceAspect.class);
			return context;
		} else {
			return null;
		}
	}

	/**
	 * 打印AbstractAutowireCapableBeanFactory中的忽略依赖接口属性值
	 * 
	 * @param context
	 * @throws IllegalAccessException
	 */
	public static void printIgnoreDependencyInterface(ConfigurableApplicationContext context)
			throws IllegalAccessException {
		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
		Class<DefaultListableBeanFactory> clazz = (Class<DefaultListableBeanFactory>) defaultListableBeanFactory
				.getClass();
		Class<AbstractAutowireCapableBeanFactory> supClazz = (Class<AbstractAutowireCapableBeanFactory>) clazz
				.getSuperclass();

		Field[] fields = supClazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getName().equals("ignoredDependencyInterfaces")) {
				Set<Class<?>> set = (Set<Class<?>>) field.get(defaultListableBeanFactory);
				log.info("{}", Arrays.toString(set.toArray(new Object[0])));
			}
		}
	}

	/**
	 * 打印注册的默认环境bean
	 * 
	 * @param context
	 */
	public static void printDefaultEnvironmentBeans(ApplicationContext context) {
		log.info("{}", JSON.toJSONString(context.getBean("systemEnvironment")));
	}

	/**
	 * 打印beanDefinitions
	 * 
	 * @param context
	 */
	public static void printBeanDefinitions(AnnotationConfigApplicationContext context) {
		String[] beanDefinitionsNames = context.getBeanDefinitionNames();
		for (String beanDefinitionsName : beanDefinitionsNames) {
			log.info("{}-{}", beanDefinitionsName, context.getBeanDefinition(beanDefinitionsName).getScope());
		}

		// 方式1，打印输出：
		/**
		 * 2020-12-22 18:15:23,502 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalConfigurationAnnotationProcessor
		 * 2020-12-22 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalAutowiredAnnotationProcessor
		 * 2020-12-22 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalCommonAnnotationProcessor
		 * 2020-12-22 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.event.internalEventListenerProcessor 2020-12-22
		 * 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.event.internalEventListenerFactory 2020-12-22
		 * 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115): appConfig 2020-12-22 18:15:23,508 INFO [main]
		 * com.example.demo._5_4_2.AppConfig printBeanDefinitions(115): service
		 * 2020-12-22 18:15:23,508 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115): serviceAspect 2020-12-22 18:15:23,509 INFO [main]
		 * com.example.demo._5_4_2.AppConfig printBeanDefinitions(115):
		 * org.springframework.aop.config.internalAutoProxyCreator 2020-12-22
		 * 18:15:23,509 INFO [main] com.example.demo._5_4_2.AppConfig main(41):
		 * com.example.demo._5_4_2.Service$$EnhancerBySpringCGLIB$$d8a9c560 2020-12-22
		 * 18:15:23,513 INFO [main] com.example.demo._5_4_2.ServiceAspect test(23): I'm
		 * saying hello 2020-12-22 18:15:23,533 INFO [main]
		 * com.example.demo._5_4_2.Service sayNothing(13): nothing 2020-12-22
		 * 18:15:23,539 INFO [main] com.example.demo._5_4_2.AppConfig main(44): false
		 */

		// 方式2，打印输出
		/**
		 * 2020-12-22 18:15:43,180 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalConfigurationAnnotationProcessor
		 * 2020-12-22 18:15:43,183 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalAutowiredAnnotationProcessor
		 * 2020-12-22 18:15:43,183 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.annotation.internalCommonAnnotationProcessor
		 * 2020-12-22 18:15:43,183 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.event.internalEventListenerProcessor 2020-12-22
		 * 18:15:43,183 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115):
		 * org.springframework.context.event.internalEventListenerFactory 2020-12-22
		 * 18:15:43,183 INFO [main] com.example.demo._5_4_2.AppConfig
		 * printBeanDefinitions(115): service 2020-12-22 18:15:43,184 INFO [main]
		 * com.example.demo._5_4_2.AppConfig printBeanDefinitions(115): serviceAspect
		 * 2020-12-22 18:15:43,184 INFO [main] com.example.demo._5_4_2.AppConfig
		 * main(41): com.example.demo._5_4_2.Service 2020-12-22 18:15:43,184 INFO [main]
		 * com.example.demo._5_4_2.Service sayNothing(13): nothing 2020-12-22
		 * 18:15:43,185 INFO [main] com.example.demo._5_4_2.AppConfig main(44): false
		 */
	}

	/**
	 * 打印一些AbstractBeanFactoryMethod的方法
	 * 
	 * @param context 应用程序航下文
	 * @param method  需要打印的方法
	 * @param arg     反射方法调用参数
	 */
	public static void printAbstractBeanFactoryMethod(ConfigurableApplicationContext context, String method,
			Object... arg) throws InvocationTargetException, IllegalAccessException {
		if (context == null || method == null) {
			return;
		}
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
		Class<AbstractBeanFactory> clazz = (Class<AbstractBeanFactory>) beanFactory.getClass().getSuperclass()
				.getSuperclass();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			m.setAccessible(true);
			if (m.getName().equals(method)) {
				log.info("{}", m.getName());
				Object obj = m.invoke(beanFactory, arg);
				log.info("{}", JSON.toJSONString(obj));
			}
		}
	}
}
