import syntaxtree.Node;
import visitor.P2SVisitor;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {

	public static void main(String[] args) {
		try {
			InputStream in = new FileInputStream(args[0]);
			Node root = new PigletParser(in).Goal();
			P2SVisitor v = new P2SVisitor();
			root.accept(v, null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
