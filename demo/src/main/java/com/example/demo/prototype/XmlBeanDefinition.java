package com.example.demo.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.example.demo.gson.JSON;

/**
 * 多种解决办法 https://www.baeldung.com/spring-inject-prototype-bean-into-singleton
 */
public class XmlBeanDefinition {

	private static final Logger log = LoggerFactory.getLogger(XmlBeanDefinition.class);

	public static void main(String[] args) {
		String path = "classpath:prototype/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		for (int i = 0; i < 2; i++) {
			Thread thread = new Thread(() -> {
				/**
				 * 第一种方法，这种方式无效
				 */
//				SingletonBean singletonBean = context.getBean("singletonBean", SingletonBean.class);
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						singletonBean.getPrototypeBean().hashCode(), singletonBean);

				/**
				 * 第二种方法
				 */
				// Every time the getPrototypeBean() method is called, a new instance of
				// PrototypeBean will be returned from the ApplicationContext.
				//
				// However, this approach has serious disadvantages. It contradicts the
				// principle of inversion of control, as we request the dependencies from the
				// container directly.
				//
				// Also, we fetch the prototype bean from the applicationContext within the
				// SingletonAppcontextBean class. This means coupling the code to the Spring
				// Framework.
//				SingletonBean1 singletonBean = context.getBean("singletonBean1", SingletonBean1.class);
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						singletonBean.getPrototypeBean().hashCode(), singletonBean);
				/**
				 * Method Injection -1
				 */
//				SingletonBean2 singletonBean = context.getBean("singletonBean2", SingletonBean2.class);
//				PrototypeBean prototypeBean = singletonBean.getPrototypeBean();
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));

				/**
				 * Method Injection -2
				 */
//				SingletonBean4 singletonBean = context.getBean("singletonBean4", SingletonBean4.class);
//				PrototypeBean prototypeBean = singletonBean.getPrototypeBean();
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));

				/**
				 * JSR-330 provier接口
				 */
//				SingletonBean3 singletonBean = context.getBean("singletonBean3", SingletonBean3.class);
//				PrototypeBean prototypeBean = singletonBean.getPrototypeBean();
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));

				/**
				 * Scope proxy CGLIB代理 By default, Spring holds a reference to the real object
				 * to perform the injection. Here, we create a proxy object to wire the real
				 * object with the dependent one. By default, Spring holds a reference to the
				 * real object to perform the injection. Here, we create a proxy object to wire
				 * the real object with the dependent one.
				 */
//				SingletonBean5 singletonBean = context.getBean("singletonBean5", SingletonBean5.class);
//				PrototypeBeanScopedProxy prototypeBeanScopedProxy = singletonBean.getPrototypeBeanScopedProxy();
//				log.info("{}", prototypeBeanScopedProxy.hashCode());
//				PrototypeBean prototypeBean = prototypeBeanScopedProxy.getPrototypeBean();
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));

				/**
				 * ObjectFactory Interface
				 * 
				 * Let's have a look at getPrototypeInstance() method; getObject() returns a
				 * brand new instance of PrototypeBean for each request. Here, we have more
				 * control over initialization of the prototype.
				 *
				 * Also, the ObjectFactory is a part of the framework; this means avoiding
				 * additional setup in order to use this option.
				 * 
				 */
//				SingletonBean6 singletonBean = context.getBean("singletonBean6", SingletonBean6.class);
//				PrototypeBean prototypeBean = singletonBean.getPrototypeBeanObjectFactory().getObject();
//				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
//						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));

				/**
				 * Create a Bean at Runtime Using java.util.Function
				 */
				SingletonBean7 singletonBean = context.getBean("singletonBean7", SingletonBean7.class);
				PrototypeBean prototypeBean = singletonBean.getBeanFactory().apply(null);
				log.info("singleton->hashcode:{},prototype->hashcode:{},{}", singletonBean.hashCode(),
						prototypeBean.hashCode(), JSON.toJSONString(prototypeBean));
			});
			thread.start();
		}
		context.close();
	}
}
