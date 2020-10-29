package com.example.demo.lazy.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 在容器级别控制延迟初始化的行为
 */
public class ControlLazyInit {

	public static Logger log = LoggerFactory.getLogger(ControlLazyInit.class);

	public static ControlLazyInit init() {
		log.info("init bean");
		return new ControlLazyInit();
	}

	public static void main(String[] args) throws InterruptedException {
		String path = "classpath:controlLazyInit.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		Thread.sleep(2000L);
		ControlLazyInit bean = context.getBean("bean", ControlLazyInit.class);
		context.close();
	}
}
