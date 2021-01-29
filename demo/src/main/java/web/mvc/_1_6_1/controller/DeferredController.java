package web.mvc._1_6_1.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api")
public class DeferredController {

	private static final Logger log = LoggerFactory.getLogger(DeferredController.class);

	@GetMapping("/test")
	public DeferredResult<Map<String, Object>> test() {
		DeferredResult<Map<String, Object>> deferredResult = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			log.info("Processing in another thread");
			try {
				Thread.sleep(5000);
				Map<String, Object> map = new HashMap<>();
				map.put("1", 2);
				deferredResult.setResult(map);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
				Thread.currentThread().interrupt();
			}
		});
		log.info("main thread");
		return deferredResult;
	}
}
