package web.mvc._1_9_2.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class Controller {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 使用etag，http状态码变为304
	 * 
	 * @param version
	 * @return
	 */
	@GetMapping("/test")
	public ResponseEntity<String> test(String version) {
		log.info("进入了方法");
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)).eTag(version).body(version);
	}

	@PostMapping("/test/post")
	public Map<String, Object> testPost(@RequestBody Map<String, Object> param) {
		return param;
	}
}
