package com.example.demo.custom.nature.bean.cycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 实现lifecycle
 */
public class LifecycleImpl implements Lifecycle {

	private String name = "aaa";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	/**
	 * NOTE: This interface does not imply specific auto-startup semantics. Consider
	 * implementing SmartLifecycle for that purpose
	 */
	public void start() {
		log.info("start");
	}

	@Override
	public void stop() {
		log.info("stop");
		try {
			Thread.sleep(2000L);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("{}", "The component have been stopped");
	}

	@Override
	public boolean isRunning() {
		try {
			Thread.sleep(2000L);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("{}", "The component is running");
		return true;
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/lifecycle/lifecycle.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
