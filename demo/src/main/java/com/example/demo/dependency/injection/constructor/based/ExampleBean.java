package com.example.demo.dependency.injection.constructor.based;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 构造参数注入-通过制定类型来匹配构造参数
 */
public class ExampleBean {

    private static final Logger log = LoggerFactory.getLogger(ExampleBean.class);

    private int years;

    private String ultimateAnswer;

    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }

    @Override
    public String toString() {
        return "ExampleBean{" +
                "years=" + years +
                ", ultimateAnswer='" + ultimateAnswer + '\'' +
                '}';
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        ExampleBean exampleBean = context.getBean("exampleBean", ExampleBean.class);
        log.info("{}", exampleBean);

        ExampleBean exampleBean1 = context.getBean("exampleBean1", ExampleBean.class);
        log.info("{}", exampleBean1);

        ExampleBean exampleBean2 = context.getBean("exampleBean2", ExampleBean.class);
        log.info("{}", exampleBean2);

        log.info("bean1 hashcode:{},bean2 hashcode:{},bean3 hashcode:{}", exampleBean.hashCode(), exampleBean1.hashCode(), exampleBean2.hashCode());
        context.close();
    }
}
