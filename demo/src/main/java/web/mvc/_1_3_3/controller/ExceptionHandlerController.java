package web.mvc._1_3_3.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ex")
public class ExceptionHandlerController {

	@PostMapping("/test")
	public void test() throws Exception {
		throw new Exception("hahah");
	}

	@ExceptionHandler(Exception.class)
	public Map<String, Object> handler(Exception ex) {
		Map<String, Object> map = new HashMap<>();
		map.put("ex", ex.getMessage());
		return map;
	}
}
