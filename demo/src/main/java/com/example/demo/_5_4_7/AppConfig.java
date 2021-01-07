package com.example.demo._5_4_7;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("com.example.demo._5_4_7");
		context.refresh();

		Service service = context.getBean("jdkRegexpMethodPointService", Service.class);
		service.buy("book1");
//		service.buy(null);
	}
}
