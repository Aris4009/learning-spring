package com.example.demo._6_4_5;

public class ServiceImpl implements IService {

	private String name;

	public ServiceImpl(String name) {
		this.name = name;
	}

	@Override
	public String process(String name) {
		this.name = name;
		return name;
	}

	public String getName() {
		return name;
	}
}
