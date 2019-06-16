package visitor;

import symbol.FlowGraph;
import symbol.ProcedureBlock;
import syntaxtree.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class Spiglet2KangaVisitor extends GJDepthFirst<Object, Object> {
	private HashMap<String, FlowGraph> flowGraphHashMap = new HashMap<String, FlowGraph>();
	private HashMap<String, String> globalLabel = new HashMap<String, String>();
	private ProcedureBlock curpBlock;
	private FlowGraph curFlowGraph;
	private int curLabel = 0;

	private void println(String str) {
		System.out.println(str);
	}

	public void print(String str) {
		System.out.print(str);
	}

	private String getReg(String tmpNumStr, String defaultReg) {
		int tmpNum = Integer.parseInt(tmpNumStr);
		if (curpBlock.regStack.containsKey(tmpNum)) {
			println("ALOAD " + defaultReg + " " + curpBlock.regStack.get(tmpNum));
			return defaultReg;
		} else {
			return curpBlock.regCandi.get(tmpNum);
		}
	}

	private void moveReg(String tmpNumStr, String exp) {
		int tmpNum = Integer.parseInt(tmpNumStr);
		if (curpBlock.regSkip.contains(tmpNum)) {
			return;
		}
		if (curpBlock.regStack.containsKey(tmpNum)) {
			println("MOVE v0 " + exp);
			println("ASTORE " + curpBlock.regStack.get(tmpNum) + " v0");
		} else {
			String reg = getReg(tmpNumStr, "");
			println("MOVE " + reg + " " + exp);
		}
	}

	private String newLabel(String oldLabel) {
		if (globalLabel.containsKey(oldLabel)) {
			return globalLabel.get(oldLabel);
		}
		String newL = "L" + curLabel;
		curLabel++;
		globalLabel.put(oldLabel, newL);
		return newL;
	}

	private void storeS07() {
		int useStackNum = curpBlock.useStack;
		int usedRegNum = curpBlock.regSave.size();
		int startPos = useStackNum - usedRegNum;
		for (String regname : curpBlock.regSave) {
			println("ASTORE SPILLEDARG " + startPos + " " + regname);
			startPos++;
		}
	}

	private void loadParameters() {
		int paranum = curpBlock.paranum;
		for (int i = 0; i < 4 && i < paranum; ++i) {
			if (curpBlock.tmpMap.containsKey(i)) {
				moveReg("" + i, "a" + i);
			}
		}
		if (paranum > 4) {
			for (int i = 4; i < paranum; ++i) {
				if (curpBlock.tmpMap.containsKey(i)) {
					if (curpBlock.regCandi.containsKey(i)) {
						println("ALOAD " + getReg("" + i, "") + " SPILLEDARG " + (i - 4));
					} else if (curpBlock.regStack.containsKey(i)) {
						println("ALOAD v0 SPILLEDARG " + (i - 4));
						moveReg("" + i, "v0");
					}
				}
			}
		}
	}

	private void loadS07() {
		int useStackNum = curpBlock.useStack;
		int usedRegNum = curpBlock.regSave.size();
		int startPos = useStackNum - usedRegNum;
		for (String regname : curpBlock.regSave) {
			println("ALOAD " + regname + " SPILLEDARG " + startPos);
			startPos++;
		}
	}


	public Object visit(NodeListOptional n, Object argu) {
		Vector<String> _ret = new Vector<>();
		if (n.present()) {
			int _count = 0;
			for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				String reg = (String) e.nextElement().accept(this, argu);
				_ret.add(reg);
				_count++;
			}
		}
		return _ret;
	}

	public Object visit(NodeOptional n, Object argu) {
		if (n.present()) {
			Object _ret = n.node.accept(this, argu);
			print(newLabel((String) _ret) + " ");
			return _ret;
		} else
			return null;
	}

	public Object visit(NodeToken n, Object argu) {
		return null;
	}

	public Object visit(Goal n, Object argu) {
		flowGraphHashMap = (HashMap<String, FlowGraph>) argu;
		curFlowGraph = flowGraphHashMap.get("MAIN");
		curpBlock = curFlowGraph.pBlock;
		globalLabel.clear();
		println("MAIN [0][" + (curpBlock.useStack - curpBlock.regSave.size()) + "][" + curpBlock.inCall + "]");
		n.f1.accept(this, argu);
		println("END");
		n.f3.accept(this, argu);
		return null;
	}

	public Object visit(Procedure n, Object argu) {
		String pName = (String) n.f0.accept(this, argu);
		int paraNum = Integer.parseInt((String) n.f2.accept(this, argu));
		curFlowGraph = flowGraphHashMap.get(pName);
		curpBlock = curFlowGraph.pBlock;
		globalLabel.clear();
		println(pName + " [" + paraNum + "][" + curFlowGraph.pBlock.useStack + "][" + curFlowGraph.pBlock.inCall + "]");
		storeS07();
		loadParameters();
		String reg = (String) n.f4.accept(this, argu);
		println("MOVE v0 " + reg);
		loadS07();
		println("END");
		return null;
	}

	public Object visit(NoOpStmt n, Object argu) {
		println("NOOP");
		return null;
	}

	public Object visit(ErrorStmt n, Object argu) {
		println("ERROR");
		return null;
	}

	public Object visit(CJumpStmt n, Object argu) {
		String reg = getReg((String) n.f1.accept(this, argu), "v0");
		String label = (String) n.f2.accept(this, argu);
		println("CJUMP " + reg + " " + newLabel(label));
		return null;
	}

	public Object visit(JumpStmt n, Object argu) {
		String label = (String) n.f1.accept(this, argu);
		println("JUMP " + newLabel(label));
		return null;
	}

	public Object visit(HStoreStmt n, Object argu) {
		String reg1 = getReg((String) n.f1.accept(this, argu), "v0");
		String bias = (String) n.f2.accept(this, argu);
		String reg2 = getReg((String) n.f3.accept(this, argu), "v1");
		println("HSTORE " + reg1 + " " + bias + " " + reg2);
		return null;
	}

	public Object visit(HLoadStmt n, Object argu) {
		String tmp1 = (String) n.f1.accept(this, argu);
		String reg2 = getReg((String) n.f2.accept(this, argu), "v1");
		int bias = Integer.parseInt((String) n.f3.accept(this, argu));
		if (curpBlock.regStack.containsKey(Integer.parseInt(tmp1))) {
			println("HLOAD v1 " + reg2 + " " + bias);
			moveReg(tmp1, "v1");
		} else {
			println("HLOAD " + getReg(tmp1, "v0") + " " + reg2 + " " + bias);
		}
		return null;
	}

	public Object visit(MoveStmt n, Object argu) {
		String reg1 = (String) n.f1.accept(this, argu);
		String exp = (String) n.f2.accept(this, argu);
		moveReg(reg1, exp);
		return null;
	}

	public Object visit(PrintStmt n, Object argu) {
		String sExp = (String) n.f1.accept(this, argu);
		println("PRINT " + sExp);
		return null;
	}

	public Object visit(Exp n, Object argu) {
		return n.f0.accept(this, argu);
	}

	public Object visit(StmtExp n, Object argu) {
		n.f1.accept(this, argu);
		return n.f3.accept(this, argu);
	}

	public Object visit(Call n, Object argu) {
		Vector<String> paras = (Vector<String>) n.f3.accept(this, argu);
		int size = paras.size();
		for (int i = 0; i < size && i <= 3; ++i) {
			println("MOVE a" + i + " " + getReg(paras.get(i), "v0"));
		}
		if (size > 4) {
			for (int i = 4; i < size; ++i) {
				println("PASSARG " + (i - 3) + " " + getReg(paras.get(i), "v0"));
			}
		}
		String sExp = (String) n.f1.accept(this, argu);
		println("CALL " + sExp);
		return "v0";
	}

	public Object visit(HAllocate n, Object argu) {
		String sExp = (String) n.f1.accept(this, argu);
		return "HALLOCATE " + sExp;
	}

	public Object visit(BinOp n, Object argu) {
		String op = (String) n.f0.accept(this, argu);
		String reg = getReg((String) n.f1.accept(this, argu), "v1");
		String sExp = (String) n.f2.accept(this, argu);
		return op + " " + reg + " " + sExp;
	}

	public Object visit(Operator n, Object argu) {
		int choice = n.f0.which;
		if (choice == 0)
			return "LT";
		else if (choice == 1)
			return "PLUS";
		else if (choice == 2)
			return "MINUS";
		else if (choice == 3)
			return "TIMES";
		return null;
	}

	public Object visit(SimpleExp n, Object argu) {
		Object _ret = n.f0.accept(this, argu);
		if (n.f0.which == 0) {
			_ret = getReg((String)_ret, "v0");
		}
		return _ret;
	}

	public Object visit(Temp n, Object argu) {
		n.f0.accept(this, argu);
		return n.f1.accept(this, argu);
	}

	public Object visit(IntegerLiteral n, Object argu) {
		return n.f0.toString();
	}

	public Object visit(Label n, Object argu) {
		return n.f0.toString();
	}

}
