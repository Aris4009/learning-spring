package foo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

@ComponentScan
@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(App.class);
		context.refresh();

		Service service = context.getBean(Service.class);
		service.test();

//		ApplicationContext context = new ClassPathXmlApplicationContext("../app.xml", App.class);
//		Service service = (Service) context.getBean("service");
//		log.info("{}", service.getClass().getName());
//		service.test();

//		Service service = new Service();
//		service.test();
	}

}
