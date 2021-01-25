package web.mvc._1_1.servlet;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class TestTomcat {

	public static void main(String[] args) throws LifecycleException {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(9999);
		tomcat.addWebapp("/test", "/");
		tomcat.start();
		tomcat.getServer().await();
	}
}
