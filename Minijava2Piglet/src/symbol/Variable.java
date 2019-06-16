package symbol;

public class Variable extends Identifier {
	public boolean initialized;
	public Type owner;
	public String rType;

	public Variable(int r, int c, String tn, String n, boolean i, Type o) {
		super(r, c, tn, n);
		owner = o;
		initialized = i;
		rType = tn;
	}

}
