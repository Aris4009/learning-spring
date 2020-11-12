package com.example.demo.annotation.xml.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注解bean
 */
@Component
public class AnnotationBean {

	private XmlBean xmlBean;

	@Autowired
	public AnnotationBean(XmlBean xmlBean) {
		this.xmlBean = xmlBean;
	}

	public XmlBean getXmlBean() {
		return xmlBean;
	}

	public void setXmlBean(XmlBean xmlBean) {
		this.xmlBean = xmlBean;
	}

	@Override
	public String toString() {
		return "AnnotationBean{" + "xmlBean=" + xmlBean + '}';
	}
}
