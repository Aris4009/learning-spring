package com.example.demo.custom.nature.bean.destruction.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 销毁回调方法
 */
public class DestructionBean {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String name = "test";

	public void destroy() {
		this.name = null;
		log.info("{}-{}", this.getClass(), "销毁");
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/initialization/callback/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
