package data.access._1_4_2;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.demo.gson.JSON;

public class DefaultFooService implements FooService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private JdbcTemplate jdbcTemplate;

	public DefaultFooService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Foo getFoo(String fooName) {
		StringBuilder builder = new StringBuilder();
		builder.append("select * from foo where name = ?");

		StringBuilder countBuilder = new StringBuilder();
		countBuilder.append("select count(1) from ");
		countBuilder.append("(");
		countBuilder.append(builder.toString());
		countBuilder.append(") t ");
		int count = jdbcTemplate.queryForObject(countBuilder.toString(), Integer.class, new Object[] { fooName });
		Foo foo = null;
		if (count > 0) {
			foo = jdbcTemplate.queryForObject(builder.toString(), new RowMapper<Foo>() {
				@Override
				public Foo mapRow(ResultSet rs, int rowNum) throws SQLException {
					Foo tmp = new Foo();
					tmp.setId(rs.getInt(1));
					tmp.setName(rs.getString(2));
					return tmp;
				}
			}, new Object[] { fooName });
			log.info("{}", JSON.toJSONString(foo));
		}
		return foo;
	}

	@Override
	public Foo getFoo(String fooName, String barName) {
		return null;
	}

	@Override
	public void insertFoo(Foo foo) {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into foo(name) values(?)");
		int code = jdbcTemplate.update(builder.toString(), new Object[] { foo.getName() });
		log.info("code:{}", code);
		throw new UnsupportedOperationException("不支持的操作");
	}

	@Override
	public void updateFoo(Foo foo) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append("update foo set name = ? ");
		builder.append("where id = ?");

		int code = jdbcTemplate.update(builder.toString(), new Object[] { foo.getName(), foo.getId() });
		log.info("code:{}", code);
		// 即未检查异常时，会触发事务回滚。即RuntimeException及其子类
//		throw new RuntimeException("可以回滚的异常");
		// 其他已检查异常默认不会触发回滚事务
		throw new IOException("不能触发回滚到异常");
	}

	@Override
	public void testTimeout() {
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("insert into foo(name) values(?)");
		int code = jdbcTemplate.update(builder.toString(),
				new Object[] { String.valueOf(new Random().nextInt(10000)) });
		log.info("code:{}", code);
	}
}
