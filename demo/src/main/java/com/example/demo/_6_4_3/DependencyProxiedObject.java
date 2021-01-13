package com.example.demo._6_4_3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 可以透明的像依赖其他普通Java对象一样，依赖被代理的对象
 */
@Component
public class DependencyProxiedObject {

	private final IService service;

	@Autowired
	public DependencyProxiedObject(@Qualifier("jdkProxyFactoryBean") IService service) {
		this.service = service;
	}

	public IService getService() {
		return service;
	}
}
