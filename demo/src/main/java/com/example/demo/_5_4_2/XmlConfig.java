package com.example.demo._5_4_2;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XmlConfig {

	public static void main(String[] args) {
		String path = "_5_4_1/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
