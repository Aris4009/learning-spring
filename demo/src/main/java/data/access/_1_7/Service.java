package data.access._1_7;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import data.access._1_5_1.Test;

@Component
public class Service {

	private static final String INSERT_USER_SQL = "insert into user(name) values(?)";

	private static final String INSERT_TEST_SQL = "insert into test(name) values(?)";

	private final JdbcTemplate jdbcTemplate;

	private final ApplicationEventPublisher publisher;

	public Service(JdbcTemplate jdbcTemplate, ApplicationEventPublisher publisher) {
		this.jdbcTemplate = jdbcTemplate;
		this.publisher = publisher;
	}

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Transactional
	public void user(User user) {
		User u = insertUser(user);
		Test t = insertTest(new Test(user.getName()));
		this.publisher.publishEvent(new CreateUserEvent(u, t));
	}

	@Transactional(rollbackFor = Exception.class)
	public void userRollback(User user) throws Exception {
		User u = insertUser(user);
		Test t = insertTest(new Test(user.getName()));
		this.publisher.publishEvent(new CreateUserEvent(u, t));
		throw new Exception("抛出异常");
	}

	private User insertUser(User user) {
		PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
				INSERT_USER_SQL, Types.VARCHAR);
		preparedStatementCreatorFactory.setReturnGeneratedKeys(true);
		PreparedStatementCreator preparedStatementCreator = preparedStatementCreatorFactory
				.newPreparedStatementCreator(new Object[] { user.getName() });
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int code = jdbcTemplate.update(preparedStatementCreator, keyHolder);
		User u = null;
		if (code > 0 && keyHolder.getKey() != null) {
			u = new User(user.getName());
			u.setId(keyHolder.getKey().intValue());
		}
		return u;
	}

	private Test insertTest(Test test) {
		PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
				INSERT_TEST_SQL, Types.VARCHAR);
		preparedStatementCreatorFactory.setReturnGeneratedKeys(true);
		PreparedStatementCreator preparedStatementCreator = preparedStatementCreatorFactory
				.newPreparedStatementCreator(new Object[] { test.getName() });
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int code = jdbcTemplate.update(preparedStatementCreator, keyHolder);
		Test t = null;
		if (code > 0 && keyHolder.getKey() != null) {
			t = new Test(test.getName());
			t.setId(keyHolder.getKey().intValue());
		}
		return t;
	}
}
