package com.example.demo.autowired.self.reference;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类自引用bean
 */
@Configuration
public class SelfReference {

	private Map<String, String> map;

	public Map<String, String> getMap() {
		return map;
	}

	@Autowired
	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	/**
	 * 标记为static，解决自引用问题，相当于解藕
	 */
	@Bean
	public static Map<String, String> map() {
		Map<String, String> map = new HashMap<>();
		map.put("1", "2");
		return map;
	}

	@Override
	public String toString() {
		return "SelfReference{" + "map=" + map + '}';
	}

	private static Logger log = LoggerFactory.getLogger(SelfReference.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.autowired.self.reference");
		SelfReference bean = context.getBean("selfReference", SelfReference.class);
		log.info("{}", bean);
		context.close();
	}
}
