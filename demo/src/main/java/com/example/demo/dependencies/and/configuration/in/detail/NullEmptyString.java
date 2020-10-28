package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NullEmptyString {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private static final Logger log = LoggerFactory.getLogger(NullEmptyString.class);

	public static void main(String[] args) {
		String path = "classpath:nullEmptyStringValue.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		NullEmptyString nullBean = context.getBean("nullString", NullEmptyString.class);
		NullEmptyString emptyBean = context.getBean("emptyString", NullEmptyString.class);
		log.info("nullBean:{},emptyBean:{}", nullBean.getName(), emptyBean.getName());
		context.close();
	}
}
