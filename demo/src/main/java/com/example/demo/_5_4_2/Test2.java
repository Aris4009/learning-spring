package com.example.demo._5_4_2;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

public class Test2 {

	static Class<?> javaxInjectProviderClass;

	static {
		try {
			System.out.println("------------");
			javaxInjectProviderClass = ClassUtils.forName("javax.inject.Provider", Test2.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			// JSR-330 API not available - Provider interface simply not supported then.
			javaxInjectProviderClass = null;
		}
	}

	public static void main(String[] args) {
		System.out.println(ObjectUtils.identityToString(Test.class));
	}
}
