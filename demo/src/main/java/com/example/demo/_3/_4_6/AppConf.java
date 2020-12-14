package com.example.demo._3._4_6;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

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

		List<Integer> integerList = new ArrayList<>();
		integerList.add(1);
		integerList.add(2);

		List<String> stringList = (List<String>) conversionService.convert(integerList,
				TypeDescriptor.forObject(integerList),
				TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
		log.info("{}", JSON.toJSONString(stringList));
		context.close();
	}
}
