package com.example.demo._1_15_2.annotation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Bean
	public BlockedListEventPublisher publisher() {
		List<String> list = new ArrayList<>();
		list.add("a1");
		list.add("b1");
		return new BlockedListEventPublisher(this.applicationEventPublisher, list);
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		BlockedListEventPublisher publisher = context.getBean("publisher", BlockedListEventPublisher.class);
		publisher.publish("b1");
		publisher.publish("a2");
		context.close();
	}
}
