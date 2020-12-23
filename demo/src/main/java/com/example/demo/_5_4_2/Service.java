package com.example.demo._5_4_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Service {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public Service() {
		log.info("初始化");
	}

	public void sayNothing() {
		log.info("{}", "nothing");
	}
}
