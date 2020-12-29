package com.example.demo._5_4_2._1;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Service
public class Service {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String name;

	private final int age;

	public Service(@Value("张三") String name, @Value("20") int age) {
		this.name = name;
		this.age = age;
	}

	public void sayHello() {
		log.info("I am raw1,name:{},age:{}", this.name, this.age);
	}

	protected int sayHello(int age) {
		log.info("I am raw2,name:{},new age:{}", this.name, age);
		return age;
	}

	public Map<String, Integer> sayHello(String name, int age, boolean flag) throws Exception {
		if (!flag) {
			throw new Exception("raw3发生了异常错误");
		}
		log.info("I am raw3,new name:{},new age:{},flag:{}", name, age, flag);
		Map<String, Integer> map = new HashMap<>();
		map.put(name, age);
		return map;
	}

	protected String sayHello(String name) throws Exception {
		log.info("I am raw4,new name:{},age:{}", name, this.age);
		throw new Exception("raw4发生了异常错误");
	}

	public Map<String, Integer> sayHello(String name, int age) {
		log.info("I am raw5,new name:{},new age:{}", name, age);
		Map<String, Integer> map = new HashMap<>();
		map.put(name, age);
		return map;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	@Override
	public String toString() {
		return "Service{" + "name='" + name + '\'' + ", age=" + age + '}';
	}
}
