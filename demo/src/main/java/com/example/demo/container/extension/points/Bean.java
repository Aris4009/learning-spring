package com.example.demo.container.extension.points;

public class Bean {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Bean{" + "name='" + name + '\'' + '}';
	}
}
