package com.example.demo.bean.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Bean 范围
 */
public class BeanScope {

	private static Logger log = LoggerFactory.getLogger(BeanScope.class);

	@Override
	public String toString() {
		return String.valueOf(this.hashCode());
	}

	public static void main(String[] args) {
		String path = "classpath:beanScope.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		BeanScope singletonScope1 = context.getBean("singleton", BeanScope.class);
		log.info("bean 1 hashcode:{}", singletonScope1);
		BeanScope singletonScope2 = context.getBean("singleton", BeanScope.class);
		log.info("bean 2 hashcode:{}", singletonScope2);
		for (int i = 0; i < 10; i++) {
			BeanScope prototype = context.getBean("prototype", BeanScope.class);
			log.info("prototype {} hashcode:{}", i, prototype);
		}
		context.close();
	}
}
