package symbol;

import java.util.LinkedHashMap;
import java.util.Vector;

public class Class extends Identifier {
	private LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();
	private LinkedHashMap<String, Method> methods = new LinkedHashMap<>();

	private Class parent;
	private String parentName;
	public int size;


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public Class(int r, int c, String n, String p) {
		super(r, c, n, n);
		parentName = p;
		parent = null;
		size = -1;
	}

	public int getSize() {
		if (size != -1) return size;
		if (parent == null)
			return 4 * (1 + variables.size());
		return 4 * (1 + variables.size()) + parent.getSize();
	}

	public void addMethod(Method method) throws Exception {
		String name = method.getName();
		if (methods.containsKey(name))
			throw new Exception();

		methods.put(name, method);
	}

	public LinkedHashMap<String, Variable> getVariables() {
		return variables;
	}

	public Method getMethod(String name) {
		return methods.get(name);
	}

	public LinkedHashMap<String, Method> getMethods() {
		return methods;
	}

	String getParentName() {
		return parentName;
	}

	void setParent(Class parent) {
		this.parent = parent;
	}

	public void addVariable(Variable var) throws Exception {
		String name = var.getName();
		if (variables.containsKey(name)) {
			throw new Exception("Error");
		}

		variables.put(name, var);
	}

	public int getVarIdx(String name) {
		int i = 0;

		for (String k : variables.keySet()) {
			if (k.equals(name)) {
				return i;
			}
			i ++;
		}

		return -1;
	}

	public Variable getVariable(String name) {
		if (variables.get(name) != null || parent == null)
			return variables.get(name);
		return parent.getVariable(name);
	}

	public Class getParent() {
		return parent;
	}

	public int getVariablePos(String name) {
		Class tClass = this;
		int sum = 0;
		int tmp;
		while (true) {
			tmp = tClass.getVarIdx(name);
			if (tmp != -1)
				break;
			sum += 1 + tClass.variables.size();
			tClass = tClass.getParent();
		}

		return (1 + tmp + sum) * 4;
	}

	public Vector<Object> getMethodClassPos(String name) {
		Vector<Object> retVec = new Vector();
		Class tmpClass = this;
		int ret = 0;
		while (true) {
			if (tmpClass.getMethod(name) != null) {
				retVec.add(4 * ret);
				retVec.add(tmpClass);
				return retVec;
			}
			ret += tmpClass.variables.size() + 1;
			tmpClass = tmpClass.getParent();
		}
	}

	public int getMethodPos(String name) {
		int len = methods.size();
		int i = 0;
		for (Method method : methods.values()) {
			if (name.equals(method.getName()))
				return 4 * i;
			i++;
		}
		return -1;
	}
}
