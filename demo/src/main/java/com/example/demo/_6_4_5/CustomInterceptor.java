package com.example.demo._6_4_5;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class CustomInterceptor implements MethodInterceptor {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Object obj = invocation.proceed();
		stopWatch.stop();
		long time = stopWatch.getTotalTimeNanos();
		int a = "|Method:【】".length() + invocation.getMethod().toString().length();
		StringBuilder builder = new StringBuilder();
		builder.append("\r\n");
		for (int i = 0; i < a; i++) {
			builder.append("-");
		}
		builder.append("\r\n");
		builder.append("|Method:【").append(invocation.getMethod()).append("】\r\n|Running time:【").append(time)
				.append("】 ns\r\n");
		for (int i = 0; i < a; i++) {
			builder.append("-");
		}
		builder.append("\r\n");
		log.info("{}", builder);
		return obj;
	}
}
