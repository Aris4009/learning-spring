package com.example.demo._3._7_2;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MyConstraintValidator implements ConstraintValidator<MyConstraint, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.length() < 8) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void initialize(MyConstraint constraintAnnotation) {

	}
}
