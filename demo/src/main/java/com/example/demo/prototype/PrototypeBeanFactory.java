package com.example.demo.prototype;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;

public class PrototypeBeanFactory implements ObjectFactory {

	@Override
	public Object getObject() throws BeansException {
		return new PrototypeBean("aaa", 4);
	}
}
