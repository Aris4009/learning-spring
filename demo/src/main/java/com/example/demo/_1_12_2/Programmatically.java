package com.example.demo._1_12_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.example.demo.hello.Hello;

@Configuration
public class Programmatically {

	@Bean
	@Description("i am description")
	public Hello hello2() {
		Hello hello = new Hello();
		hello.setName("hello");
		hello.setAge(33);
		return hello;
	}

	private static Logger log = LoggerFactory.getLogger(Programmatically.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(Programmatically.class);
		annotationConfigApplicationContext.refresh();
		Hello hello = annotationConfigApplicationContext.getBean(Hello.class);
		log.info("{}", hello);
		annotationConfigApplicationContext.close();
	}
}
