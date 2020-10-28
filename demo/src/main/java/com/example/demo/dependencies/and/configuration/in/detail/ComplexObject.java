package com.example.demo.dependencies.and.configuration.in.detail;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 复杂的bean
 */
public class ComplexObject {

	private Properties adminEmails;

	private List<Object> someList;

	private Map<String, Object> someMap;

	private Set<Object> someSet;

	public Properties getAdminEmails() {
		return adminEmails;
	}

	public void setAdminEmails(Properties adminEmails) {
		this.adminEmails = adminEmails;
	}

	public List<Object> getSomeList() {
		return someList;
	}

	public void setSomeList(List<Object> someList) {
		this.someList = someList;
	}

	public Map<String, Object> getSomeMap() {
		return someMap;
	}

	public void setSomeMap(Map<String, Object> someMap) {
		this.someMap = someMap;
	}

	public Set<Object> getSomeSet() {
		return someSet;
	}

	public void setSomeSet(Set<Object> someSet) {
		this.someSet = someSet;
	}

	private static final Logger log = LoggerFactory.getLogger(ComplexObject.class);

	public static void main(String[] args) {
		String path = "classpath:complexObject.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		ComplexObject complexObject = context.getBean("moreComplexObject", ComplexObject.class);
		log.info("properties:{},list:{},map:{},set:{}", complexObject.adminEmails, complexObject.someList,
				complexObject.someMap, complexObject.someSet);
		context.close();
	}
}
