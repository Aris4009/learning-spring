package com.example.demo.xml.ioc.container;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * xml方式初始化bean
 */
public class InitBeans {

    public static final Logger log = LoggerFactory.getLogger(InitBeans.class);

    public static void main(String[] args) {
        try {
            String path = "classpath*:beans.xml";
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
            Hello hello = context.getBean(Hello.class);
            log.info("init hello bean {}",hello);
            context.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
