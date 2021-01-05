package com.example.demo._5_10_1.preconstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

/**
 * 将bean注入到普通对象中，使用了AspectJ的织入器 需要在maven中添加相关依赖 spring-aspects
 * 
 * https://www.baeldung.com/spring-inject-bean-into-unmanaged-objects
 */
@ComponentScan
@EnableSpringConfigured
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		PersonObject personObject = new PersonObject("test");
		log.info("{}", personObject.getId());
		log.info("{}", personObject.getId());

		PersonObject personObject1 = new PersonObject("test");
		log.info("{}", personObject1.getId());
		log.info("{}", personObject1.getId());
	}
}
