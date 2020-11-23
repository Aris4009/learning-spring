package com.example.demo.prototype;

public abstract class SingletonBean4 {

	private PrototypeBean prototypeBean;

	public PrototypeBean getPrototypeBean() {
		this.prototypeBean = initPrototypeBean();
		return prototypeBean;
	}

	public abstract PrototypeBean initPrototypeBean();
}
