package com.example.demo.custom.nature.bean.initialization.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 依赖XML注解
 */
public class InitMethodBean {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void init() {
		log.info("{}", this.hashCode());
		log.info("bean初始化后，执行本方法");
		this.name = "test";
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/initialization/callback/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
