package com.example.demo._5_4_5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("com.example.demo._5_4_5");
		context.refresh();

		Service service = context.getBean(Service.class);
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(service::test);
			thread.start();
			thread.join();
		}
		log.info("{},{}", service.getClass().getName(), service.hashCode());

		UsageTracked usageTracked = (UsageTracked) context.getBean("jdkRegexpMethodPointService");
		log.info("{},{}", usageTracked.getClass().getName(), usageTracked.hashCode());
	}
}
