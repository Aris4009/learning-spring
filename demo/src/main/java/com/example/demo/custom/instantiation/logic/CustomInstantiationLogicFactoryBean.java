package com.example.demo.custom.instantiation.logic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component(value = "customFactoryBean")
public class CustomInstantiationLogicFactoryBean implements FactoryBean {

	@Override
	public Object getObject() throws Exception {
		String name = "hello";
		List<Integer> list = new ArrayList<>();
		list.add(1);
		ComplexBean complexBean = new ComplexBean(name, list);
		return complexBean;
	}

	@Override
	public Class<?> getObjectType() {
		return ComplexBean.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	private static Logger log = LoggerFactory.getLogger(CustomInstantiationLogicFactoryBean.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				CustomInstantiationLogicFactoryBean.class);
		ComplexBean bean = context.getBean("customFactoryBean", ComplexBean.class);
		CustomInstantiationLogicFactoryBean factoryBean = context.getBean("&customFactoryBean",
				CustomInstantiationLogicFactoryBean.class);
		log.info("{}", bean);
		log.info("{}", factoryBean);
		context.close();
	}
}
