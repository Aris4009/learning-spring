package com.example.demo.arbitrary.method.replacement;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.MethodReplacer;

/**
 * 替代方法,重载原方法
 */
public class ReplacementObj implements MethodReplacer {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object reimplement(Object o, Method method, Object[] objects) throws Throwable {
		String arg = (String) objects[0];
		log.info("I am replacement method,the arg is {}", arg);
		return arg;
	}
}
