package com.example.demo._5_10_1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
//The @Configurable annotation, in this case, marks the PersonObject class as being eligible for Spring-driven configuration.
public class PersonObject {

	private int id;

	private String name;

	@Autowired
	private IdService idService;

	public PersonObject(String name) {
		this.name = name;
	}

	public int getId() {
		this.id = idService.generateId();
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
