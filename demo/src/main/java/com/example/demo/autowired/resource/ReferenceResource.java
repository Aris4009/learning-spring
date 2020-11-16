package com.example.demo.autowired.resource;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 注入map、数组
 */
@Component
public class ReferenceResource {

	@Resource
	private Map<String, String> mapResource;

	@Resource
	private String[] arr;

	public ReferenceResource(Map<String, String> mapResource, String[] arr) {
		this.mapResource = mapResource;
		this.arr = arr;
	}

	@Override
	public String toString() {
		return "ReferenceResource{" + "map=" + mapResource + ", arr=" + Arrays.toString(arr) + '}';
	}

	private static Logger log = LoggerFactory.getLogger(ReferenceResource.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.autowired.resource");
		ReferenceResource bean = context.getBean("referenceResource", ReferenceResource.class);
		log.info("{}", bean);
		context.close();
	}
}
