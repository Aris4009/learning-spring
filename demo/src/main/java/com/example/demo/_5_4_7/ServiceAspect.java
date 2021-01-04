package com.example.demo._5_4_7;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ServiceAspect {

	// args,匹配到连接点(使用Spring AOP时方法的执行)的限制，其中参数是给定类型的实例。也可以使用参数名代替参数类型，相应的参数值会传递给通知方法

	private final int DEFAULT_TIME = 3;

	private int max = DEFAULT_TIME;

	public void setMax(int max) {
		this.max = max;
	}

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Pointcut(value = "execution(public java.lang.String com.example.demo._5_4_7.Service..*buy(java.lang.String)) && args(name)")
	public void doAround(String name) {
	}

	@Around(value = "doAround(name)", argNames = "pj,name")
	public Object around(ProceedingJoinPoint pj, String name) throws Throwable {
		log.info("name:{}", name);
		log.info("before");
		int n = 0;
		while (n < max) {
			try {
				return pj.proceed();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			n++;
		}
		log.info("after retry:{} times", max);
		return null;
	}
}
