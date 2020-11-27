package com.example.demo._1_13_3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:_1_13_3/app.properties")
public class AppConfig {

	private Environment environment;

	@Autowired
	public AppConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	public TestBean testBean() {
		TestBean testBean = new TestBean();
		testBean.setName(environment.getProperty("testbean.name"));
		return testBean;
	}

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		TestBean testBean = context.getBean("testBean", TestBean.class);
		log.info(testBean.getName());
		context.close();
	}
}
