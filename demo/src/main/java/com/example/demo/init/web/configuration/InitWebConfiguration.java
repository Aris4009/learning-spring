package com.example.demo.init.web.configuration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 通过编程实现web配置初始化
 */
public class InitWebConfiguration {

	public static void main(String[] args) {
		String path = "classpath:dispatcher-config.xml";
		WebApplicationInitializer webApplicationInitializer = new WebApplicationInitializer() {
			@Override
			public void onStartup(ServletContext servletContext) throws ServletException {
				XmlWebApplicationContext context = new XmlWebApplicationContext();
				context.setConfigLocation(path);
				ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher",
						new DispatcherServlet(context));
				dispatcher.setLoadOnStartup(1);
				dispatcher.addMapping("/");
			}
		};
	}
}
