package com.example.demo._5_4_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Arrays;

@Configurable
@EnableAspectJAutoProxy
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		int code = 2;
		AnnotationConfigApplicationContext context = init(code);
		if (context == null) {
			return;
		}
		log.info("{}", Arrays.toString(context.getBeanDefinitionNames()));
		Service service = context.getBean("service", Service.class);
		log.info("{}", service.getClass().getName());
		service.sayNothing();
		context.close();
	}

	public static AnnotationConfigApplicationContext init(int code) {
		if (code == 1) {
			// 这种方式可以生效
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
			context.register(AppConfig.class, Service.class, ServiceAspect.class);
			context.refresh();
			return context;
		} else if (code == 2) {
			// 这种方式不生效？不知为何？
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
					"com/example/demo/_5_4_2");
			return context;
		} else if (code == 3) {
			// 这种方式可以生效，需要在AppConfig上加入包扫描注解@ComponentScan
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
			return context;
		} else if (code == 4) {
			// 这种方式可以生效，不需要加包扫描注解@ComponentScan
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class,
					Service.class, ServiceAspect.class);
			return context;
		} else {
			return null;
		}
	}
}
