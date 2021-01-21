package data.access._1_7;

import data.access._1_5_1.Test;

public class CreateUserEvent {

	private final User user;

	private final Test test;

	public CreateUserEvent(User user, Test test) {
		this.user = user;
		this.test = test;
	}

	public User getUser() {
		return user;
	}

	public Test getTest() {
		return test;
	}

	@Override
	public String toString() {
		return "CreateUserEvent{" + "user=" + user + ", test=" + test + '}';
	}
}
