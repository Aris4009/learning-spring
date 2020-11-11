package com.example.demo.custom.nature.bean.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 构造函数注入ApplicationContext，在bean定义中使用autowire装配
 */
public class RefByConstruct {

	private ApplicationContext context;

	private static Logger log = LoggerFactory.getLogger(RefByConstruct.class);

	public RefByConstruct(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public String toString() {
		return "RefByConstruct{" + "context=" + context + '}';
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/aware/aware.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		RefByConstruct refByConstruct = context.getBean("refByConstruct", RefByConstruct.class);
		log.info(context.toString());
		log.info("{}", refByConstruct);
		context.close();
	}
}
