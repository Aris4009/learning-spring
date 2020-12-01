package com.example.demo._1_15_1;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

public class Example {

	private MessageSource messageSource;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void execute() {
		String message = this.messageSource.getMessage("argument.required", new Object[] { "userDao" }, "Required",
				Locale.ENGLISH);
		log.info("exceptions:{}", message);
	}
}
