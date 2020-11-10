package com.example.demo.custom.nature.bean.initialization.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 实现InitializingBean接口，不推荐使用
 */
public class InitializingBeanImpl implements InitializingBean {

	private String age;

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("{}", this.hashCode());
		log.info("初始化bean后执行该方法");
		this.age = "33";
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/initialization/callback/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
