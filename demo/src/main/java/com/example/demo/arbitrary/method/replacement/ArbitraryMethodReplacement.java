package com.example.demo.arbitrary.method.replacement;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 任意方法替换
 */
public class ArbitraryMethodReplacement {

	public static void main(String[] args) {
		String path = "classpath:replacementMethod.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		OriginObj originObj1 = context.getBean("origin1", OriginObj.class);
		originObj1.old("test");
		context.close();
	}
}
