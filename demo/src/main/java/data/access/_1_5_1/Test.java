package data.access._1_5_1;

public class Test {

	private int id;

	private String name;

	public Test(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Test(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Test{" + "id=" + id + ", name='" + name + '\'' + '}';
	}
}
