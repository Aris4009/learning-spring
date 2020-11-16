package com.example.demo.autowired.resource;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用@Resource注入Map或者数组
 */
@Configuration
public class ResourceConfigurer {

	@Bean
	public Map<String, String> mapResource() {
		Map<String, String> map = new HashMap<>();
		map.put("1", "2");
		return map;
	}

	@Bean
	public String[] strArrResource() {
		String[] arr = new String[2];
		arr[0] = "3";
		arr[1] = "4";
		return arr;
	}

	@Bean
	public Map<String, String> mapResource2() {
		Map<String, String> map = new HashMap<>();
		map.put("333", "444");
		return map;
	}
}
