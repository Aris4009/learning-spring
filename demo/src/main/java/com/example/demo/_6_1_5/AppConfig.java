package com.example.demo._6_1_5;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean
	public Advisor initAdvisor() {
		StaticMethodMatcherPointcutAdvisor advice = new StaticMethodMatcherPointcutAdvisor() {
			@Override
			public boolean matches(Method method, Class<?> targetClass) {
				return targetClass == Service.class && method.getName().equals("getName");
			}
		};
		advice.setAdvice(initAdvice());
		return advice;
	}

	@Bean
	public Advice initAdvice() {
		return new MethodInterceptor() {
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				log.info("before:{}", invocation.getMethod().getName());
				Object obj = invocation.proceed();
				log.info("after:{}", invocation.getMethod().getName());
				return obj;
			}
		};
	}

	@Bean
	public ProxyFactoryBean proxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(new Service("张三"));
		proxyFactoryBean.addAdvisor(initAdvisor());
		return proxyFactoryBean;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		Service service = context.getBean(Service.class);
		String name = service.getName();
		int age = service.getAge();
		log.info("name:{},age:{}", name, age);
	}
}
