package com.example.demo.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class Client {

	public static void main(String[] args) {
		ICar car = new CarImpl();
		InvocationHandler handler = new MyInvocationHandler(car);
		ICar proxy = (ICar) Proxy.newProxyInstance(handler.getClass().getClassLoader(), car.getClass().getInterfaces(),
				handler);
		proxy.run();
	}
}
