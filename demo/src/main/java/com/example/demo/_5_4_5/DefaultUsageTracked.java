package com.example.demo._5_4_5;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUsageTracked implements UsageTracked {

	private final AtomicInteger counter;

	private static final Logger log = LoggerFactory.getLogger(DefaultUsageTracked.class);

	public DefaultUsageTracked() {
		this.counter = new AtomicInteger();
	}

	@Override
	public int incrementUseCount() {
		return counter.incrementAndGet();
	}

	public static void main(String[] args) {
		UsageTracked usageTracked = new DefaultUsageTracked();
		int a = usageTracked.incrementUseCount();
		int b = usageTracked.incrementUseCount();
		log.info("{},{}", a, b);
	}
}
