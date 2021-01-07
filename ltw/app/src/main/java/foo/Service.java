package foo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
public class Service {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public void test() {
		try {
			log.info("test");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
