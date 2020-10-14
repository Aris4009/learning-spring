package com.example.demo.ioc.container;

import com.example.demo.hello.Hello;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于java的元数据配置
 */
@Configuration
public class JavaBasedConfiguration {

    @Bean
    public Hello helloBean() {
        return new Hello("hello",12);
    }
}
