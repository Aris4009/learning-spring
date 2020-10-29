package com.example.demo.using.depends.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassA {

	public static Logger log = LoggerFactory.getLogger(ClassA.class);

	public static ClassA init() {
		log.info("init class A");
		return new ClassA();
	}
}
