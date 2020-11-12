package com.example.demo.annotation.xml.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * XML混合注解
 */
public class XmlWithAnnotation {

	private static Logger log = LoggerFactory.getLogger(XmlWithAnnotation.class);

	public static void main(String[] args) {
		String path = "classpath:annotation/xml/annotation/bean.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		XmlBean xmlBean = context.getBean("xmlBean", XmlBean.class);
		AnnotationBean annotationBean = context.getBean("annotationBean", AnnotationBean.class);
		log.info("xmlBean:{}", xmlBean);
		log.info("annotationBean:{}", annotationBean);
		context.close();
	}
}
