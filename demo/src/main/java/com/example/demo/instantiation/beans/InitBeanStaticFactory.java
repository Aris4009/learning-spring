package com.example.demo.instantiation.beans;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 初始化bean使用静态工厂方法
 */
public class InitBeanStaticFactory {

    private static final Logger log = LoggerFactory.getLogger(InitStaticNestedBean.class);

    private static Hello hello = new Hello();

    private InitBeanStaticFactory() {
    }

    public static Hello create() {
        hello.setAge(3);
        hello.setName("3");
        return hello;
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        Hello hello = context.getBean("initHello", Hello.class);
        log.info("{}", hello == null);
        log.info("{}", hello);
        context.close();
    }
}
