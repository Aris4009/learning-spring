package com.example.demo._3._3_4;

import org.springframework.core.convert.converter.Converter;

/*
    这个类在当前版本中已经找不到了，可能在很久之前的版本中已经被移除
 */
public class StringToInteger implements Converter<String, Integer> {

	@Override
	public Integer convert(String source) {
		return Integer.valueOf(source);
	}
}
