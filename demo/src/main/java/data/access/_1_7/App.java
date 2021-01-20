package data.access._1_7;

import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;

@Configuration
@ComponentScan
public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);

	@Bean
	public static DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/test?useSSL=false");
		dataSource.setUsername("root");
		return dataSource;
	}

	@Bean
	public static JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean("txManager")
	public static TransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(App.class);
		context.refresh();

		CreateUserPublisher createUserPublisher = context.getBean(CreateUserPublisher.class);
		int code = new Random().nextInt(Integer.MAX_VALUE);
		User user = new User("我是" + String.valueOf(code));
		log.info("{}", user);
		createUserPublisher.publish(user);
	}
}
