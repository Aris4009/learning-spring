package com.example.demo.dependencies.and.configuration.in.detail;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StronglyTypedCollection {

	private Map<String, Float> map;

	public Map<String, Float> getMap() {
		return map;
	}

	public void setMap(Map<String, Float> map) {
		this.map = map;
	}

	private static final Logger log = LoggerFactory.getLogger(StronglyTypedCollection.class);

	public static void main(String[] args) {
		String path = "classpath:stronglyTypedCollection.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		StronglyTypedCollection bean = context.getBean("bean", StronglyTypedCollection.class);
		log.info("child:{}", bean.getMap());
		context.close();
	}
}
