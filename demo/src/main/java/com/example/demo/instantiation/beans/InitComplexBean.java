package com.example.demo.instantiation.beans;

import com.example.demo.hello.Hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 初始化复杂的Bean，属性包含List、Map、Set等
 */
public class InitComplexBean {

    private Hello hello;

    private static final Logger log = LoggerFactory.getLogger(InitStaticNestedBean.class);

    private List<String> list;

    private Map<String, String> map;

    private Set<Integer> set;

    public Hello getHello() {
        return hello;
    }

    public void setHello(Hello hello) {
        this.hello = hello;
    }

    public static Logger getLog() {
        return log;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Set<Integer> getSet() {
        return set;
    }

    public void setSet(Set<Integer> set) {
        this.set = set;
    }

    public InitComplexBean(Hello hello, List<String> list, Map<String, String> map, Set<Integer> set) {
        this.hello = hello;
        this.list = list;
        this.map = map;
        this.set = set;
    }

    @Override
    public String toString() {
        return "InitComplexBean{" +
                "hello=" + hello +
                ", list=" + list +
                ", map=" + map +
                ", set=" + set +
                '}';
    }

    public static void main(String[] args) {
        String path = "classpath:beans.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
        InitComplexBean bean = context.getBean("initComplexBean", InitComplexBean.class);
        log.info("{}", bean);
        context.close();
    }
}
