package com.example.demo.custom.nature.bean.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 构造函数注入ApplicationContext，在bean定义中使用autowire装配，使用byType进行装配
 */
public class RefBySetter {

	private ApplicationContext context;

	private static Logger log = LoggerFactory.getLogger(RefBySetter.class);

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public String toString() {
		return "RefByConstruct{" + "context=" + context + '}';
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/aware/aware.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		RefBySetter refBySetter = context.getBean("refBySetter", RefBySetter.class);
		log.info(context.toString());
		log.info("{}", refBySetter);
		context.close();
	}
}
