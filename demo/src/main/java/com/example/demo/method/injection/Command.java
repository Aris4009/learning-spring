package com.example.demo.method.injection;

import java.util.Map;

public class Command {

	private Map state;

	public Map getState() {
		return state;
	}

	public void setState(Map state) {
		this.state = state;
	}

	public Object execute() {
		return new Object();
	}

	@Override
	public String toString() {
		return "Command{" + "state=" + state + '}';
	}
}
