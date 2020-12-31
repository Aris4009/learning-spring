package com.example.demo._5_4_2._1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableAspectJAutoProxy
public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("com/example/demo/_5_4_2/_1");
		context.refresh();
		Service service = context.getBean(Service.class);
		log.info("{}", service.getClass());
		log.info(service.toString());
		long start = System.nanoTime();
		process(service, 5);
		long end = System.nanoTime();
		log.info("{}", end - start);
	}

	private static void process(Service service, int arg) throws Exception {
		switch (arg) {
		case 1:
			service.sayHello();
			break;
		case 2:
			log.info("{}", service.sayHello(100));
			break;
		case 3:
			log.info("{}", service.sayHello("李四", 100, false));
			break;
		case 4:
			log.info("{}", service.sayHello("李四"));
			break;
		case 5:
			log.info("{}", service.sayHello("李四", 100));
			break;
		}
	}
}
