package com.example.demo.autowired.custom.qualifier;

public class MovieCatalog {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "MovieCatalog{" + "name='" + name + '\'' + '}';
	}
}
