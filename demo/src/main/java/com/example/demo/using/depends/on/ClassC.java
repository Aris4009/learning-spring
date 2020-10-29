package com.example.demo.using.depends.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ClassC 不直接依赖ClassA、ClassB 但是ClassC的初始化，需要让ClassA、ClassB先初始化
 */
public class ClassC {

	public static Logger log = LoggerFactory.getLogger(ClassC.class);

	public static ClassC init() {
		log.info("init class C");
		return new ClassC();
	}

	public static void main(String[] args) {
		String path = "classpath:depends-on.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
