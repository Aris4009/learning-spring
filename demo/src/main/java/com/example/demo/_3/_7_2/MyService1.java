package com.example.demo._3._7_2;

import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyService1 {

	private final Validator validator;

	@Autowired
	public MyService1(Validator validator) {
		this.validator = validator;
	}
}
