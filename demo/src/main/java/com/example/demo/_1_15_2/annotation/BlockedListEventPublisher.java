package com.example.demo._1_15_2.annotation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class BlockedListEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;

	private final List<String> addressList;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public BlockedListEventPublisher(ApplicationEventPublisher applicationEventPublisher, List<String> addressList) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.addressList = addressList;
	}

	public void publish(String address) {
		if (addressList.contains(address)) {
			log.info("publish:{}", address);
			applicationEventPublisher.publishEvent(new BlockedListEvent(this, address, address));
		} else {
			log.info("not publish:{}", address);
		}
	}
}
