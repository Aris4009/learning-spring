package web.mvc._1_3_1.aop.service;

import java.sql.Types;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import data.access._1_5_1.Test;
import data.access._1_7.User;

@org.springframework.stereotype.Service
public class Service {

	private static final String INSERT_USER_SQL = "insert into user(name) values(?)";

	private static final String INSERT_TEST_SQL = "insert into test(name) values(?)";

	private JdbcTemplate jdbcTemplate;

	public Service(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public User insertUser(User user) {
		PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
				INSERT_USER_SQL, Types.VARCHAR);
		preparedStatementCreatorFactory.setReturnGeneratedKeys(true);
		PreparedStatementCreator preparedStatementCreator = preparedStatementCreatorFactory
				.newPreparedStatementCreator(new Object[] { user.getName() });
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int code = jdbcTemplate.update(preparedStatementCreator, keyHolder);
		User u = null;
		if (code > 0 && keyHolder.getKey() != null) {
			// 此处故意将user设置为null，触发异常，来查看事务是否生效
//			u = new User(user.getName());
			u.setId(keyHolder.getKey().intValue());
		}
		return u;
	}

	public Test insertTest(Test test) {
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
