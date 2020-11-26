package com.example.demo._1_12_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.hello.Hello;

@Configuration
public class SimpleConstruction {

	@Bean
	public Hello hello1() {
		Hello hello = new Hello();
		return hello;
	}

	private static Logger log = LoggerFactory.getLogger(SimpleConstruction.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SimpleConstruction.class);
		Hello hello = context.getBean(Hello.class);
		hello.setName("name");
		hello.setAge(22);
		log.info("{}", hello);
		context.close();
	}
}
