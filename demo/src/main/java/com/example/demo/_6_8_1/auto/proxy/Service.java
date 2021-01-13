package com.example.demo._6_8_1.auto.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
public class Service {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public String test(String name) {
		log.info("autoProxyService1:{}", name);
		return name;
	}
}
