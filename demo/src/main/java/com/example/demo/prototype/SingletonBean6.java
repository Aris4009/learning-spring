package com.example.demo.prototype;

import org.springframework.beans.factory.ObjectFactory;

public class SingletonBean6 {

	private ObjectFactory<PrototypeBean> prototypeBeanObjectFactory;

	public SingletonBean6(ObjectFactory<PrototypeBean> prototypeBeanObjectFactory) {
		this.prototypeBeanObjectFactory = prototypeBeanObjectFactory;
	}

	public ObjectFactory<PrototypeBean> getPrototypeBeanObjectFactory() {
		return prototypeBeanObjectFactory;
	}
}
