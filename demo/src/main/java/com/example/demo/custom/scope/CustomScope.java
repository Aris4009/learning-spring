package com.example.demo.custom.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.SimpleThreadScope;

/**
 * 暂时未解决自定义Scope
 */
public class CustomScope {

	private static Logger log = LoggerFactory.getLogger(CustomScope.class);

	public static void main(String[] args) throws InterruptedException {
		method1();
//		method2();
	}

	private static void method1() {
		String path = "classpath:custom/scope/customScope.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		Scope scope = new SimpleThreadScope();
		context.getBeanFactory().registerScope("custom", scope);
		for (int i = 0; i < 2; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Thing thing = context.getBean("thing", Thing.class);
					log.info("{}-{}-thing.hashCode:{},custom.hashCode:{}", Thread.currentThread().getName(),
							scope.getConversationId(), thing.hashCode(), thing.getCustomScope().hashCode());
					CustomScope customScope = context.getBean("bean", CustomScope.class);
					log.info("{}", customScope.hashCode());
				}
			});
			thread.setName(String.valueOf(i));
//			thread.join();
			thread.start();
		}
//		context.close();
	}

	private static void method2() {
		String path = "classpath:custom/scope/customerScope2.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		for (int i = 0; i < 2; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Thing thing = context.getBean("thing1", Thing.class);
					log.info("{}-thing.hashCode:{},custom.hashCode:{}", Thread.currentThread().getName(),
							thing.hashCode(), thing.getCustomScope().hashCode());
				}
			});
			thread.setName(String.valueOf(i));
//			thread.join();
			thread.start();
		}
		context.close();
	}
}
