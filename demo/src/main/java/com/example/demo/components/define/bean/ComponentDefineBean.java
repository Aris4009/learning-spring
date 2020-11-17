package com.example.demo.components.define.bean;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @Component也可以像@Configurer一样，为容器提供bean
 */
@Component
public class ComponentDefineBean {

	@Bean
	public List<String> list() {
		List<String> list = new ArrayList<>();
		list.add("1");
		return list;
	}

	private static Logger log = LoggerFactory.getLogger(ComponentDefineBean.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.components.define.bean");
		log.info("{}", context.getBean("list", List.class).hashCode());
		context.close();
	}
}
