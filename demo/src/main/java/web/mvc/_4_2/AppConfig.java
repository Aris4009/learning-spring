package web.mvc._4_2;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@Configuration
@ComponentScan
@EnableWebSocket
@EnableWebMvc
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	public static void main(String[] args) {
		try {
			Tomcat tomcat = new Tomcat();
			tomcat.setPort(9999);

			String baseDir = "/Users/aris/idea-workspace/learning-spring/demo/target/classes/web/mvc/_4_2";
			tomcat.addWebapp("", baseDir);

			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			log.error(e.getMessage(), e);
		}
	}
}
