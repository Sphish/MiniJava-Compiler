import java.io.*;

import symbol.ClassList;
import visitor.*;
import syntaxtree.*;

public class Main {
	public static void main(String []args) {
		try {
			InputStream in = new FileInputStream(args[0]);
			Node root =  new MiniJavaParser(in).Goal();
			root.accept(new BuildSymbolTableVisitor(), null);
			ClassList.getInstance().updateParentClass();
			root.accept(new Minijava2PigletVisitor(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}