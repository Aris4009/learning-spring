package web.mvc.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class TestFutureTask {

	private static final Logger log = LoggerFactory.getLogger(TestFutureTask.class);

	public static void main(String[] args) {
		List<Future<String>> list = new ArrayList<>();
		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			Task task = new Task(i);
			list.add(executor.submit(task));
		}
		executor.shutdown();

		log.info("主线程执行");

		// 同步获取结果
		list.forEach(result -> {
			try {
				log.info("{}", result.get());
			} catch (InterruptedException | ExecutionException exception) {
				log.error(exception.getMessage(), exception);
				Thread.currentThread().interrupt();
			}
		});

		// 异步获取结果
//		list.forEach(result -> new Thread(() -> {
//			try {
//				log.info("{}", result.get());
//			} catch (InterruptedException | ExecutionException exception) {
//				log.error(exception.getMessage(), exception);
//				Thread.currentThread().interrupt();
//			}
//		}).start());

		log.info("{}", "执行完毕");
	}
}

class Task implements Callable<String> {

	private final int i;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public Task(int i) {
		this.i = i;
	}

	/**
	 * 模拟耗时计算
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public String call() throws Exception {
		StopWatch stopWatch = new StopWatch(String.valueOf(i));
		stopWatch.start();
		Thread.sleep(5000);
		int r = new Random().nextInt(Integer.MAX_VALUE);
		stopWatch.stop();
		log.info("{},{}", i, stopWatch.getTotalTimeMillis());
		return String.valueOf(i + "-------------------" + r);
	}
}
