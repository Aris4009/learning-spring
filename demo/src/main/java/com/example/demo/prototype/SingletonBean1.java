package com.example.demo.prototype;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.example.demo.gson.JSON;

public class SingletonBean1 implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private PrototypeBean prototypeBean;

	public PrototypeBean getPrototypeBean() {
		prototypeBean = applicationContext.getBean("prototypeBean", PrototypeBean.class);
		return prototypeBean;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(prototypeBean);
	}
}
