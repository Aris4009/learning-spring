package com.example.demo._5_4_2;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ServiceAspect {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	// 必须定义切点，否则不生效
	@Pointcut("execution(* sayNothing())")
	public void sayHello() {
	}

	@Before("sayHello()")
	public void test() {
		log.info("I'm saying hello");
	}
}
