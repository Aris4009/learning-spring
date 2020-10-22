package com.example.demo.dependency.injection.constructor.based;

import com.example.demo.instantiation.beans.InitStaticNestedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 基于构造函数的依赖注入
 * 不指定构造参数的顺序和类型
 */
public class BeanOne {

    private static final Logger log = LoggerFactory.getLogger(InitStaticNestedBean.class);

    private BeanTwo beanTwo;

    private BeanThree beanThree;

    public BeanOne(BeanTwo beanTwo, BeanThree beanThree) {
        this.beanTwo = beanTwo;
        this.beanThree = beanThree;
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        BeanOne one = context.getBean("beanOne", BeanOne.class);
        log.info("{}", one.hashCode());
        context.close();
    }
}
