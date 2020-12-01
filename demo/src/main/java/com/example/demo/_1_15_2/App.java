package com.example.demo._1_15_2;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	public static void main(String[] args) {
		String path = "classpath:_1_15_2/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		EmailService service = context.getBean("emailService", EmailService.class);
		List<String> list = new ArrayList<>();
		list.add("错误地址1");
		list.add("正确地址");
		list.add("错误地址2");
		list.forEach(address -> service.sendEmail(address, address));
		context.close();
	}
}
