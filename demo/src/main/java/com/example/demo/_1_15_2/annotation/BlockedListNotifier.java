package com.example.demo._1_15_2.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BlockedListNotifier {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@EventListener
	public void custom(BlockedListEvent event) {
		// notify appropriate parties via notificationAddress...
		log.info("处理事件：{}-{}-{}-{}", event.getSource(), event.getTimestamp(), event.getAddress(), event.getContent());
	}
}
