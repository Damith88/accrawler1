package accrawler.common;

public enum Category {
	OTHER(1, "other"), ACCIDENT(2, "accident");

	private String name;
	private int id;

	Category(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
