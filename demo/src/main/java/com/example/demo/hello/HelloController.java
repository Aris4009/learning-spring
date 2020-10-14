package com.example.demo.hello;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Hello hello;

    @PostMapping("/test")
    public Hello hello(){
//        Hello hello = new Hello();
//        hello.setAge(1);
//        hello.setName("test");
        try {
            logger.info(new ObjectMapper().writeValueAsString(hello));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return hello;
    }
}
