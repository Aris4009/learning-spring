package com.example.demo._5_10_1.preconstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable(preConstruction = true)
//The @Configurable annotation, in this case, marks the PersonObject class as being eligible for Spring-driven configuration.
public class PersonObject {

	private static Logger log = LoggerFactory.getLogger(PersonObject.class);

	private int id;

	private String name;

	@Autowired
	private IdService idService;

	public PersonObject(String name) {
		log.info("pre construct:{}", idService.generateId());
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
