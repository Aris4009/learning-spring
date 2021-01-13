package com.example.demo._6_4_3;

import java.util.Random;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean("serviceImpl")
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
	// JDK代理
	public ProxyFactoryBean jdkProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(serviceImpl());
		proxyFactoryBean.addAdvice(advice());
		// 如果此属性设置为true，则强制创建CGLIB代理
//		proxyFactoryBean.setProxyTargetClass(true);

		// 设置代理对象需实现的接口
		proxyFactoryBean.addInterface(IService.class);
		proxyFactoryBean.addInterface(IService2.class);
		return proxyFactoryBean;
	}

	@Bean("cglibProxyFactoryBean")
	// CGLIB代理
	public ProxyFactoryBean cglibProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(service());
		proxyFactoryBean.addAdvice(advice());
		return proxyFactoryBean;
	}

	@Bean("prototypeService")
	@Scope("prototype")
	public IService prototypeService() {
		return new ServiceImpl("_A" + new Random().nextInt(Integer.MAX_VALUE));
	}

	@Bean("prototypeAdvice")
	@Scope("prototype")
	public Advice prototypeAdvice() {
		return new TestAdvice();
	}

	@Bean("prototypeProxyFactoryBean")
	public ProxyFactoryBean prototypeProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setSingleton(false);
		proxyFactoryBean.setTarget(prototypeService());
		proxyFactoryBean.setTargetName("prototypeService");
		proxyFactoryBean.setInterceptorNames("prototypeAdvice");
		return proxyFactoryBean;
	}

	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class, DependencyProxiedObject.class);
		context.refresh();

		IService iService = context.getBean("jdkProxyFactoryBean", IService.class);
		log.info("{},hashcode:{}", iService.getClass().getName(), iService.hashCode());
		iService.test("李四");

		log.info("==================");
		IService2 iService2 = context.getBean("jdkProxyFactoryBean", IService2.class);
		log.info("{},hashcode:{}", iService2.getClass().getName(), iService2.hashCode());
		iService2.service2();

//		log.info("==================");
//		IService3 iService3 = context.getBean(IService3.class);
//		log.info("{},hashcode:{}", iService3.getClass().getName(), iService3.hashCode());
//		iService3.service3();

		log.info("==================");
		Service service = context.getBean(Service.class);
		log.info("{}", service.getClass().getName());
		service.test("张三");

		log.info("==================");
		log.info("==================");

		// prototype 配置
		for (int i = 0; i < 10; i++) {
			ProxyFactoryBean proxyFactoryBean = context.getBean("&prototypeProxyFactoryBean", ProxyFactoryBean.class);
			IService tmp = (IService) proxyFactoryBean.getObject();
			assert tmp != null;
			log.info("{},hashcode:{}，objectType:{}", tmp.getClass().getName(), tmp.hashCode(),
					proxyFactoryBean.getObjectType());
			tmp.test("李四");
			log.info("advice:{}", proxyFactoryBean.getAdvisors()[0].hashCode());
		}

		// 获取代理对象实例
		log.info("--------------------");
		IService impl = context.getBean("jdkProxyFactoryBean", IService.class);
		log.info("{}", impl.getClass().getName());
		impl.test("haha");

		// 获取依赖代理对象的实例
		log.info("------------------");
		DependencyProxiedObject dependencyProxiedObject = context.getBean(DependencyProxiedObject.class);
		dependencyProxiedObject.getService().test("i am dependencyProxiedObject");

		// 代理对象可转换为Advised接口
		log.info("------------------");
		Advised advised = (Advised) impl;
		log.info("{}", advised.hashCode());
	}
}
