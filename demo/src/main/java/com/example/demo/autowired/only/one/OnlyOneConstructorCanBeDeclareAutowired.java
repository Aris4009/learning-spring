package com.example.demo.autowired.only.one;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 只有一个构造函数可以被声明为@Autowired
 */
@Component
public class OnlyOneConstructorCanBeDeclareAutowired {

	private String name;

	private int age;

	@Autowired
	public OnlyOneConstructorCanBeDeclareAutowired(String name) {
		this.name = name;
	}

//	@Autowired
	public OnlyOneConstructorCanBeDeclareAutowired(String name, int age) {
		this.name = name;
		this.age = age;
	}
}
