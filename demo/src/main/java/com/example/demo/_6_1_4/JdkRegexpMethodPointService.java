package com.example.demo._6_1_4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdkRegexpMethodPointService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String name;

	public JdkRegexpMethodPointService(String name) {
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
