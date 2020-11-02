package com.example.demo.bean.scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * 实现BeanPostProcessor接口，管理资源
 */
public class BeanPostProcessorImpl implements BeanPostProcessor, Ordered {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (StringUtils.endsWithIgnoreCase("singleton", beanName)) {
			log.info("before:{},{}", beanName, bean.hashCode());
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (StringUtils.endsWithIgnoreCase("singleton", beanName)) {
			log.info("after:{},{}", beanName, bean.hashCode());
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
