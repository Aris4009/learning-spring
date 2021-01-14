package com.example.demo._10_1_1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Client {

	private EnumType enumType;

	@Autowired
	public void setEnumType(@Value("TYPE2") EnumType enumType) {
		this.enumType = enumType;
	}

	public EnumType getEnumType() {
		return enumType;
	}
}
