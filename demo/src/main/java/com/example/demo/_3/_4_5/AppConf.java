package com.example.demo._3._4_5;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;

import com.example.demo.gson.JSON;

@Configurable
public class AppConf {

	private static Logger log = LoggerFactory.getLogger(AppConf.class);

	@Bean(name = "factory")
	public ConversionServiceFactoryBean factoryBean() {
		return new ConversionServiceFactoryBean();
	}

	@Bean(name = "conversionService")
	public ConversionService conversionService(ConversionServiceFactoryBean factoryBean) {
		return factoryBean.getObject();
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConf.class);
		ConversionService conversionService = context.getBean("conversionService", ConversionService.class);

		String a = "a,b,c,d,e";
		List<String> list = conversionService.convert(a, List.class);
		log.info("{}", JSON.toJSONString(list));
		log.info("{}", conversionService.convert(list, String.class));
		Integer[] integers = { 2, 3, 4, 5, 6, 7 };
		log.info("{}", conversionService.convert(integers, List.class));

		List<Test> testList = new ArrayList<>();
		List<TestItem> testItemList = new ArrayList<>();
		TestItem item = new TestItem();
		item.setName("item");
		testItemList.add(item);
		Test test = new Test();
		test.setList(testItemList);
		test.setTest("test");
		testList.add(test);

		log.info("{}", JSON.toJSONString(conversionService.convert(testList, List.class)));
		context.close();
	}
}
