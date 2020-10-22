package com.example.demo.instantiation.beans;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 初始化静态内部类
 * 静态内部类的class属性需要使用$字符来分割，例如：
 * <bean id="initStaticNestedBean" class="com.example.demo.instantiation.beans.InitStaticNestedBean$NestedClassBean">
 *
 */
public class InitStaticNestedBean {

    private static final Logger log = LoggerFactory.getLogger(InitStaticNestedBean.class);

    public static class NestedClassBean{

        public NestedClassBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        private String name;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        InitStaticNestedBean.NestedClassBean nestedClassBean1 = context.getBean("initStaticNestedBean",InitStaticNestedBean.NestedClassBean.class);
        log.info("name:{},age:{}", nestedClassBean1.getName(),nestedClassBean1.getAge());

        InitStaticNestedBean.NestedClassBean nestedClassBean2 = context.getBean("name1",InitStaticNestedBean.NestedClassBean.class);
        log.info("name:{},age:{}", nestedClassBean2.getName(),nestedClassBean2.getAge());

        log.info("{}",nestedClassBean1 == nestedClassBean2);

        Hello hello = context.getBean("hello2",Hello.class);
        log.info("{}",hello);
        context.close();
    }
}
