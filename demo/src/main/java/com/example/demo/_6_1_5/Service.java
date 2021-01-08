package com.example.demo._6_1_5;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Service {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final String name;

	public Service(String name) {
		this.name = name;
	}

	public String getName() {
		log.info("get:{}", this.name);
		return this.name;
	}

	public int getAge() {
		return new Random().nextInt(10);
	}
}
