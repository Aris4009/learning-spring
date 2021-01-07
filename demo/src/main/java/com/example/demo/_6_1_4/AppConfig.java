package com.example.demo._6_1_4;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import com.example.demo.gson.JSON;

@Component
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

	@Bean("beforeAdvice")
	public static Advice initBeforeAdvice() {
		return new MethodBeforeAdvice() {

			private final Logger log = LoggerFactory.getLogger(this.getClass());

			@Override
			public void before(Method method, Object[] args, Object target) throws Throwable {
				log.info("{}", JSON.toJSONString(args));
				method.invoke(target, args);
			}
		};
	}

	@Bean
	public RegexpMethodPointcutAdvisor initAdvisor() {
		RegexpMethodPointcutAdvisor advisor = new RegexpMethodPointcutAdvisor();
		List<Pattern> list = new ArrayList<>();
		Pattern setPattern = Pattern.compile(".*set.*");
		Pattern absPattern = Pattern.compile(".*absquatulate");
		list.add(setPattern);
		list.add(absPattern);
		advisor.setPatterns(list.toArray(new String[0]));
		advisor.setAdvice(initBeforeAdvice());
		return advisor;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class, JdkRegexpMethodPointService.class);
		context.refresh();
		JdkRegexpMethodPointService jdkRegexpMethodPointService = context.getBean(JdkRegexpMethodPointService.class);
		jdkRegexpMethodPointService.setName("aaaa");
		jdkRegexpMethodPointService.absquatulate();
	}
}
