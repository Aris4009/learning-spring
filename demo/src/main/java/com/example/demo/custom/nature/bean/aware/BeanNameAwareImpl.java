package com.example.demo.custom.nature.bean.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanNameAwareImpl implements BeanNameAware {

	private String beanName;

	private static Logger log = LoggerFactory.getLogger(BeanNameAwareImpl.class);

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	public String getBeanName() {
		return beanName;
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/aware/aware.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		BeanNameAwareImpl bean = context.getBean("fff", BeanNameAwareImpl.class);
		log.info("{}", bean.getBeanName());
		context.close();
	}
}
