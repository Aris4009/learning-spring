package com.example.demo._6_1_4;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

//	@Bean
//	@Qualifier("beforeAdvice")
//	public Advice initBeforeAdvice() {
//		return new MethodBeforeAdvice() {
//
//			private final Logger log = LoggerFactory.getLogger(this.getClass());
//
//			@Override
//			public void before(Method method, Object[] args, Object target) throws Throwable {
//				log.info("init before:{}", method.getName());
//			}
//		};
//	}

	@Bean
	public Advice initAdvice() {
		return new MethodInterceptor() {
			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				log.info("before:{}", invocation.getMethod());
				Object obj = invocation.proceed();
				log.info("after:{}", invocation.getMethod());
				return obj;
			}
		};
	}

	@Bean
	public RegexpMethodPointcutAdvisor initAdvisor() {
		RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor();
		List<String> list = new ArrayList<>();
		String setPattern = ".*set.*";
		String absPattern = ".*absquatulate";
		list.add(setPattern);
		list.add(absPattern);
		advisor.setPatterns(list.toArray(new String[0]));
		advisor.setAdvice(initAdvice());
		return advisor;
	}

	@Bean
	public ProxyFactoryBean proxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.addAdvisor(initAdvisor());
		proxyFactoryBean.setTarget(new JdkRegexpMethodPointService("张三"));
		return proxyFactoryBean;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		JdkRegexpMethodPointService jdkRegexpMethodPointService = context.getBean(JdkRegexpMethodPointService.class);
		jdkRegexpMethodPointService.setName("aaaa");
		jdkRegexpMethodPointService.absquatulate();
	}
}
