package com.example.demo.autowired.array;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutowiredArray {

	private List<String> nameList;

	private Set<Integer> integerSet;

	private String[] strArr;

	@Override
	public String toString() {
		return "AutowiredArray{" + "nameList=" + nameList + ", integerSet=" + integerSet + ", strArr="
				+ Arrays.toString(strArr) + '}';
	}

	public List<String> getNameList() {
		return nameList;
	}

	@Autowired
	public void setNameList(List<String> nameList) {
		this.nameList = nameList;
	}

	public Set<Integer> getIntegerSet() {
		return integerSet;
	}

	@Autowired
	public void setIntegerSet(Set<Integer> integerSet) {
		this.integerSet = integerSet;
	}

	public String[] getStrArr() {
		return strArr;
	}

	@Autowired
	public void setStrArr(String[] strArr) {
		this.strArr = strArr;
	}
}
