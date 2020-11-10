package com.example.demo.custom.nature.bean.shuttingdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ShuttingContainer {

	private static Logger log = LoggerFactory.getLogger(ShuttingContainer.class);

	public static class Bean {

		public void destroy() {
			try {
				Thread.sleep(5000L);
				log.info("bean shutting down");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static void main(String[] args) {
		String path = "classpath:custom/nature/bean/shuttingDown/shuttingDown.xml";
		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(path);
		// 如果注释掉该方法，那么在主进程抛出异常后，程序立刻退出。如果注册了关闭钩子，那么即使发生异常后，bean也有机会执行。
		context.registerShutdownHook();
		int i = 10;
		int y = 0;
		int a = i / y;
		context.close();
		log.info("main shutting down");
	}
}
