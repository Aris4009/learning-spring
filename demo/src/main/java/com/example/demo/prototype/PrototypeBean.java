package com.example.demo.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 要注入的原型bean
 */
public class PrototypeBean {

	private String name;

	private int age;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PrototypeBean(String name, int age) {
		log.info("The prototype init");
		this.name = name;
		this.age = age;
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

	@Override
	public String toString() {
		return "PrototypeBean{" + "name='" + name + '\'' + ", age=" + age + '}';
	}
}
