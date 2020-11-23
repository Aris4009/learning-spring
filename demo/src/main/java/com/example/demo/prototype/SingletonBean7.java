package com.example.demo.prototype;

import java.util.function.Function;

public class SingletonBean7 {

	private Function<String, PrototypeBean> beanFactory;

	public SingletonBean7(Function<String, PrototypeBean> beanFactory) {
		this.beanFactory = beanFactory;
	}

	public Function<String, PrototypeBean> getBeanFactory() {
		return beanFactory;
	}
}
