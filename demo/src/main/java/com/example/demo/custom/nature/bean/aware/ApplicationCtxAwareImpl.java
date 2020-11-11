package com.example.demo.custom.nature.bean.aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 通过实现接口ApplicationContextAware,来获取ApplicationContext的引用
 */
public class ApplicationCtxAwareImpl implements ApplicationContextAware {

	private ApplicationContext context;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

	public void getAppCtx() {
		ApplicationCtxAwareImpl bean = context.getBean("appCtxImpl", ApplicationCtxAwareImpl.class);
		log.info("{}", bean.hashCode());
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/aware/aware.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationCtxAwareImpl bean = context.getBean("appCtxImpl", ApplicationCtxAwareImpl.class);
		bean.getAppCtx();
		context.close();
	}
}
