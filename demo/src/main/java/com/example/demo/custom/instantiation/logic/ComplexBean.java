package com.example.demo.custom.instantiation.logic;

import java.util.List;

public class ComplexBean {

	private String name;

	private List<Integer> list;

	public ComplexBean(String name, List<Integer> list) {
		this.name = name;
		this.list = list;
	}

	@Override
	public String toString() {
		return "ComplexBean{" + "name='" + name + '\'' + ", list=" + list + '}';
	}
}
