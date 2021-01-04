package com.example.demo._5_4_7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Service {

	private String name;

	private int age;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Service(@Value("张三") String name, @Value("100") int age) {
		this.name = name;
		this.age = age;
	}

	public String buy(String name) throws Exception {
		if (name == null) {
			throw new Exception("name is null");
		}
		log.info("buy:{}", name);
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
