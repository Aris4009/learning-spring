package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.example.demo.hello.Hello;

/**
 * 内部bean
 */
public class InnerBean {

	private Hello hello;

	private static final Logger log = LoggerFactory.getLogger(InnerBean.class);

	public Hello getHello() {
		return hello;
	}

	public void setHello(Hello hello) {
		this.hello = hello;
	}

	public static void main(String[] args) {
		String path = "classpath:innerBean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		InnerBean innerBean = context.getBean("innerBean", InnerBean.class);
		Hello hello = context.getBean("hello", Hello.class);
		log.info("inner.hello.hashCode:{},hello.hashCode:{},inner.hello:{},hello:{}", innerBean.hello.hashCode(),
				hello.hashCode(), innerBean.hello, hello);
		context.close();
	}
}
