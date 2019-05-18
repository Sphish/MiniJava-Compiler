package visitor;

import java.util.Enumeration;

import syntaxtree.*;


public class P2SVisitor extends GJDepthFirst<String, Object> {
	private int tmpIdx = 1000;
	private int tab = 0;

	private void print(String s) {
		for (int i = 0; i < tab; i++)
			System.out.print("  ");
		System.out.print(s);
	}

	private void println(String s) {
		print(s);
		System.out.println();
	}

	private void printBegin() {
		println("BEGIN");
		tab++;
	}

	private void printEnd() {
		tab--;
		println("END");
	}

	public String visit(NodeListOptional n, Object argu) {
		String _ret = "";
		if (n.present()) {
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				_ret += e.nextElement().accept(this, null);
			}
		}
		return _ret;
	}

	public String visit(NodeOptional n, Object argu) {
		if (n.present()) {
			System.out.println(n.node.accept(this, null));
		}
		return null;
	}

	public String visit(Goal n, Object argu) {
		print("MAIN");
		tab++;
		n.f1.accept(this, null);
		printEnd();
		n.f3.accept(this, null);
		return null;
	}

	public String visit(Procedure n, Object argu) {
		println("");
		println(n.f0.accept(this, argu) + "[ " + n.f2.accept(this, argu) + "] ");
		printBegin();
		println("RETURN " + n.f4.accept(this, null));
		printEnd();
		return "";
	}

	public String visit(NoOpStmt n, Object argu) {
		println("NOOP");
		return null;
	}

	public String visit(CJumpStmt n, Object argu) {
		println("CJUMP " + n.f1.accept(this, null) + n.f2.accept(this, null));
		return null;
	}

	public String visit(JumpStmt n, Object argu) {
		println("JUMP " + n.f1.accept(this, argu));
		return null;
	}


	public String visit(HStoreStmt n, Object argu) {
		println("HSTORE " + n.f1.accept(this, argu)
			+ n.f2.accept(this, argu)
			+ n.f3.accept(this, argu)
		);
		return null;

	}

	public String visit(HLoadStmt n, Object argu) {
		println("HLOAD " + n.f1.accept(this, argu)
			+ n.f2.accept(this, argu)
			+ n.f3.accept(this, argu)
		);
		return null;
	}


	public String visit(MoveStmt n, Object argu) {
		println("MOVE " + n.f1.accept(this, argu)
			+ n.f2.accept(this, argu)
		);
		return null;
	}

	public String visit(PrintStmt n, Object argu) {
		println("PRINT " + n.f1.accept(this, argu)
		);
		return null;
	}

	public String visit(Exp n, Object argu) {
		String ret = "TEMP " + tmpIdx++ + " ";
		println("MOVE " + ret + n.f0.accept(this, argu));
		return ret;
	}

	public String visit(StmtExp n, Object argu) {
		n.f1.accept(this, null);
		return n.f3.accept(this, null);
	}

	public String visit(Call n, Object argu) {
		return "CALL " + n.f1.accept(this, null) + "( " + n.f3.accept(this, null) + ")";
	}

	public String visit(HAllocate n, Object argu) {
		return "HALLOCATE " + n.f1.accept(this, null);
	}


	public String visit(BinOp n, Object argu) {
		return n.f0.accept(this, null) + 
			n.f1.accept(this, null) + 
			n.f2.accept(this, null);
	}
	
	public String visit(Operator n, Object argu) {
		String[] ops = { "LT ", "PLUS ", "MINUS ", "TIMES " };
		return ops[n.f0.which];
	}


	public String visit(Temp n, Object argu) {
		return "TEMP " + n.f1.accept(this, argu);
	}

	public String visit(IntegerLiteral n, Object argu) {
		return n.f0.toString() + " ";
	}

	public String visit(Label n, Object argu) {
		return n.f0.toString() + " ";
	}
}