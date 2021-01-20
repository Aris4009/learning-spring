package data.access._1_7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MyComponent {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

//	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.AFTER_COMMIT)
//	public void handleSuccessCreateUserEvent(CreateUserEvent createUserEvent) {
//		log.info("处理成功的事务事件");
//	}

	@TransactionalEventListener(classes = { CreateUserEvent.class }, phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleFailCreateUserEvent(CreateUserEvent createUserEvent) {
		log.info("处理失败的事务事件");
	}
}
