package com.example.demo.xml.ioc.container;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 初始化导入的配置文件
 */
public class InitMultipleImportBeans {
    public static final Logger log = LoggerFactory.getLogger(InitMultipleImportBeans.class);

    public static void main(String[] args) {
        try {
            String path = "classpath*:multiple.xml";
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
            Hello hello = context.getBean(Hello.class);
            log.info("init hello bean {}",hello);
            context.close();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }
}
