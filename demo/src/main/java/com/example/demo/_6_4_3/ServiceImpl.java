package com.example.demo._6_4_3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceImpl implements IService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private String name;

	public ServiceImpl(String name) {
		this.name = name;
	}

	@Override
	public String test(String name) {
		log.info("{}", name);
		this.name = name;
		return name;
	}

	public String getName() {
		return name;
	}
}
