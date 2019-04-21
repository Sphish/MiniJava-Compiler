package symbol;


import java.util.LinkedHashMap;

public class ClassList extends Type {
	private static ClassList instance = null;
	private LinkedHashMap<String, Class> classes = new LinkedHashMap<>();

	private ClassList() {
		super("null");
	}

	public static ClassList getInstance() {
		if (instance == null)
			instance = new ClassList();
		return instance;
	}

	public Class getClass(String name) {
		return classes.get(name);
	}

	public void addClass(Class c) throws Exception {
		String name = c.getName();
		if (classes.containsKey(name))
			throw new Exception();

		classes.put(name, c);
	}

	public void updateParentClass() {
		for (Class c : classes.values()) {
			String pn = c.getParentName();
			if (pn != null) {
				c.setParent(classes.get(pn));
			}
		}
	}
}
