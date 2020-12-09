package com.example.demo._2_6;

import org.springframework.core.io.Resource;

public class MyBean {

	private final Resource resource;

	public MyBean(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}
}
