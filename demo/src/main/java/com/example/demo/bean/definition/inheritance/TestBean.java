package com.example.demo.bean.definition.inheritance;

/**
 * 父定义可以在类声明时不定义为abstract类，仅需要在配置文件中指定abstract属性即可。
 */
public class TestBean {

	private String name;

	private String age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "TestBean{" + "name='" + name + '\'' + ", age='" + age + '\'' + '}';
	}
}
