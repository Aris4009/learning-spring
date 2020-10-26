package com.example.demo.dependencies.and.configuration.in.detail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 依赖注入的细节
 * 直接字面值
 */
public class StraightValues {

    private static final Logger log = LoggerFactory.getLogger(StraightValues.class);

    public static void main(String[] args) {
        try {
            String path = "classpath:straightValues.xml";
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
            DataSource dataSource = context.getBean("myDatasource2", DataSource.class);
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select * from test");
            List<String[]> list = new ArrayList<>();
            while (rs.next()) {
                String[] data = new String[2];
                data[0] = rs.getString(1);
                data[1] = rs.getString(2);
                list.add(data);
            }
            rs.close();
            statement.close();
            connection.close();
            context.close();
            list.forEach(d -> {
                log.info("key:{}->value:{}", d[0], d[1]);
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
