package com.example.demo.using.depends.on;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassB {

	public static Logger log = LoggerFactory.getLogger(ClassB.class);

	public static ClassB init() {
		log.info("init class B");
		return new ClassB();
	}
}
