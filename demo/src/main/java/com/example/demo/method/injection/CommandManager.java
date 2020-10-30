package com.example.demo.method.injection;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 这种例子是不可取的，不要这样做
 */
@Deprecated
public class CommandManager implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public static Logger log = LoggerFactory.getLogger(CommandManager.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected Command create() {
		return this.applicationContext.getBean("command", Command.class);
	}

	public Object process(Map commandState) {
		Command command = create();
		log.info("command.hashCode:{}", command.hashCode());
		command.setState(commandState);
		return command.execute();
	}

	public static void main(String[] args) {
		String path = "classpath:methodInjection.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		CommandManager commandManager = context.getBean("commandManager", CommandManager.class);
		commandManager.process(new HashMap<String, String>() {
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
