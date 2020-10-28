package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * idref标签使用
 */
public class IdRef {

    private IdRefTarget idRefTargetName;

    public IdRefTarget getIdRefTargetName() {
        return idRefTargetName;
    }

    public void setIdRefTargetName(IdRefTarget idRefTargetName) {
        this.idRefTargetName = idRefTargetName;
    }

    private static final Logger log = LoggerFactory.getLogger(IdRef.class);

    public static void main(String[] args) {
        String path = "classpath:idref.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        IdRef idRef = context.getBean("idRef", IdRef.class);
        IdRef idRef1 = context.getBean("idRef1", IdRef.class);
        log.info("{},{}", idRef.hashCode(), idRef.getIdRefTargetName().getName());
        log.info("{},{},{}", idRef1.hashCode(), idRef1.getIdRefTargetName().getClass(), idRef1.getIdRefTargetName().getName());
        context.close();
    }
}
