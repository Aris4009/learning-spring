package com.example.demo.wiring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.example.demo.hello.Hello;
import com.example.demo.using.depends.on.ClassC;

/**
 * 自动装配的四种模式
 */
public class Autowiring {

	public static Logger log = LoggerFactory.getLogger(ClassC.class);

	private Hello no;

	private Hello byName;

	private Hello byType;

	private Hello construct;

	public Autowiring(Hello construct) {
		this.construct = construct;
	}

	public Autowiring() {
	}

	public static void main(String[] args) {
		String path = "classpath:autowiring.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		Autowiring no = context.getBean("no", Autowiring.class);
		Autowiring byName = context.getBean("byName", Autowiring.class);
		Autowiring byType = context.getBean("byType", Autowiring.class);
		Autowiring construct = context.getBean("construct", Autowiring.class);

		log.info("{}", no.hashCode());
		log.info("{}", byName.hashCode());
		log.info("{}", byType.hashCode());
		log.info("{}", construct.hashCode());

		context.close();
	}
}
