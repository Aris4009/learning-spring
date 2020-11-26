package com.example.demo._1_12_4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	@Bean
	public ClientService clientService1() {
		ClientServiceImpl clientService = new ClientServiceImpl();
		clientService.setClientDao(clientDao());
		return clientService;
	}

	@Bean
	public ClientService clientService2() {
		ClientServiceImpl clientService = new ClientServiceImpl();
		clientService.setClientDao(clientDao());
		return clientService;
	}

	@Bean
	public ClientDao clientDao() {
		return new ClientDaoImpl();
	}

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		ClientServiceImpl clientService1 = context.getBean("clientService1", ClientServiceImpl.class);
		ClientServiceImpl clientService2 = context.getBean("clientService2", ClientServiceImpl.class);
		log.info("{},{}", clientService1.hashCode(), clientService1.getClientDao().hashCode());
		log.info("{},{}", clientService2.hashCode(), clientService2.getClientDao().hashCode());
		context.close();
	}
}
