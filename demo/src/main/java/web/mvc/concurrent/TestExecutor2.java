package web.mvc.concurrent;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * 相比直接在Executor中执行Runnable，可以选择异步方式来运行，需要在Executor的execute方法中创建一个线程来执行Runnable
 */
public class TestExecutor2 {

	public static void main(String[] args) {
		Executor executor = new SimpleExecutorImpl2();
		for (int i = 0; i < 20; i++) {
			String name = String.valueOf(i);
			executor.execute(new SimpleRunnable2(name));
		}
	}
}

class SimpleExecutorImpl2 implements Executor {
	@Override
	public void execute(Runnable command) {
		new Thread(command).start();
	}
}

class SimpleRunnable2 implements Runnable {

	private final String name;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public SimpleRunnable2(String name) {
		this.name = name;
	}

	@Override
	public void run() {
		try {
			StopWatch stopWatch = new StopWatch(this.name);
			stopWatch.start();
			Thread.sleep(5000);
			log.info("run:{}", this.name);
			stopWatch.stop();
			log.info("{}--{}", stopWatch.getId(), stopWatch.getTotalTimeMillis());
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}
}
