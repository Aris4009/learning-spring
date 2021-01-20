package data.access._1_5_1;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class ServiceImpl implements IService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final JdbcTemplate jdbcTemplate;

	private final TransactionTemplate transactionTemplate;

	private User user;

	private Test test;

	public ServiceImpl(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	@Override
	public User insertUser(User user) {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into user(name) values(?)");
		PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
				builder.toString(), Types.VARCHAR);
		preparedStatementCreatorFactory.setReturnGeneratedKeys(true);
		PreparedStatementCreator preparedStatementCreator = preparedStatementCreatorFactory
				.newPreparedStatementCreator(new Object[] { user.getName() });
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int code = this.jdbcTemplate.update(preparedStatementCreator, keyHolder);
		User u = null;
		if (code > 0) {
			u = new User(keyHolder.getKey().intValue(), user.getName());
		}
		log.info("sql:{},user:{}", builder.toString(), u);
		return u;
	}

	@Override
	public Test insertTest(Test test) {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into test(name) values(?)");
		PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
				builder.toString(), Types.VARCHAR);
		preparedStatementCreatorFactory.setReturnGeneratedKeys(true);
		PreparedStatementCreator preparedStatementCreator = preparedStatementCreatorFactory
				.newPreparedStatementCreator(new Object[] { test.getName() });
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int code = this.jdbcTemplate.update(preparedStatementCreator, keyHolder);
		Test t = null;
		if (code > 0) {
			t = new Test(keyHolder.getKey().intValue(), test.getName());
		}
		log.info("sql:{},test:{}", builder.toString(), test);
		return t;
	}

	@Override
	public void deleteUser(User user) {
		StringBuilder builder = new StringBuilder();
		builder.append("delete from user where id = ?");
		this.jdbcTemplate.update(builder.toString(), new Object[] { user.getId() });
		log.info("sql:{}", builder.toString());
	}

	@Override
	public void deleteTest(Test test) {
		StringBuilder builder = new StringBuilder();
		builder.append("delete from test where id = ?");
		this.jdbcTemplate.update(builder.toString(), new Object[] { test.getId() });
		log.info("sql:{}", builder.toString());
	}

	@Override
	public void insert() {
		this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
					insertUser(user);
					insertTest(test);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					status.setRollbackOnly();
				}
			}
		});
	}
}
