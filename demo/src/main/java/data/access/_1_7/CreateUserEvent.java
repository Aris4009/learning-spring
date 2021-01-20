package data.access._1_7;

import org.springframework.jdbc.core.JdbcTemplate;

public class CreateUserEvent {

	private final JdbcTemplate jdbcTemplate;

	private final User user;

	public CreateUserEvent(JdbcTemplate jdbcTemplate, User user) {
		this.jdbcTemplate = jdbcTemplate;
		this.user = user;
	}

	public CreateUserEvent insert() {
		StringBuilder builder = new StringBuilder();
		builder.append("insert into user(name) values(?)");
		jdbcTemplate.update(builder.toString(), new Object[] { user.getName() });
		return this;
	}
}
