package symbol;

public class Type {
	private String typeName;

	public String getType() {
		return typeName;
	}

	public void setType(String name) {
		this.typeName = name;
	}

	public Type(String n) {
		typeName = n;
	}
}
