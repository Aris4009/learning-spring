package web.mvc._1_6_1.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api")
public class DeferredController {

	private static Logger log = LoggerFactory.getLogger(DeferredController.class);

	@GetMapping("/test")
	public DeferredResult<Map<String, Object>> test() {
		DeferredResult<Map<String, Object>> deferredResult = new DeferredResult<>();
		return deferredResult;
	}
}
