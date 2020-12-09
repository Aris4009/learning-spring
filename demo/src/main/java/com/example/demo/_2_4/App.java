package com.example.demo._2_4;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		ApplicationContext context = new ClassPathXmlApplicationContext();
		Resource applicationPropertiesResource = context.getResource("format.properties");
		String a1 = read(applicationPropertiesResource);
		log.info("{}", a1);
		Resource beansXmlResource = context.getResource("classpath:beans.xml");
		String a2 = read(beansXmlResource);
		log.info("{}", a2);

//		Resource appClassResource = context.getResource("file://App.class");
//		String a3 = read(appClassResource);
//		log.info("{}", a3);

		Resource template = context.getResource("https://www.baidu.com");
		String a4 = read(template);
		log.info("{}", a4);
	}

	public static String read(Resource resource) throws IOException {
		InputStream in = resource.getInputStream();
		BufferedInputStream inputStream = new BufferedInputStream(in);
		int n;
		byte[] bytes = new byte[8192];
		StringBuilder buffer1 = new StringBuilder();
		while ((n = inputStream.read(bytes)) != -1) {
			buffer1.append(new String(bytes, 0, n));
		}
		inputStream.close();
		in.close();
		return buffer1.toString();
	}
}
