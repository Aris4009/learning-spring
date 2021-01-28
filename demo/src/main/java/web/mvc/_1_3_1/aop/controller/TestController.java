package web.mvc._1_3_1.aop.controller;

import com.example.demo.gson.JSON;
import data.access._1_5_1.Test;
import data.access._1_7.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import web.mvc._1_3_1.aop.service.Service;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

	private Service service;

	public TestController(Service service) {
		this.service = service;
	}

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/test")
	@Transactional
	public Map<String, Object> test() {
		service.insertTest(new Test("1"));
		service.insertUser(new User("2"));
		return null;
	}

	@GetMapping("/request")
	public Map<String, Object> request(WebRequest webRequest, NativeWebRequest nativeWebRequest) {
		log.info("{}", JSON.toJSONString(webRequest.getParameterMap()));
		log.info("{}", JSON.toJSONString(nativeWebRequest.getNativeRequest()));
		return null;
	}
}
