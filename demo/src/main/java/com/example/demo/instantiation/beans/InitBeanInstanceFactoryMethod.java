package com.example.demo.instantiation.beans;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 初始化bean-通过工厂实例
 */
public class InitBeanInstanceFactoryMethod {

    private static final Logger log = LoggerFactory.getLogger(InitStaticNestedBean.class);

    private static Hello hello = new Hello();

    public Hello create() {
        hello.setName("44");
        hello.setAge(44);
        return hello;
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        Hello hello = context.getBean("initHello2", Hello.class);
        log.info("{}", hello == null);
        log.info("{}", hello);
        log.info("{}", context.getType("initHello2"));
        log.info("{}", context.getBean("initHello2", context.getType("initHello2")));
        context.close();
    }
}
