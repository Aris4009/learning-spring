package com.example.demo.deprecated.required;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Required注解在Spring 5.1已经被废弃，使用构造函数或者InitializingBean.afterPropertiesSet()的自定义实现来代替
 */
public class DeprecatedRequiredBean {

	private RequiredBean requiredBean;

	public RequiredBean getRequiredBean() {
		return requiredBean;
	}

	@Required
	public void setRequiredBean(RequiredBean requiredBean) {
		this.requiredBean = requiredBean;
	}

//	@Autowired
//	public DeprecatedRequiredBean(RequiredBean requiredBean) {
//		this.requiredBean = requiredBean;
//	}

	@Override
	public String toString() {
		return "DeprecatedRequiredBean{" + "requiredBean=" + requiredBean + '}';
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				DeprecatedRequiredBean.class);
		context.close();
	}
}
