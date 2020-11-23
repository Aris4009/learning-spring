package com.example.demo.prototype;

import javax.inject.Provider;

import com.example.demo.gson.JSON;

public class SingletonBean3 {

	public SingletonBean3(Provider<PrototypeBean> prototypeBeanProvider) {
		this.prototypeBeanProvider = prototypeBeanProvider;
	}

	private final Provider<PrototypeBean> prototypeBeanProvider;

	private PrototypeBean prototypeBean;

	public PrototypeBean getPrototypeBean() {
		prototypeBean = prototypeBeanProvider.get();
		return prototypeBean;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(prototypeBean);
	}
}
