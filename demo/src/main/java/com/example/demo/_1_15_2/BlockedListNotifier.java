package com.example.demo._1_15_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

public class BlockedListNotifier implements ApplicationListener<BlockedListEvent> {

	private String notificationAddress;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public void setNotificationAddress(String notificationAddress) {
		this.notificationAddress = notificationAddress;
	}

	@Override
	public void onApplicationEvent(BlockedListEvent event) {
		// notify appropriate parties via notificationAddress...
		log.info("处理事件：{}-{}-{}-{}", event.getSource(), event.getTimestamp(), event.getAddress(), event.getContent());
	}
}
