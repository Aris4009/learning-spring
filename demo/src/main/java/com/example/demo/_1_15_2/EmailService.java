package com.example.demo._1_15_2;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public class EmailService implements ApplicationEventPublisherAware {

	private ApplicationEventPublisher applicationEventPublisher;

	private List<String> blockedList;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void setBlockedList(List<String> blockedList) {
		this.blockedList = blockedList;
	}

	public void sendEmail(String address, String content) {
		if (blockedList.contains(address)) {
			log.info("发布事件的地址:{}", address);
			log.info("start sleep...");
			try {
				Thread.sleep(3000L);
				applicationEventPublisher.publishEvent(new BlockedListEvent(this, address, content));
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
			log.info("stop sleep...");
		} else {
			log.info("不发布事件的地址:{}", address);
		}
		// send email...
	}
}
