package web.mvc._1_6_4.controller;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/api")
public class Test {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ExecutorService executor = Executors.newCachedThreadPool();

	@GetMapping("/test")
	public ResponseEntity<ResponseBodyEmitter> handle() {
		ResponseBodyEmitter emitter = new ResponseBodyEmitter();
		executor.execute(() -> {
			try {
				Thread.sleep(5000);
				emitter.send("/rbe" + " @ " + new Date(), MediaType.TEXT_PLAIN);
				emitter.complete();
			} catch (Exception ex) {
				emitter.completeWithError(ex);
			}
		});
		log.info("Main thread");
		return new ResponseEntity(emitter, HttpStatus.OK);
	}
}
