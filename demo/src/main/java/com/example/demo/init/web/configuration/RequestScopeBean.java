package com.example.demo.init.web.configuration;

public class RequestScopeBean {

	private String name;

	@Override
	public String toString() {
		return "RequestScopeBean{" + "name='" + name + '\'' + '}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
