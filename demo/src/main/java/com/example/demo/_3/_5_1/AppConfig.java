package com.example.demo._3._5_1;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.Formatter;

import com.example.demo.gson.JSON;

public class AppConfig {

	private static Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		try {
			String a = "Tom,21";
			Formatter<User> formatter = formatter();
			Locale locale = new Locale("zh");
			User user = formatter.parse(a, locale);
			formatter.print(user, locale);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static Formatter<User> formatter() {
		return new Formatter<User>() {

			@Override
			public User parse(String text, Locale locale) throws ParseException {
				ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
				factoryBean.afterPropertiesSet();
				ConversionService conversionService = factoryBean.getObject();
				List<String> list = conversionService.convert(text, List.class);
				Constructor<User> constructor = null;
				try {
					constructor = User.class.getConstructor(String.class, int.class);
					User test = constructor.newInstance("aaa", 22);
					log.info("{}", test);
				} catch (Exception e) {
					throw new ParseException(e.getMessage(), 0);
				}
				User user = BeanUtils.instantiateClass(constructor, "Kim", 12);
				log.info("{}", user);
				BeanWrapper beanWrapper = new BeanWrapperImpl(user);
				beanWrapper.setPropertyValue("name", list.get(0));
				beanWrapper.setPropertyValue("age", list.get(1));
				return (User) beanWrapper.getWrappedInstance();
			}

			@Override
			public String print(User object, Locale locale) {
				log.info("{}", object);
				return JSON.toJSONString(object);
			}
		};
	}
}
