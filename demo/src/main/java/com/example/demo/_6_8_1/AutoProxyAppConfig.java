package com.example.demo._6_8_1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo._6_8_1.auto.proxy.AutoProxyService1;
import com.example.demo._6_8_1.auto.proxy.AutoProxyService2;
import com.example.demo._6_8_1.auto.proxy.Service;

@Configuration
public class AutoProxyAppConfig {

	@Bean
	public Advice advice() {
		return new TestAdvice();
	}

	// 此处需为静态方法
	@Bean
	public static BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
		BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
		beanNameAutoProxyCreator.setBeanNames("autoProxyService1", "autoProxyService2");
		beanNameAutoProxyCreator.setInterceptorNames("advice");
		return beanNameAutoProxyCreator;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AutoProxyAppConfig.class, AutoProxyService1.class, AutoProxyService2.class, Service.class);
		context.refresh();

		AutoProxyService1 service1 = context.getBean("autoProxyService1", AutoProxyService1.class);
		service1.test("hh");

		AutoProxyService2 service2 = context.getBean(AutoProxyService2.class);
		service2.test("mm");

		Service service = context.getBean(Service.class);
		service.test("nn");
	}
}
