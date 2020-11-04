package com.example.demo.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarImpl implements ICar {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run() {
		log.info("The car is running");
	}
}
