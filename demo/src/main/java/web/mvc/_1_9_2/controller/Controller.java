package web.mvc._1_9_2.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

	@GetMapping("/test")
	public ResponseEntity<String> test(String version) {
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS)).eTag(version).body(version);
	}
}
