package com.example.demo.dependencies.and.configuration.in.detail;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 集合合并
 */
public abstract class MergingCollection {

	private Properties adminEmails;

	public Properties getAdminEmails() {
		return adminEmails;
	}

	public void setAdminEmails(Properties adminEmails) {
		this.adminEmails = adminEmails;
	}

	private static final Logger log = LoggerFactory.getLogger(MergingCollection.class);

	public static void main(String[] args) {
		String path = "classpath:mergingCollection.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		MergingCollection child = context.getBean("child", MergingCollection.class);
		log.info("child:{}", child.adminEmails);
		context.close();
	}
}
