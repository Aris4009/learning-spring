package com.example.demo.annotation.xml.annotation.without.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 如果只有一个构造器，就不需要标记为@Autowired
 */
@Component
public class OnlyOneConstruct {

	private AnnotationDependencyBean annotationDependencyBean;

//	@Autowired 此处注解可以省略
	public OnlyOneConstruct(AnnotationDependencyBean annotationDependencyBean) {
		this.annotationDependencyBean = annotationDependencyBean;
	}

	public AnnotationDependencyBean getAnnotationDependencyBean() {
		return annotationDependencyBean;
	}

	public void setAnnotationDependencyBean(AnnotationDependencyBean annotationDependencyBean) {
		this.annotationDependencyBean = annotationDependencyBean;
	}

	@Override
	public String toString() {
		return "OnlyOneConstruct{" + "annotationDependencyBean=" + annotationDependencyBean + '}';
	}

	private static Logger log = LoggerFactory.getLogger(OnlyOneConstruct.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				"com.example.demo.annotation.xml.annotation.without.annotation");
		OnlyOneConstruct bean = context.getBean("onlyOneConstruct", OnlyOneConstruct.class);
		log.info("{}", bean);
	}
}
