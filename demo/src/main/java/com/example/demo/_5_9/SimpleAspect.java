package com.example.demo._5_9;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class SimpleAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Pointcut("execution(public void com.example.demo._5_9.Simple..*(..))")
	public void before() {
	}

	@Before(value = "before()")
	public void doBefore() {
		log.info("before");
	}
}
