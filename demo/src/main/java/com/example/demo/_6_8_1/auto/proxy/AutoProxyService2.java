package com.example.demo._6_8_1.auto.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("autoProxyService2")
public class AutoProxyService2 {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public String test(String name) {
		log.info("autoProxyService2:{}", name);
		return name;
	}

}