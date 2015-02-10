package accrawler.common;

public enum Tag {
	CHILD(1, "child");

	private String name;
	private int id;

	Tag(int id, String name) {
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
