package com.example.demo._1_12_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * full模式 vs lite模式
 * 
 * https://cloud.tencent.com/developer/article/1555407
 */
@Configuration
public class FullConfiguration {

	private static Logger log = LoggerFactory.getLogger(FullConfiguration.class);

	@Bean(name = "user1")
	public User user() {
		User user = new User();
		log.info("{}", user.hashCode());
		return user;
	}

	@Bean(name = "user2")
	public User user2() {
		User user = user();
		log.info("{}", user.hashCode());
		return user;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(FullConfiguration.class);
		User user1 = context.getBean("user1", User.class);
		User user2 = context.getBean("user2", User.class);
		context.close();
	}
}
