package com.example.demo.annotation.xml.annotation.without.annotation;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class AnnotationDependencyBean {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AnnotationDepencyBean{" + "name='" + name + '\'' + '}';
	}

	@PostConstruct
	public void init() {
		this.name = "aaa";
	}
}
