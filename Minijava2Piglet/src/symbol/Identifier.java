package symbol;

public class Identifier extends Type {
	private int row, column;
	private String name;

	public Identifier(int r, int c, String tn, String n) {
		super(tn);
		row = r;
		column = c;
		name = n;
	}

	public String getName() {
		return name;
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addVariable(Variable var) throws Exception {
		throw new Exception("");
	}
}
