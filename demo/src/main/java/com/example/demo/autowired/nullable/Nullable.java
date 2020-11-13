package com.example.demo.autowired.nullable;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Nullable {

	private List<String> nameList;

	private Set<Integer> integerSet;

	public List<String> getNameList() {
		return nameList;
	}

	public void setNameList(List<String> nameList) {
		this.nameList = nameList;
	}

	public Set<Integer> getIntegerSet() {
		return integerSet;
	}

	public void setIntegerSet(Set<Integer> integerSet) {
		this.integerSet = integerSet;
	}

//	@Autowired
//	public Nullable(@org.springframework.lang.Nullable List<String> nameList,
//			@org.springframework.lang.Nullable Set<Integer> integerSet) {
//		this.nameList = nameList;
//		this.integerSet = integerSet;
//	}

//	@Autowired
//	public Nullable(List<String> nameList, Set<Integer> integerSet) {
//		this.nameList = nameList;
//		this.integerSet = integerSet;
//	}

	@Override
	public String toString() {
		return "Nullable{" + "nameList=" + nameList + ", integerSet=" + integerSet + '}';
	}

	private static Logger log = LoggerFactory.getLogger(Nullable.class);

	public static void main(String[] args) {
//		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ArrayConfigure.class,
//				Nullable.class);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Nullable.class);
		log.info("{}", context.getBean("nullable", Nullable.class));
		context.close();
	}
}
