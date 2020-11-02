package com.example.demo.method.injection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * look up method injection
 */
public abstract class LookUpInjection {

	public static Logger log = LoggerFactory.getLogger(LookUpInjection.class);

	protected abstract Command createCommand();

	public Object process(Map commandState) {
		Command command = createCommand();
		command.setState(commandState);
		return command.execute();
	}

	public static void main(String[] args) {
		String path = "classpath:lookUpMethodInjection.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		LookUpInjection commandManager = context.getBean("commandManager", LookUpInjection.class);
		Object process = commandManager.process(new HashMap<String, String>() {
			{
				put("a", "b");
			}
		});
		log.info("commandManager.hashCode:{}", commandManager.hashCode());

		commandManager.process(new HashMap<String, String>() {
			{
				put("c", "d");
			}
		});
		log.info("commandManager.hashCode:{}", commandManager.hashCode());
		context.close();
	}
}
