package dao.support._3_1;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

	private final JdbcTemplate jdbcTemplate;

	public UserRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public int countAll() {
		return jdbcTemplate.queryForObject("select count(1) from user", Integer.class);
	}

	public int countById(User user) {
		return jdbcTemplate.queryForObject("select count(1) from user where id = ?", Integer.class, user.getId());
	}

	public String queryNameById(User user) {
		return jdbcTemplate.queryForObject("select name from user where id = ?", String.class, user.getId());
	}

	public User queryUserById(User user) {
		return jdbcTemplate.queryForObject("select * from user where id = ?", (resultSet, rowNum) -> {
			return new User(resultSet.getInt(1), resultSet.getString(2));
		}, user.getId());
	}

	public List<User> queryAllUserListById() {
		return jdbcTemplate.query("select * from user", (resultSet, rowNum) -> {
			return new User(resultSet.getInt(1), resultSet.getString(2));
		});
	}

	public int insertUser(User user) {
		return jdbcTemplate.update("insert into user(name) values(?)", user.getName());
	}

	public int updateUserById(User user) {
		return jdbcTemplate.update("update user set name = ? where id = ?", user.getName(), user.getId());
	}

	public int deleteUserById(User user) {
		return jdbcTemplate.update("delete from user where id = ?", user.getId());
	}

	public void createTable() {
		jdbcTemplate.execute("create table user1(id integer, name varchar(100))");
	}
}
