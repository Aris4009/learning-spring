package com.example.demo._6_8_1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAdvice implements MethodInterceptor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		log.info("before:{},{}", invocation.getMethod().getName(), invocation.getArguments());
		Object obj = invocation.proceed();
		log.info("after:{},{}", invocation.getMethod().getName(), invocation.getArguments());
		return obj;
	}
}
