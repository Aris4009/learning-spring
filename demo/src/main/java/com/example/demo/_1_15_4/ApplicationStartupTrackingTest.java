package com.example.demo._1_15_4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.metrics.StartupStep;

@Configuration
public class ApplicationStartupTrackingTest {

	@Bean
	public List<String> testList() {
		List<String> list = new ArrayList<>();
		list.add("1");
		return list;
	}

	private static Logger log = LoggerFactory.getLogger(ApplicationStartupTrackingTest.class);

	public static void main(String[] args) {
		// JDK 11以后才可使用
//		FlightRecorderApplicationStartup flightRecorderApplicationStartup = new FlightRecorderApplicationStartup();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
//		context.setApplicationStartup(flightRecorderApplicationStartup);
		// 创建一个StartupStep并开始记录
		StartupStep startupStep = context.getApplicationStartup().start("spring.context.base-packages.scan");
		// 为当前阶段增加tag
		String[] basePackages = { "com.example.demo._1_15_4" };
		startupStep.tag("packages", () -> Arrays.toString(basePackages));
		// 执行正在检测的实际阶段
		context.scan(basePackages);
		// 结束当前阶段
		startupStep.end();
		context.close();
	}
}
