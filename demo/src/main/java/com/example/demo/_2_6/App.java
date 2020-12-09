package com.example.demo._2_6;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		String path = "classpath:_2_6/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		MyBean myBean = context.getBean("myBean", MyBean.class);
		String a = read(myBean.getResource());
		log.info("{}", a);
		context.close();
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
