package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.example.demo.hello.Hello;

public class ReferencesToOtherBean {

	private Hello hello;

	public ReferencesToOtherBean(Hello hello) {
		this.hello = hello;
	}

	private static final Logger log = LoggerFactory.getLogger(ReferencesToOtherBean.class);

	public static void main(String[] args) {
		String path = "classpath:refToOtherBean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		ReferencesToOtherBean rToB = context.getBean("rToB", ReferencesToOtherBean.class);
		ReferencesToOtherBean rToA = context.getBean("rToA", ReferencesToOtherBean.class);
		log.info("hashCode:{},hello.hashCode:{},hello:{}", rToB.hashCode(), rToB.hello.hashCode(), rToB.hello);
		log.info("hashCode:{},hello.hashCode:{},hello:{}", rToA.hashCode(), rToA.hello.hashCode(), rToA.hello);
		context.close();
	}
}
