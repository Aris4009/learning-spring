package com.example.demo.prototype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.gson.JSON;

/**
 * 单例bean
 */
public class SingletonBean {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private PrototypeBean prototypeBean;

	public PrototypeBean getPrototypeBean() {
		return prototypeBean;
	}

	public SingletonBean() {
		log.info("The singleton init");
	}

	public void setPrototypeBean(PrototypeBean prototypeBean) {
		log.info("The prototype injected");
		this.prototypeBean = prototypeBean;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(prototypeBean);
	}
}
