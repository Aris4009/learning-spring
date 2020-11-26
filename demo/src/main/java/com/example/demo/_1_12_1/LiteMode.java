package com.example.demo._1_12_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * lite mode
 */
@Component
public class LiteMode {

	private static Logger log = LoggerFactory.getLogger(LiteMode.class);

	@Bean(name = "user3")
	public User user() {
		User user = new User();
		log.info("{}", user.hashCode());
		return user;
	}

	@Bean(name = "user4")
	public User user2() {
		User user = user();
		log.info("{}", user.hashCode());
		return user;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LiteMode.class);
		User user1 = context.getBean("user3", User.class);
		User user2 = context.getBean("user4", User.class);
		context.close();
	}

}
