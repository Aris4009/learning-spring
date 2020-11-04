package com.example.demo.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyInvocationHandler implements InvocationHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Object object;

	public MyInvocationHandler(Object object) {
		this.object = object;
	}

	public MyInvocationHandler() {
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		log.info("before");
		Object invoke = method.invoke(object, args);
		log.info("after");
		return invoke;
	}
}
