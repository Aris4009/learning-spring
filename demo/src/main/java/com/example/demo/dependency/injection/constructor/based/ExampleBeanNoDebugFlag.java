package com.example.demo.dependency.injection.constructor.based;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.beans.ConstructorProperties;

/**
 * 不开启debug flag的情况下，表明参数的名称
 */
public class ExampleBeanNoDebugFlag {

    private static final Logger log = LoggerFactory.getLogger(ExampleBeanNoDebugFlag.class);

    private int years;

    private String ultimateAnswer;

    @ConstructorProperties({"y", "u"})
    public ExampleBeanNoDebugFlag(int years, String ultimateAnswer) {
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
        ExampleBeanNoDebugFlag exampleBeanNoDebugFlag = context.getBean("exampleBeanNoDebugFlag", ExampleBeanNoDebugFlag.class);
        log.info("{}", exampleBeanNoDebugFlag);
        context.close();
    }

}
