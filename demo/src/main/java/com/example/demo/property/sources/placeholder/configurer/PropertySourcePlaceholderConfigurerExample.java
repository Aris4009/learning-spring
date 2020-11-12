package com.example.demo.property.sources.placeholder.configurer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 加载配置文件例子
 */
public class PropertySourcePlaceholderConfigurerExample {

	private String p1;

	private String p2;

	private String p3;

	private String p4;

	public String getP1() {
		return p1;
	}

	public void setP1(String p1) {
		this.p1 = p1;
	}

	public String getP2() {
		return p2;
	}

	public void setP2(String p2) {
		this.p2 = p2;
	}

	public String getP3() {
		return p3;
	}

	public void setP3(String p3) {
		this.p3 = p3;
	}

	public String getP4() {
		return p4;
	}

	public void setP4(String p4) {
		this.p4 = p4;
	}

	@Override
	public String toString() {
		return "PropertySourcePlaceholderConfigurerExample{" + "p1='" + p1 + '\'' + ", p2='" + p2 + '\'' + ", p3='" + p3
				+ '\'' + ", p4='" + p4 + '\'' + '}';
	}

	private static Logger log = LoggerFactory.getLogger(PropertySourcePlaceholderConfigurerExample.class);

	public static void main(String[] args) {
		String path = "classpath:property/sources/placeholder/configurer/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		PropertySourcePlaceholderConfigurerExample bean = context.getBean("bean",
				PropertySourcePlaceholderConfigurerExample.class);
		log.info("{}", bean);
		context.close();
	}
}
