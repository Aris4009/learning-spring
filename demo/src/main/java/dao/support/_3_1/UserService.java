package dao.support._3_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private final UserRepository userRepository;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public void execute(User user) {
//		log.info("{}", userRepository.countAll());
//		log.info("{}", userRepository.countById(user));
//		log.info("{}", userRepository.queryNameById(user));
//		log.info("{}", userRepository.queryUserById(user));
//		log.info("{}", JSON.toJSONString(userRepository.queryAllUserListById()));
//		log.info("{}", userRepository.insertUser(new User("haha")));
//		log.info("{}", userRepository.updateUserById(user));
//		log.info("{}", userRepository.deleteUserById(user));
		userRepository.createTable();
	}
}
