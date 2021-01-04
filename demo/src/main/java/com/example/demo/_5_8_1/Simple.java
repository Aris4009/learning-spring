package com.example.demo._5_8_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.framework.ProxyFactory;

public class Simple implements Pojo {

	private static Logger log = LoggerFactory.getLogger(Simple.class);

	@Override
	public void foo() {
		// this next method invocation is a direct call on the 'this' reference
//		this.bar();

		// this works, but... gah!
		((Pojo) AopContext.currentProxy()).bar();
	}

	@Override
    public void bar() {
		// some logic...
		log.info("this is logic");
	}

	public static void main(String[] args) {
//		Pojo pojo = new Simple();
//		pojo.foo();

		Object target = new Simple();
		ProxyFactory proxyFactory = new ProxyFactory(target);
		proxyFactory.setExposeProxy(true);
		proxyFactory.addInterface(Pojo.class);
		proxyFactory.addAdvice(new RetryAdvice());
		Pojo pojo = (Pojo) proxyFactory.getProxy();
		log.info("{}", pojo.getClass().getName());
		pojo.foo();
	}
}
