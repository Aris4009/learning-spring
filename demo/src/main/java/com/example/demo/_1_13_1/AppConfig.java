package com.example.demo._1_13_1;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.demo.gson.JSON;

@Configuration
public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Bean("list")
	@Profile("dev")
	public List<String> devList() {
		List<String> list = new ArrayList<>();
		list.add("dev");
		log.info(JSON.toJSONString(list));
		return list;
	}

	@Bean("list")
	@Profile("pro")
	public List<String> proList() {
		List<String> list = new ArrayList<>();
		list.add("pro");
		log.info(JSON.toJSONString(list));
		return list;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.getEnvironment().setActiveProfiles("dev");
		context.refresh();
		context.close();
	}
}
