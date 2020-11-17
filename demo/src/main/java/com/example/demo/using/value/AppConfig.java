package com.example.demo.using.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 找不到注解，默认会把value的占位符复制给变量。如果需要严格的属性值对应，那么需要使用PropertySourcesPlaceholderConfigurer，并且在配置里，这个bean的方法必须是静态的。
 */
@Configuration
@PropertySource("classpath:using/value/application.properties")
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.using.value");
		MovieRecommender bean = context.getBean("movieRecommender", MovieRecommender.class);
		log.info("{}", bean);
		context.close();
	}

	/*
	 * 该方法必须是静态的
	 */
//	@Bean
//	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//		return new PropertySourcesPlaceholderConfigurer();
//	}
}
