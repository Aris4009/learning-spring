package com.example.demo._1_15_3;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ResourceTest {

	private static Logger log = LoggerFactory.getLogger(ResourceTest.class);

	public static void main(String[] args) throws IOException {
		String path = "classpath:annotation/xml/annotation/bean.xml";
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		Resource resource = context.getResource("classpath:beans.xml");
		InputStream inputStream = resource.getInputStream();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		byte[] bytes = new byte[4096];
		int n = 0;
		StringBuilder builder = new StringBuilder();
		while ((n = bufferedInputStream.read(bytes)) != -1) {
			String s = new String(bytes, 0, n);
			builder.append(s);
		}
		log.info("{}", builder);
		inputStream.close();
	}
}
