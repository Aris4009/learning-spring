package data.access._1_5_1;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Boot {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("dataAccess/_1_5_1/bean.xml");
		ServiceImpl service = context.getBean("service", ServiceImpl.class);

		User user = new User("我是用户1");
		Test test = new Test("我是测试1");
		service.setUser(user);
		service.setTest(test);
		service.insert();

		context.close();
	}
}
