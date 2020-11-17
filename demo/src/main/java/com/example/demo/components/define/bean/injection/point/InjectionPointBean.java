package com.example.demo.components.define.bean.injection.point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 创建prototype实例，编写一个带有InjectionPoint参数的工厂方法
 */
@Configuration
public class InjectionPointBean {

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static List<String> list(InjectionPoint injectionPoint) {
		log.info("{}", injectionPoint.getMember());
		List<String> list = new ArrayList<>();
		list.add(String.valueOf(new Random().nextInt()));
		return list;
	}

	private static Logger log = LoggerFactory.getLogger(InjectionPointBean.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.components.define.bean.injection.point");
		for (int i = 0; i < 2; i++) {
			Thread thread = new Thread(() -> {
				TBean tBean = context.getBean("tBean", TBean.class);
				log.info("{},{}", tBean.hashCode(), tBean.getList().hashCode());
			});
			thread.start();
		}
		context.close();
	}
}
