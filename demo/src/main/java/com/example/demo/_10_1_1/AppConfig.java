package com.example.demo._10_1_1;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean
	@Scope("prototype")
	public PrototypeBean prototypeBean() {
		return new PrototypeBean("3", 4);
	}

	@Bean
	public PropertyPathFactoryBean propertyPathFactoryBean() {
		PropertyPathFactoryBean propertyPathFactoryBean = new PropertyPathFactoryBean();
		propertyPathFactoryBean.setTargetBeanName("prototypeBean");
		propertyPathFactoryBean.setPropertyPath("age");
		return propertyPathFactoryBean;
	}

	@Bean
	public PropertiesFactoryBean propertiesFactoryBean() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("format.properties"));
		return propertiesFactoryBean;
	}

	@Bean
	public ListFactoryBean listFactoryBean() {
		ListFactoryBean listFactoryBean = new ListFactoryBean();
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			list.add(String.valueOf(i));
		}
		listFactoryBean.setSourceList(list);
		return listFactoryBean;
	}

	@Bean
	public MapFactoryBean mapFactoryBean() {
		MapFactoryBean factoryBean = new MapFactoryBean();
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < 2; i++) {
			map.put(String.valueOf(i), i);
		}
		factoryBean.setSourceMap(map);
		return factoryBean;
	}

	@Bean
	public SetFactoryBean setFactoryBean() {
		SetFactoryBean factoryBean = new SetFactoryBean();
		Set<String> set = new TreeSet<>();
		for (int i = 0; i < 2; i++) {
			set.add(String.valueOf(i + 99));
		}
		factoryBean.setSourceSet(set);
		return factoryBean;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		String[] arr = context.getBeanDefinitionNames();
		for (String s : arr) {
			log.info(s);
		}

		Client client = context.getBean(Client.class);
		log.info("{}", client.getEnumType());

		log.info("{}", context.getBean("propertyPathFactoryBean"));
		log.info("{}", context.getBean("propertyPathFactoryBean").getClass());

		Properties properties = context.getBean("propertiesFactoryBean", Properties.class);
		log.info("{}", properties);

		List<String> list = context.getBean("listFactoryBean", List.class);
		log.info("{}", list);

		Map<String, Integer> map = context.getBean("mapFactoryBean", Map.class);
		log.info("{}", map);

		Set<String> set = context.getBean("setFactoryBean", Set.class);
		log.info("{}", set);
	}
}
