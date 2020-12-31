package com.example.demo._5_4_5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.example.demo.gson.JSON;

@org.springframework.stereotype.Service
public class Service {

	private final String name;

	private final int age;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public Service(@Value("张三") String name, @Value("100") int age) {
		this.name = name;
		this.age = age;
	}

	public void test() {
		log.info(JSON.toJSONString(this));
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}
}
