package com.example.demo.init.web.configuration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

/**
 * session级别的作用域
 */
@RestController
@RequestMapping("/api/bean/scope")
@SessionScope
public class SessionScopeController {

	private RequestScopeBean requestScopeBean = new RequestScopeBean();

	@GetMapping("/session")
	public String requestScopeBean() {
		return String.valueOf(requestScopeBean.hashCode());
	}
}
