package com.example.demo._5_9;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public class Simple implements Pojo {

	private static Logger log = LoggerFactory.getLogger(Simple.class);

	@Override
	public void foo() {
		// this next method invocation is a direct call on the 'this' reference
		this.bar();

		// this works, but... gah!
//		((Pojo) AopContext.currentProxy()).bar();
	}

	@Override
	public void bar() {
		// some logic...
		log.info("this is logic");
	}

	public static void main(String[] args) {
		Object target = new Simple();
		AspectJProxyFactory factory = new AspectJProxyFactory(target);

		SimpleAspect aspect = new SimpleAspect();
		factory.addAspect(aspect);

		Pojo pojo = factory.getProxy();
		log.info("{}", pojo.getClass().getName());
		pojo.foo();

		pojo.bar();
	}

}
