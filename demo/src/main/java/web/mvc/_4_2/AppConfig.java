package web.mvc._4_2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		WebServer webServer = null;
		try {
			TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory("", 9999);
			webServer = factory.getWebServer(new WebAppInitializer());
			webServer.start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
