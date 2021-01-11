package com.example.demo._6_4_3;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	public IService serviceImpl() {
		return new ServiceImpl("张三");
	}

	public Service service() {
		return new Service("李四");
	}

	@Bean
	public Advice advice() {
		return new TestAdvice();
	}

	@Bean("jdkProxyFactoryBean")
	public ProxyFactoryBean jdkProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(serviceImpl());
		proxyFactoryBean.addAdvice(advice());
		return proxyFactoryBean;
	}

	@Bean("cglibProxyFactoryBean")
	public ProxyFactoryBean cglibProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(service());
		proxyFactoryBean.addAdvice(advice());
		return proxyFactoryBean;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		IService iService = context.getBean(IService.class);
		log.info("{}", iService.getClass().getName());
		iService.test("李四");

		Service service = context.getBean(Service.class);
		log.info("{}", service.getClass().getName());
		service.test("张三");
	}
}
