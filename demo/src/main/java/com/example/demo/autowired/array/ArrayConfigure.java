package com.example.demo.autowired.array;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArrayConfigure {

	@Bean
	public List<String> nameList() {
		List<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		return list;
	}

	@Bean
	public Set<Integer> integerSet() {
		Set<Integer> set = new HashSet<>();
		set.add(1);
		set.add(2);
		return set;
	}

	@Bean
	public String[] strArr() {
		String[] arr = new String[2];
		arr[0] = "1";
		return arr;
	}

	private static Logger log = LoggerFactory.getLogger(ArrayConfigure.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.autowired.array");
		AutowiredArray bean = context.getBean("autowiredArray", AutowiredArray.class);
		log.info("{}", bean);
		context.close();
	}
}
