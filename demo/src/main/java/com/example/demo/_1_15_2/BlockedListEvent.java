package com.example.demo._1_15_2;

import org.springframework.context.ApplicationEvent;

public class BlockedListEvent extends ApplicationEvent {

	private final String address;

	private final String content;

	public BlockedListEvent(Object source, String address, String content) {
		super(source);
		this.address = address;
		this.content = content;
	}

	public String getAddress() {
		return address;
	}

	public String getContent() {
		return content;
	}
}
