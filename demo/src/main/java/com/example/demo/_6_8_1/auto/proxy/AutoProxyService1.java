package com.example.demo._6_8_1.auto.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("autoProxyService1")
public class AutoProxyService1 {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public String test(String name) {
		log.info("autoProxyService1:{}", name);
		return name;
	}

}
