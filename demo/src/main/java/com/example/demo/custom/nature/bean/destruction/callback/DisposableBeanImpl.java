package com.example.demo.custom.nature.bean.destruction.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class DisposableBeanImpl implements DisposableBean {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void destroy() throws Exception {
		log.info("{}-{}", this.getClass(), "销毁");
	}
}
