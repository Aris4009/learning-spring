package com.example.demo.autowired.custom.qualifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MovieRecommender {

	@Autowired
	@Genre("Action")
	private MovieCatalog actionCatalog;

	private MovieCatalog comedyCatalog;

	@Autowired
	public void setComedyCatalog(@Genre("Comedy") MovieCatalog comedyCatalog) {
		this.comedyCatalog = comedyCatalog;
	}

	public MovieCatalog getActionCatalog() {
		return actionCatalog;
	}

	public MovieCatalog getComedyCatalog() {
		return comedyCatalog;
	}

	private static Logger log = LoggerFactory.getLogger(MovieRecommender.class);

	public static void main(String[] args) {
		String path = "classpath:autowired.custom.qualifier/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		MovieRecommender bean = context.getBean("movieRecommender", MovieRecommender.class);
		log.info("{}", bean.getActionCatalog());
		log.info("{}", bean.getComedyCatalog());
		context.close();
	}
}