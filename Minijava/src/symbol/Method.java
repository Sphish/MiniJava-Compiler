package symbol;

import java.util.LinkedHashMap;

public class Method extends Identifier {
	private Class owner;
	private LinkedHashMap<String, Variable> parameters = new LinkedHashMap<>();
	private LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();

	public Method(int r, int c, String t, String n, Class o) {
		super(r, c, t, n);
		owner = o;
	}

	public void addParameter(Variable var) throws Exception {
		String name = var.getName();
		if (variables.containsKey(name) || parameters.containsKey(name))
			throw new Exception("Error");

		parameters.put(name, var);

	}

	public void addVariable(Variable var) throws Exception {
		String name = var.getName();
		if (variables.containsKey(name) || parameters.containsKey(name)) {
			throw new Exception("Error");
		}

		variables.put(name, var);
	}

	public LinkedHashMap<String, Variable> getParameters() {
		return parameters;
	}

	public Variable getVariable(String name) {
		if (variables.get(name) != null)
			return variables.get(name);
		if (parameters.get(name) != null)
			return parameters.get(name);
		return owner.getVariable(name);
	}

	public boolean isParam(String name) {
		return parameters.containsKey(name);
	}

	public int getParamIdx(String name) {
		int i = 0;

		for (String k : parameters.keySet()) {
			if (k.equals(name)) {
				return i;
			}
			i ++;
		}

		return -1;
	}

	public boolean isTmpVar(String name) {
		return variables.containsKey(name);
	}

	public Variable getParameter(String name) {
		return parameters.get(name);
	}

	public Class getOwner() {
		return owner;
	}
}