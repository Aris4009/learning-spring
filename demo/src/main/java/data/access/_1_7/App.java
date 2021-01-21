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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan
@EnableTransactionManagement
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	@Bean
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/test?useSSL=false");
		dataSource.setUsername("root");
		return dataSource;
	}

	@Bean
	public JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean("tm")
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public TransactionTemplate transactionTemplate() {
		return new TransactionTemplate(transactionManager());
	}

	public static void main(String[] args) throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(App.class);
		context.refresh();

		Service service = context.getBean(Service.class);
		int code = new Random().nextInt(Integer.MAX_VALUE);
		User user = new User("我是" + String.valueOf(code));
		log.info("{}", user);
		service.user(user);
		log.info("================");
		service.userRollback(user);
	}
}
