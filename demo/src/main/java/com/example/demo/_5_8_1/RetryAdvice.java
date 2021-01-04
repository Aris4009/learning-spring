package com.example.demo._5_8_1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryAdvice implements MethodInterceptor {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		log.info("before");
		Object obj = invocation.proceed();
		log.info("after");
		return obj;
	}
}
