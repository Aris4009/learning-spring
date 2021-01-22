package dao.support._3_1;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class NamedUserRepository {

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public NamedUserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public int countByUserName(User user) {
		String sql = "select count(1) from user where name = :name";
		SqlParameterSource sqlParameterSource = new MapSqlParameterSource("name", user.getName());
		return this.namedParameterJdbcTemplate.queryForObject(sql, sqlParameterSource, Integer.class);
	}

}
