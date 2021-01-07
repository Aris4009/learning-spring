package foo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

@org.aspectj.lang.annotation.Aspect
public class Aspect {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * this is a pointcut
	 */
	@Pointcut("execution(public void foo.Service..*(..))")
	public void methodsToBeProfiled() {
	}

	@Around("methodsToBeProfiled()")
	public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		log.info("{}", "aspect");
		Object obj = null;
		StopWatch sw = new StopWatch(getClass().getSimpleName());
		try {
			Thread.sleep(5000L);
			sw.start(pjp.getSignature().getName());
			obj = pjp.proceed();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sw.stop();
			log.info("{}", sw.prettyPrint());
		}
		return obj;
	}

}
