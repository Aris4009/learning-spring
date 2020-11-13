package com.example.demo.autowired.primary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Configuration
public class MovieConfiguration {

	@Bean
	@Primary
	public MovieCatalog first() {
		MovieCatalog movieCatalog = new MovieCatalog("first");
		return movieCatalog;
	}

	@Bean
	public MovieCatalog second() {
		MovieCatalog movieCatalog = new MovieCatalog("second");
		return movieCatalog;
	}

	private static Logger log = LoggerFactory.getLogger(MovieConfiguration.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.autowired.primary");
		MovieRecommender bean = context.getBean("movieRecommender", MovieRecommender.class);
		log.info("{},{}", bean, bean.hashCode());
		context.close();

		String path = "classpath:autowired/primary/bean.xml";
		ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext(path);
		bean = classPathXmlApplicationContext.getBean("movieRecommender", MovieRecommender.class);
		log.info("{},{}", bean, bean.hashCode());
		classPathXmlApplicationContext.close();
	}
}
