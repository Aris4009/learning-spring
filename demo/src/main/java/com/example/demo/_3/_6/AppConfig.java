package com.example.demo._3._6;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

@Configurable
public class AppConfig {

	public FormattingConversionService conversionService() {
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);
		conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

		DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
		registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
		registrar.registerFormatters(conversionService);

		DateFormatterRegistrar dateFormatterRegistrar = new DateFormatterRegistrar();
		dateFormatterRegistrar.setFormatter(new DateFormatter("yyyyMMdd"));
		dateFormatterRegistrar.registerFormatters(conversionService);
		return conversionService;
	}
}
