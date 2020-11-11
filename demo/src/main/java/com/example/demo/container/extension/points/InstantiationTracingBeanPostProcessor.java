package com.example.demo.container.extension.points;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		log.info("Bean '" + beanName + "' created : " + bean.toString());
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		log.info("Bean '" + beanName + "' created : " + bean.toString());
		return bean;
	}

	private static Logger log = LoggerFactory.getLogger(InstantiationTracingBeanPostProcessor.class);

	public static void main(String[] args) {
		String path = "classpath:container/extension/points/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		Bean bean = context.getBean("bean", Bean.class);
		log.info(context.toString());
		context.close();
	}
}
