package dao.support._3_1;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@ComponentScan
public class App {
	private static final Logger log = LoggerFactory.getLogger(data.access._1_7.App.class);

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

	@Bean
	public SimpleJdbcCall simpleJdbcCall() {
		return new SimpleJdbcCall(jdbcTemplate());
	}

	@Bean
	public SimpleJdbcInsert simpleJdbcInsert() {
		return new SimpleJdbcInsert(jdbcTemplate());
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
		return new NamedParameterJdbcTemplate(jdbcTemplate());
	}

	@Bean
	public JdbcDaoSupport jdbcDaoSupport() {
		JdbcDaoSupport jdbcDaoSupport = new JdbcDaoSupport() {
		};
		jdbcDaoSupport.setJdbcTemplate(jdbcTemplate());
		return jdbcDaoSupport;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(App.class);
		context.refresh();
//		UserService userService = context.getBean(UserService.class);
//		userService.execute(new User(70));

//		JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
//		JdbcDaoSupport jdbcDaoSupport = context.getBean(JdbcDaoSupport.class);
//		log.info("{}", jdbcTemplate == jdbcDaoSupport.getJdbcTemplate());

		NamedUserRepository namedUserRepository = context.getBean(NamedUserRepository.class);
		log.info("{}", namedUserRepository.countByUserName(new User("haha")));
		context.close();
	}
}
