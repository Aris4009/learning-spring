package com.example.demo.prototype;

import java.util.function.Function;

public class FunctionBeanFactory implements Function {

	@Override
	public Object apply(Object o) {
		return new PrototypeBean("bb", 55);
	}
}
