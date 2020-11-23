package com.example.demo.prototype;

import com.example.demo.gson.JSON;

/**
 * 要注入的原型bean的代理
 */
public class PrototypeBeanScopedProxy {

	private PrototypeBean prototypeBean;

	public PrototypeBeanScopedProxy(PrototypeBean prototypeBean) {
		this.prototypeBean = prototypeBean;
	}

	public PrototypeBean getPrototypeBean() {
		return prototypeBean;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(prototypeBean);
	}
}
