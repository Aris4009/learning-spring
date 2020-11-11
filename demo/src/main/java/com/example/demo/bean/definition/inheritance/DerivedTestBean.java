package com.example.demo.bean.definition.inheritance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 注意配置文件中，parent属性
 */
public class DerivedTestBean extends TestBean {

	private static Logger log = LoggerFactory.getLogger(DerivedTestBean.class);

	public void initMethod() {
		log.info("name:{},age:{}", this.getName(), this.getAge());
	}

	public static void main(String[] args) {
		String path = "classpath:bean/definition/inheritance/beanDefinitionInheritance.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		// 被标记为abstract的bean定义不能被引用，下面会出错。但是，如果abstract是false，并且TestBean不是abstract，那么可以获取该bean定义
		// 父bean可以不被定义为abstract类，只需要在bean的定义中指定abstract的属性即可。
//		log.info("parent:{}", context.getBean("testBean", TestBean.class));
		context.close();
	}
}
