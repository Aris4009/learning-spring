package com.example.demo._1_15_1;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MessageSourceTest {

	private static Logger log = LoggerFactory.getLogger(MessageSourceTest.class);

	public static void main(String[] args) {
		String path = "classpath:_1_15_1/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		MessageSource messageSource = context;
		String message = messageSource.getMessage("message", null, "Default", Locale.ENGLISH);
		log.info("{}", message);

		Example example = context.getBean("example", Example.class);
		example.execute();
		message = messageSource.getMessage("argument.required", new Object[] { "userDao" }, "Required", Locale.UK);
		log.info("{}", message);

		ExampleMessageSourceAware exampleMessageSourceAware = context.getBean("exampleMsa",
				ExampleMessageSourceAware.class);
		exampleMessageSourceAware.execute();
	}
}
