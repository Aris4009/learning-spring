package com.example.demo._5_4_2._1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ServiceAspect {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Pointcut("execution(public void com.example.demo._5_4_2._1..*.sayHello())")
	public void before() {
	}

	@Pointcut("execution(protected int com.example.demo._5_4_2._1..*.sayHello(int))")
	public void afterReturning() {
	}

	@Pointcut("execution(public java.util.Map<String,Integer> com.example.demo._5_4_2._1..*.sayHello(String, int ,boolean))")
	public void afterThrowing() {
	}

	@Pointcut("execution(protected java.lang.String com.example.demo._5_4_2._1..*.sayHello(String))")
	public void after() {
	}

	@Pointcut("execution(public java.util.Map<String,Integer> com.example.demo._5_4_2._1..*.sayHello(String,int))")
	public void around() {
	}

	@Pointcut("execution(public java.lang.String com.example.demo._5_4_2._1..*.hello()) && within(com.example.demo._5_4_2._1.*)")
	public void dynamicAround() {

	}

	@Before("before()")
	public void doBefore() {
		log.info("before raw1");
	}

	@AfterReturning(pointcut = "afterReturning()", returning = "obj")
	// Please note that it is not possible to return a totally different reference
	// when using after returning advice.
	public void doAfterReturning(int obj) {
		log.info("after returning:{}", obj);
	}

	@AfterThrowing(pointcut = "afterThrowing()", throwing = "ex")
	public void doAfterThrowing(Exception ex) {
		log.info("after throwing:{}", ex.getMessage());
	}

	@After("after()")
	public void doAfter() {
		log.info("after");
	}

	@Around("around()")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		log.info("before");
		Object retVal = pjp.proceed(new Object[] { "王五", -100 });
		log.info("after");
		Map<String, Integer> map = new HashMap<>();
		map.put("新的值", -999);

		Object _this = pjp.getThis();
		log.info("this.class:{},this:{}", _this.getClass(), _this);
		Object target = pjp.getTarget();
		log.info("target.class:[],target:{}", target.getClass(), target);
		Object[] obj = pjp.getArgs();
		log.info("args:{}", Arrays.toString(obj));
		Signature signature = pjp.getSignature();
		log.info("signature:{}", signature);

		log.info(pjp.toString());
		return map;
	}

}
