package com.example.demo._6_1_4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JdkRegexpMethodPointService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String name;

	public JdkRegexpMethodPointService(@Value("张三") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		log.info("setName");
		this.name = name;
	}

	public void absquatulate() {
		log.info("absquatulate");
	}
}
