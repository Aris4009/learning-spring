package com.example.demo.custom.nature.bean.destruction.callback;

import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaConfigDestructCallback {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public class DestroyAutoClose implements AutoCloseable {

		@Override
		public void close() throws Exception {
			log.info("I am close");
		}
	}

	public class DestroyCloseable implements Closeable {
		@Override
		public void close() throws IOException {
			log.info("I am close");
		}
	}

	public class Destroy {

		public void destroy() {
			log.info("I am close");
		}
	}

	@Bean
	public DestroyAutoClose getDestroyAutoClose() {
		return new DestroyAutoClose();
	}

	@Bean
	public DestroyCloseable getDestroyCloseable() {
		return new DestroyCloseable();
	}

	@Bean(destroyMethod = "destroy")
	public Destroy getDestroy() {
		return new Destroy();
	}
}
