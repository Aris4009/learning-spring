package com.example.demo.arbitrary.method.replacement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 原始方法
 */
public class OriginObj {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public String old(String arg) {
		log.info("I am old method,the arg is {}", arg);
		return arg;
	}
}
