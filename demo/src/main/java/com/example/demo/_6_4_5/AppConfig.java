package com.example.demo._6_4_5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

@Configuration
@ComponentScan
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean("globalDebug")
	public DebugInterceptor globalDebug() {
		DebugInterceptor debugInterceptor = new DebugInterceptor();
		debugInterceptor.setUseDynamicLogger(true);
		return new DebugInterceptor();
	}

	@Bean("globalPerformance")
	public PerformanceMonitorInterceptor globalPerformance() {
		return new PerformanceMonitorInterceptor(false);
	}

	@Bean("globalCustomInterceptor")
	public CustomInterceptor globalCustom() {
		return new CustomInterceptor();
	}

	@Bean("proxyFactoryBean")
	public ProxyFactoryBean proxyFactoryBean() {
		ProxyFactoryBean factoryBean = new ProxyFactoryBean();
		factoryBean.setTarget(new ServiceImpl("li"));
		factoryBean.setInterceptorNames("global*");
		return factoryBean;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		IService service = (IService) context.getBean("&proxyFactoryBean", ProxyFactoryBean.class).getObject();
		if (!ObjectUtils.isEmpty(service)) {
			for (int i = 0; i < 20; i++) {
				service.process(String.valueOf(10));
			}
		}

		DebugInterceptor debugInterceptor = context.getBean(DebugInterceptor.class);
		log.info("{}", debugInterceptor.getCount());
	}
}
