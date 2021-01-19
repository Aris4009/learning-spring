package data.access._1_4_2;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Boot {

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("dataAccess/_1_4_2/bean.xml");
		FooService fooService = (FooService) ctx.getBean("fooService");
//		fooService.getFoo("hello");
//
//		Foo foo = new Foo();
//		foo.setName("hello2");
//		fooService.insertFoo(foo);
//
//		fooService.getFoo("hello2");

//		Foo foo = new Foo();
//		foo.setId(1);
//		foo.setName("test");
//		fooService.updateFoo(foo);

		fooService.testTimeout();
		ctx.close();
	}
}
