package web.mvc.concurrent;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * 直接运行Executor，它没有强制要求执行是异步的，所以，直接调用会导致所有Runnable同步运行
 */
public class TestExecutor {

	public static void main(String[] args) {
		SimpleExecutorImpl executor = new SimpleExecutorImpl();
		for (int i = 0; i < 2; i++) {
			String id = String.valueOf(i);
			executor.setId(id);
			executor.execute(new SimpleRunnable(id));
		}
	}
}

class SimpleExecutorImpl implements Executor {

	private String id;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void execute(Runnable command) {
		StopWatch stopWatch = new StopWatch(this.id);
		stopWatch.start();
		command.run();
		stopWatch.stop();
		log.info("{}--{}", stopWatch.getId(), stopWatch.getTotalTimeMillis());
	}
}

class SimpleRunnable implements Runnable {

	private final String name;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public SimpleRunnable(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
			log.info("run:{}", this.name);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}
}
