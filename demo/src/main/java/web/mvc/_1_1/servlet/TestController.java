package web.mvc._1_1.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

	private HttpServletRequest request;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@GetMapping("/test")
	public Map<String, Object> test() {
		Enumeration<String> enumeration = request.getAttributeNames();
		int i = 100;
		while (enumeration.hasMoreElements() && i > 0) {
			log.info("{}", enumeration.nextElement());
			i--;
		}
		log.info("{}", i);
		Map<String, Object> map = new HashMap<>();
		map.put("1", "hello");
		map.put("2", 1);
		return map;
	}
}
