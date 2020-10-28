package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 复合嵌套属性名，只要全路径上的属性都不为null，除了最终属性外，就可以使用这种方式
 */
public class CompoundPropertyNames {

	private Fred fred;

	public Fred getFred() {
		return fred;
	}

	public void setFred(Fred fred) {
		this.fred = fred;
	}

	private static final Logger log = LoggerFactory.getLogger(CompoundPropertyNames.class);

	public static CompoundPropertyNames init() {
		log.info("init");
		CompoundPropertyNames compoundPropertyNames = new CompoundPropertyNames();
		compoundPropertyNames.setFred(new Fred());
		compoundPropertyNames.getFred().setBob(new Bob());
		return compoundPropertyNames;
	}

	public static void main(String[] args) {
		String path = "classpath:compoundPropertyNames.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		CompoundPropertyNames bean = context.getBean("bean", CompoundPropertyNames.class);
		log.info("{}", bean.getFred().getBob().getSammy());
		context.close();
	}
}
