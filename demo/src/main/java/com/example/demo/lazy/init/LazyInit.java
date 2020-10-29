package com.example.demo.lazy.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LazyInit {

	public static Logger log = LoggerFactory.getLogger(LazyInit.class);

	public static LazyInit init(String name) {
		log.info("i am init:{}", name);
		return new LazyInit();
	}

	public static void main(String[] args) throws InterruptedException {
		String path = "classpath:lazyInit.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		Thread.sleep(2000L);
		LazyInit lazyInit = context.getBean("lazyInit", LazyInit.class);
		context.close();
	}
}
