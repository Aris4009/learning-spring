package data.access._1_7;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CreateUserPublisher {

	private final ApplicationEventPublisher publisher;

	private final JdbcTemplate jdbcTemplate;

	public CreateUserPublisher(ApplicationEventPublisher publisher, JdbcTemplate jdbcTemplate) {
		this.publisher = publisher;
		this.jdbcTemplate = jdbcTemplate;
	}

	public void publish(User user) {
		this.publisher.publishEvent(new CreateUserEvent(this.jdbcTemplate, user).insert());
	}
}
