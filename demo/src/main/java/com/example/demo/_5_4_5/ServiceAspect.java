package com.example.demo._5_4_5;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ServiceAspect {

	@DeclareParents(value = "com.example.demo._5_4_5.Service", defaultImpl = DefaultUsageTracked.class)
	public static UsageTracked mixin;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Pointcut("execution(public void com.example.demo._5_4_5..*test()) && this(usageTracked)")
	public void before(UsageTracked usageTracked) {
	}

	@Before(value = "before(usageTracked)")
	public void doBefore(UsageTracked usageTracked) {
		log.info("{}", usageTracked.incrementUseCount());
	}
}
