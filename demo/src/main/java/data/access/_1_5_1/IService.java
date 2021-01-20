package data.access._1_5_1;

public interface IService {

	public User insertUser(User user);

	public Test insertTest(Test test);

	public void deleteUser(User user);

	public void deleteTest(Test test);

	public void insert();
}
