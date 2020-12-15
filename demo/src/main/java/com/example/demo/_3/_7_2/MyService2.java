package com.example.demo._3._7_2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

@Service
public class MyService2 {

	private Validator validator;

	@Autowired
	public MyService2(Validator validator) {
		this.validator = validator;
	}
}
