package com.example.demo.init.web.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.ApplicationScope;

/**
 * session级别的作用域
 */
@RestController
@RequestMapping("/api/bean/scope")
@ApplicationScope
public class ApplicationScopeController {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private RequestScopeBean requestScopeBean = new RequestScopeBean();

	@GetMapping("/application")
	public String requestScopeBean() {
		return String.valueOf(requestScopeBean.hashCode());
	}
}
