package com.example.demo._1_15_1;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

public class ExampleMessageSourceAware implements MessageSourceAware {

	private MessageSource messageSource;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void execute() {
		String message = messageSource.getMessage("message", null, "Default", Locale.ENGLISH);
		log.info("{}-----------", message);
	}
}
