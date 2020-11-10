package com.example.demo.custom.nature.bean.cycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SmartLifecycleImpl implements SmartLifecycle {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	/**
	 * The stop method defined by SmartLifecycle accepts a callback. Any
	 * implementation must invoke that callback’s run() method after that
	 * implementation’s shutdown process is complete That enables asynchronous
	 * shutdown where necessary, since the default implementation of the
	 * LifecycleProcessor interface, DefaultLifecycleProcessor, waits up to its
	 * timeout value for the group of objects within each phase to invoke that
	 * callback.
	 */
	public void stop(Runnable callback) {
		// 可自行实现，如果不需要该方法，那就实现一个空方法。
		stop();
		// 此处非常重要，DefaultLifecycleProcessor需要执行callback方法，如果不调用run方法，将导致stop失败
		// 该类默认stop的超时时间为30秒
		callback.run();
	}

	@Override
	public boolean isRunning() {
		try {
			Thread.sleep(2000L);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
	}

	@Override
	public boolean isAutoStartup() {
		log.info("The app start");
		return true;
	}

	@Override
	/**
	 * 数值越高，启动时越早执行，关闭时越晚关闭,最大值为Integer.MAX_VALUE 任何未实现该接口的对象的默认值是0
	 */
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/lifecycle/lifecycle.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(path);
		context.close();
	}
}
