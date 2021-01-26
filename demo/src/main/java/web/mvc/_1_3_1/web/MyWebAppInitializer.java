package web.mvc._1_3_1.web;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class MyWebAppInitializer implements WebApplicationInitializer {

	private static Logger log = LoggerFactory.getLogger(MyWebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(WebConfig.class);

		servletContext.addListener(new ContextLoaderListener(context));
		Servlet servlet = new DispatcherServlet(context);
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", servlet);
		dispatcher.addMapping("/api");
		dispatcher.setLoadOnStartup(1);
	}

	public static void main(String[] args) {
		try {
			Tomcat tomcat = new Tomcat();
			tomcat.setPort(9999);
			tomcat.addWebapp("/", System.getProperty("java.io.tmpdir"));
			tomcat.start();
			tomcat.getServer().await();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
