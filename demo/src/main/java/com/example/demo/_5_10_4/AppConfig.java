package com.example.demo._5_10_4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@ComponentScan
@EnableLoadTimeWeaving
/**
 * 该实例无法运行，请参考lwt的源码
 */
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		Service service = context.getBean(Service.class);
		service.test();
	}
}
