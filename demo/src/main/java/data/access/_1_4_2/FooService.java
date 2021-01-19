package data.access._1_4_2;

import java.io.IOException;

public interface FooService {

	Foo getFoo(String fooName);

	Foo getFoo(String fooName, String barName);

	void insertFoo(Foo foo);

	void updateFoo(Foo foo) throws IOException;

	void testTimeout();
}
