package com.example.demo.annotation.xml.annotation;

/**
 * XML bean
 */
public class XmlBean {

	private String name;

	public XmlBean(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "XmlBean{" + "name='" + name + '\'' + '}';
	}
}
