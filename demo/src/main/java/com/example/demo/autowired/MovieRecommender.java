package com.example.demo.autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MovieRecommender {

	private MovieCatalog movieCatalog;

	private CustomerPreferenceDao customerPreferenceDao;

	@Autowired
	public void prepare(MovieCatalog movieCatalog, CustomerPreferenceDao customerPreferenceDao) {
		this.movieCatalog = movieCatalog;
		this.customerPreferenceDao = customerPreferenceDao;
	}

	private static Logger log = LoggerFactory.getLogger(MovieRecommender.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.autowired");
		log.info("{}", context.getBean("movieRecommender", MovieRecommender.class).hashCode());
		context.close();
	}
}
