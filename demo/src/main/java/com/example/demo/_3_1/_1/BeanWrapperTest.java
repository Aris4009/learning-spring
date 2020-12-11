package com.example.demo._3_1._1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyValue;

import com.example.demo.gson.JSON;

public class BeanWrapperTest {

	private static Logger log = LoggerFactory.getLogger(BeanWrapperTest.class);

	public static void main(String[] args) {
		Object object = new Company();
		BeanWrapper beanWrapper = new BeanWrapperImpl(object);
		beanWrapper.setPropertyValue("name", "Some Company Inc.");
		log.info("{}", JSON.toJSONString(object));

		Employee employee = new Employee();
		employee.setName("a1");
		employee.setSalary(89f);
		PropertyValue value = new PropertyValue("managingDirector", employee);
		beanWrapper.setPropertyValue(value);
		log.info("{}", JSON.toJSONString(object));

		BeanWrapper beanWrapper1 = new BeanWrapperImpl(new Employee());
		beanWrapper1.setPropertyValue("name", "a2");
		beanWrapper1.setPropertyValue("salary", 22.334f);
		beanWrapper.setPropertyValue("managingDirector", beanWrapper1.getWrappedInstance());

		Float salary = (Float) beanWrapper.getPropertyValue("managingDirector.salary");
		log.info("{}", salary);
	}
}
