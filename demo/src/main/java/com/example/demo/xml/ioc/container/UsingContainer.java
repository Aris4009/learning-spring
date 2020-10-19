package com.example.demo.xml.ioc.container;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 使用容器
 */
public class UsingContainer {
    public static final Logger log = LoggerFactory.getLogger(UsingContainer.class);

    public static void main(String[] args) {
        String path = "classpath:multiple.xml";
        GenericApplicationContext context = new GenericApplicationContext();
        new XmlBeanDefinitionReader(context).loadBeanDefinitions(path);
        context.refresh();
        Hello hello = context.getBean("hello",Hello.class);
        log.info("{}",hello);
        context.close();
    }
}
