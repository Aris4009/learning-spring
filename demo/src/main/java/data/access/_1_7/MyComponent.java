package data.access._1_7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MyComponent {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.BEFORE_COMMIT)
	public void beforeCommit(CreateUserEvent createUserEvent) {
		log.info("事务事件提交前,:{},{}", createUserEvent.getUser(), createUserEvent.getTest());
	}

	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.AFTER_COMPLETION)
	public void afterCompletion(CreateUserEvent createUserEvent) {
		log.info("事务事件完成后");
	}

	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.AFTER_COMMIT)
	public void afterCommit(CreateUserEvent createUserEvent) {
		log.info("事务事件提交后");
	}

	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.AFTER_ROLLBACK)
	public void afterRollback(CreateUserEvent createUserEvent) {
		log.info("事务事件回滚后");
	}

}
