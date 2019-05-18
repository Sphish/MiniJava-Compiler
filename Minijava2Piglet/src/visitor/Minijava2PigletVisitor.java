package visitor;

import printer.PigletPrinter;

import symbol.*;
import symbol.Class;
import symbol.Identifier;
import symbol.Type;
import syntaxtree.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Minijava2PigletVisitor extends GJDepthFirst<Type, Type> {
	private ClassList classList = ClassList.getInstance();
	private HashMap <String, String> tmpVar = new HashMap<>();
	private int tmpIdx = 20;
	private int labelIdx = 0;
	private int paramIdx = 0;
	private int paramCount;
	private int paramAddr;

	private String getTempIdx(Method tMethod, String name) {
		Class tClass = tMethod.getOwner();
		String allName = tClass.getName() + "_" + tMethod.getName() + "_" + name;
		if (tmpVar.containsKey(allName)) {
			return tmpVar.get(allName);
		} else {
			tmpVar.put(allName, "" + tmpIdx);
			return "" + tmpIdx++;
		}
	}

	public Type visit(MainClass n, Type argu) {
		PigletPrinter.printMain();
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass(classType.getType());
		Method tMethod = tClass.getMethod("main");

		n.f11.accept(this, tMethod);
		n.f14.accept(this, tMethod);
		n.f15.accept(this, tMethod);

		PigletPrinter.printEnd();
		return null;
	}

	public Type visit(ClassDeclaration n, Type argu) {
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass(classType.getType());

		n.f3.accept(this, tClass); // VarDeclaration
		n.f4.accept(this, tClass); // Methodeclaration

		return null;
	}


	public Type visit(ClassExtendsDeclaration n, Type argu) {
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass((classType).getType());


		n.f3.accept(this, argu);
		n.f5.accept(this, tClass);
		n.f6.accept(this, tClass);

		return null;
	}

	public Type visit(MethodDeclaration n, Type argu) {
		n.f1.accept(this, argu);

		Identifier tIdentifier = (Identifier) n.f2.accept(this, argu);
		String name = tIdentifier.getName();

		Method tMethod = ((Class) argu).getMethod(name);
		int paraNum = tMethod.getParameters().size();
		PigletPrinter.printMethod(((Class) argu).getName(), name, paraNum + 1);
		PigletPrinter.printBegin();
		n.f4.accept(this, tMethod); // Params
		n.f7.accept(this, tMethod); // VarDeclaration
		n.f8.accept(this, tMethod); // Statements
		PigletPrinter.print("RETURN");
		n.f10.accept(this, tMethod); // Return Expressions
		PigletPrinter.println("");
		PigletPrinter.printEnd();

		return null;
	}


	// Expression
	public Type visit(Expression n, Type argu) {
		return n.f0.accept(this, argu);
	}

	public Type visit(AndExpression n, Type argu) {
		//logic: if f0 -> return f2 else return 0
		int tIdx = tmpIdx++;
		int lIdx = labelIdx++;
		PigletPrinter.printBegin();
		PigletPrinter.println("MOVE TEMP " + tIdx + " 0");
		PigletPrinter.print("CJUMP ");
		n.f0.accept(this, argu);
		PigletPrinter.println("L" + lIdx);
		PigletPrinter.print("MOVE TEMP " + tIdx + " ");
		n.f2.accept(this, argu);
		PigletPrinter.println("L" + lIdx + " NOOP");
		PigletPrinter.println("RETURN TEMP " + tIdx);
		PigletPrinter.printEnd();

		return null;
	}

	public Type visit(NotExpression n, Type argu) {
		// !f1
		PigletPrinter.print("MINUS 1 ");
		n.f1.accept(this, argu);
		return null;
	}

	public Type visit(CompareExpression n, Type argu) {
		PigletPrinter.print("LT ");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	public Type visit(PlusExpression n, Type argu) {
		PigletPrinter.print("PLUS ");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	public Type visit(MinusExpression n, Type argu) {
		PigletPrinter.print("MINUS ");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	public Type visit(TimesExpression n, Type argu) {
		PigletPrinter.print("TIMES ");
		n.f0.accept(this, argu);
		n.f2.accept(this, argu);
		return null;
	}

	public Type visit(BracketExpression n, Type argu) {
		// (f1)
		Type ret = n.f1.accept(this, argu);
		if (ret instanceof Identifier) {
			((Identifier) ret).setColumn(-1);
			((Identifier) ret).setRow(-1);
		}
		return ret;
	}

	public Type visit(ArrayLookup n, Type argu) {
		//f0[f2];
		int arrayIdx = tmpIdx++;
		int expIdx = tmpIdx++;
		int retIdx = tmpIdx++;
		int lenIdx = tmpIdx++;
		int labelTrue = labelIdx++;
		int labelFalse= labelIdx++;
		int labelInit = labelIdx++;

		PigletPrinter.printBegin();
		PigletPrinter.print("MOVE TEMP " + arrayIdx + " ");
		n.f0.accept(this, argu);
		PigletPrinter.println("");

		// check array address
		PigletPrinter.println("CJUMP LT TEMP " + arrayIdx + " 1 L" + labelInit);
		PigletPrinter.println("ERROR");
		PigletPrinter.print("L" + labelInit + " MOVE TEMP " + expIdx + " ");
		n.f2.accept(this, argu);
		PigletPrinter.println("");

		PigletPrinter.println("HLOAD TEMP " + lenIdx + " TEMP " + arrayIdx + " 0");
		PigletPrinter.println("CJUMP LT TEMP " + expIdx + " TEMP " + lenIdx + " L" + labelFalse);
		PigletPrinter.println("HLOAD TEMP " + retIdx + " PLUS TEMP " + arrayIdx +
			" TIMES 4 TEMP " + expIdx + " 4");
		// a[x] = *a + 4x + 4)
		PigletPrinter.println("JUMP L" + labelTrue);
		PigletPrinter.println("L" + labelFalse + " ERROR");
		PigletPrinter.println("L" + labelTrue + " NOOP");
		PigletPrinter.println("RETURN TEMP " + retIdx);
		PigletPrinter.printEnd();

		return null;
	}

	public Type visit(ArrayLength n, Type argu) {
		int arrayIdx = tmpIdx++;
		int retIdx = tmpIdx++;
		int labelInit = labelIdx++;
		PigletPrinter.printBegin();
		PigletPrinter.print("MOVE TEMP " + arrayIdx + " ");
		n.f0.accept(this, argu);
		PigletPrinter.println("");
		PigletPrinter.println("CJUMP LT TEMP " + arrayIdx + " 1 L + labelInit");
		PigletPrinter.println("ERROR");
		PigletPrinter.println("L" + labelInit + " HLOAD TEMP " + retIdx + " TEMP " + arrayIdx + " 0");
		return null;
	}

	public Type visit(MessageSend n, Type argu) {
		//f0.f2(f4?)
		int vTIdx = tmpIdx++;
		int dTIdx = tmpIdx++;
		int tIdx = tmpIdx++;
		int iLabel = labelIdx++;

		PigletPrinter.println("CALL");
		PigletPrinter.printBegin();
		PigletPrinter.print("MOVE TEMP " + vTIdx + " ");
		Identifier tIdent = (Identifier)n.f0.accept(this, argu);


		Class tClass;
		if (tIdent.getRow() == -1) // new A().a()
			tClass = ClassList.getInstance().getClass(tIdent.getType());
		else if (tIdent instanceof  Class) // this.a()
			tClass = (Class) tIdent;
		else {
			Method tMethod = (Method) argu;
			tClass = classList.getClass(tMethod.getVariable(tIdent.getName()).getType());
		}

		tIdent = (Identifier) n.f2.accept(this, tClass);
		Vector<Object> tmpV = tClass.getMethodClassPos(tIdent.getName());
		Class wClass = (Class) tmpV.get(1);
		Method iMethod = wClass.getMethod(tIdent.getName());
		paramCount = iMethod.getParameters().size();

		PigletPrinter.println("CJUMP LT TEMP " + vTIdx + " 1 L" + iLabel);
		PigletPrinter.println("ERROR");
		PigletPrinter.println("L" + iLabel + " MOVE TEMP " + vTIdx + " PLUS " + tmpV.get(0).toString()
			+ " TEMP " + vTIdx);
		PigletPrinter.println("HLOAD TEMP " + tIdx + " TEMP " + vTIdx + " 0");
		PigletPrinter.println("HLOAD TEMP " + dTIdx + " TEMP " + tIdx + " "
			+ wClass.getMethodPos(tIdent.getName()));
		PigletPrinter.println("RETURN TEMP " + dTIdx);
		PigletPrinter.printEnd();
		PigletPrinter.print("(TEMP " + vTIdx + " ");
		n.f4.accept(this, argu);
		PigletPrinter.println(")");
		return classList.getClass(iMethod.getType());
	}

	public Type visit(ExpressionList n, Type argu) {
		// only appear in message send
		// f0(f1)* ??
		int tIdx = tmpIdx++;
		PigletPrinter.printBegin();
		PigletPrinter.print("MOVE TEMP " + tIdx + " ");
		n.f0.accept(this, argu);
		PigletPrinter.println("");
		PigletPrinter.println("RETURN TEMP " + tIdx);
		PigletPrinter.printEnd();
		paramIdx = 1;
		n.f1.accept(this, argu); // ExpRest
		return null;
	}

	public Type visit(ExpressionRest n, Type argu) {
		// ,f1
		paramIdx++;
		if (paramIdx >= 19) {
			if (paramIdx == 19) {
				int tIdx = tmpIdx++;
				PigletPrinter.printBegin();
				PigletPrinter.println("MOVE TEMP " + tIdx
					+ " HALLOCATE " + (4 * (paramCount - 18)));
				paramAddr = tIdx;
			}
			int exceed = paramIdx - 19;
			PigletPrinter.print("HSTORE TEMP " + paramAddr + " " + 4 * exceed + " ");
			n.f1.accept(this, argu);
			PigletPrinter.println("");
			if (paramCount == paramIdx) {
				PigletPrinter.println("RETURN TEMP" + paramAddr);
				PigletPrinter.printEnd();
			}
		} else {
			int tIdx = tmpIdx++;
			PigletPrinter.printBegin();
			PigletPrinter.print("MOVE TEMP " + tIdx + " ");
			n.f1.accept(this, argu);
			PigletPrinter.println("");
			PigletPrinter.println("RETURN TEMP " + tIdx);
			PigletPrinter.printEnd();
		}
		return null;
	}

	public Type visit(PrimaryExpression n, Type argu) {
		Type primary = n.f0.accept(this, argu);
		if (primary instanceof Class) // thisExp
			return primary;
		if (primary instanceof Identifier) {
			Identifier tIdent = (Identifier) primary;
			if (tIdent.getRow() == -1) // alloc exp
				return primary;
			String name = tIdent.getName();
			Method tMethod = (Method) argu;
			Class tClass = tMethod.getOwner();
			if (tMethod.isTmpVar(name))
				PigletPrinter.print("TEMP " + getTempIdx(tMethod, name));
			else if (tMethod.isParam(name)) {
				int idx = tMethod.getParamIdx(name);
				if (idx < 18)
					PigletPrinter.print("TEMP "+(idx+1)+" ");
				else {
					idx -= 18;
					int tIdx = tmpIdx++;
					PigletPrinter.printBegin();
					PigletPrinter.println("HLOAD TEMP "+tIdx+" TEMP 19 "+4*idx);
					PigletPrinter.println("RETURN TEMP "+tIdx);
					PigletPrinter.printEnd();
				}
			} else {
				int tIdx = tmpIdx++;
				PigletPrinter.printBegin();
				PigletPrinter.println("HLOAD TEMP "+tIdx+" TEMP 0 "+tClass.getVariablePos(name));
				PigletPrinter.println("RETURN TEMP "+tIdx);
				PigletPrinter.printEnd();
			}
		}
		return primary;
	}

	public Type visit(IntegerLiteral n, Type argu) {
		PigletPrinter.print(n.f0.toString() + " ");
		n.f0.accept(this, argu);
		return null;
	}

	public Type visit(TrueLiteral n, Type argu) {
		PigletPrinter.print("1 ");
		n.f0.accept(this, argu);
		return null;
	}

	public Type visit(FalseLiteral n, Type argu) {
		PigletPrinter.print("0 ");
		n.f0.accept(this, argu);
		return null;
	}

	public Type visit(syntaxtree.Identifier n, Type argu) {
		String name = n.f0.toString();
		return new Identifier(0, 0, name, name);
	}

	public Type visit(ThisExpression n, Type argu) {
		PigletPrinter.print("TEMP 0");
		return ((Method) argu).getOwner();
	}

	public Type visit(ArrayAllocationExpression n, Type argu) {
		// new int[f3]
		int expIdx = tmpIdx++;
		int arrayIdx = tmpIdx++;
		int tIdx = tmpIdx++;
		int labelStart = labelIdx++;
		int labelEnd = labelIdx++;
		PigletPrinter.printBegin();
		PigletPrinter.print("MOVE TEMP " + expIdx + " ");
		n.f3.accept(this, argu);
		PigletPrinter.println("");
		PigletPrinter.println("MOVE TEMP " + arrayIdx + " HALLOCATE TIMES 4 PLUS 1 TEMP " + expIdx);
		// array length
		PigletPrinter.println("HSTORE TEMP " + arrayIdx + " 0 TEMP " + expIdx);
		// memset
		PigletPrinter.println("MOVE TEMP " + tIdx + " 0");
		PigletPrinter.println("L" + labelStart + " CJUMP LT TEMP " + tIdx
			+ " TEMP " + expIdx + " L" + labelEnd);
		PigletPrinter.println("HSTORE PLUS TEMP " + arrayIdx + " TIMES 4 TEMP " + tIdx + " 4 0");
		PigletPrinter.println("MOVE TEMP " + tIdx + " PLUS 1 TEMP " + tIdx);
		PigletPrinter.println("JUMP L" + labelStart);
		PigletPrinter.println("RETURN TEMP " + arrayIdx);
		PigletPrinter.printEnd();
		return null;
	}

	public Type visit(AllocationExpression n, Type argu) {
		// new f1()
		Identifier tIdent = (Identifier) n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().getClass(tIdent.getName());

		int vTableIdx = tmpIdx++;
		int allocated = 0;
		PigletPrinter.printBegin();
		PigletPrinter.println("MOVE TEMP " + vTableIdx + " HALLOCATE " + tClass.getSize());
		while (tClass != null) {
			int dTableIdx = tmpIdx++;
			LinkedHashMap<String, Method> methods = tClass.getMethods();
			PigletPrinter.println("MOVE TEMP " + dTableIdx +
				" HALLOCATE " + (4 * methods.size()));

			int i = 0;
			for (Method m : methods.values()) {
				PigletPrinter.println("HSTORE TEMP " + dTableIdx + " " + 4 * i
					+ " " + tClass.getName() + "_" + m.getName());
				i ++;
			}

			PigletPrinter.println("HSTORE TEMP " + vTableIdx + " " + 4 * allocated + " TEMP " + dTableIdx);
			allocated ++;

			int len = tClass.getVariables().size();
			for (int j = allocated; j < allocated + len; j ++) {
				PigletPrinter.println("HSTORE TEMP " + vTableIdx + " " + 4 * j + " 0");
			}
			allocated += len;
			tClass = tClass.getParent();
		}

		PigletPrinter.print("RETURN");
		PigletPrinter.println("TEMP " + vTableIdx);
		PigletPrinter.printEnd();

		return new Identifier(-1, -1, tIdent.getName(), tIdent.getName());
	}

	// statement
	public Type visit(ArrayAssignmentStatement n, Type argu) {
	//	f0[f2] = f5
		int arrayIdx = tmpIdx++;
		int expIdx = tmpIdx++;
		int lenIdx = tmpIdx++;
		int labelTrue = labelIdx++;
		int labelFalse= labelIdx++;
		int labelInit = labelIdx++;

		Identifier tIdent = (Identifier) n.f0.accept(this, argu);
		String name = tIdent.getName();
		Method tMethod = (Method) argu;
		Class tClass = tMethod.getOwner();

		if (tMethod.isTmpVar(name))
			PigletPrinter.println("MOVE TEMP " + arrayIdx + " TEMP " + getTempIdx(tMethod, name));
		else
			PigletPrinter.println("HLOAD TEMP " + arrayIdx + " TEMP 0 " + tClass.getVariablePos(name));
			// ?

		// check array address
		PigletPrinter.println("CJUMP LT TEMP " + arrayIdx + " 1 L" + labelInit);
		PigletPrinter.println("ERROR");
		PigletPrinter.print("L" + labelInit + " MOVE TEMP " + expIdx + " ");
		n.f2.accept(this, argu);
		PigletPrinter.println("");

		PigletPrinter.println("HLOAD TEMP " + lenIdx + " TEMP " + arrayIdx + " 0");
		PigletPrinter.println("CJUMP LT TEMP " + expIdx + " TEMP " + lenIdx + " L" + labelFalse);
		PigletPrinter.println("MOVE TEMP " + arrayIdx + " PLUS TEMP " + arrayIdx +
			" TIMES 4 PLUS 1 TEMP " + expIdx);
		PigletPrinter.print("HSTORE TEMP " + arrayIdx + " 0 ");
		// a[x] = *a + 4x + 4)
		n.f5.accept(this, argu);
		PigletPrinter.println("");
		PigletPrinter.println("JUMP L" + labelTrue);
		PigletPrinter.println("L" + labelFalse + " ERROR");
		PigletPrinter.println("L" + labelTrue + " NOOP");

		return null;
	}

	public Type visit(PrintStatement n, Type argu) {
		// print(f2);
		PigletPrinter.print("PRINT ");
		n.f2.accept(this, argu);
		PigletPrinter.println("");
		return null;
	}

	public Type visit(IfStatement n, Type argu) {
		// if (f2) f4 else f6
		int fLabel = labelIdx++;
		int tLabel = labelIdx++;
		PigletPrinter.print("CJUMP ");
		n.f2.accept(this, argu);
		PigletPrinter.println("L" + fLabel);
		n.f4.accept(this, argu);
		PigletPrinter.println("JUMP L" + tLabel);
		PigletPrinter.println("L" + fLabel + " NOOP");
		n.f6.accept(this, argu);
		PigletPrinter.println("L" + tLabel + " NOOP");
		return null;
	}

	public Type visit(WhileStatement n, Type argu) {
		// while (f2) f4
		int sLabel = labelIdx++;
		int eLabel = labelIdx++;
		PigletPrinter.print("L" +sLabel + " CJUMP ");
		n.f2.accept(this, argu);
		PigletPrinter.println("L" + eLabel);
		n.f4.accept(this, argu);
		PigletPrinter.println("JUMP L" + sLabel);
		PigletPrinter.println("L" + eLabel + " NOOP");
		return null;
	}

	public Type visit(AssignmentStatement n, Type argu) {
		// ???
		Identifier tIdent = (Identifier) n.f0.accept(this, argu);
		Method tMethod = (Method) argu;
		Class tClass = tMethod.getOwner();
		String name = tIdent.getName();
		int idx = -1, tNum = -1;
		//Variable lVar = tMethod.getVariable(name);

		if (tMethod.isTmpVar(name)) {
			PigletPrinter.print("MOVE TEMP " + getTempIdx(tMethod, name) + " ");
		} else if (tMethod.isParam(name)) {
			idx = tMethod.getParamIdx(name);
			if (idx < 18)
				PigletPrinter.print("MOVE TEMP " + (idx + 1) + " ");
			else {
				tNum = tmpIdx++;
				PigletPrinter.println("HLOAD TEMP " + tNum + " TEMP 19 " + 4 * (idx - 18));
			}
		} else { //class variable
			PigletPrinter.println("HSTORE TEMP 0 " + tClass.getVariablePos(name) + " ");
		}

		n.f2.accept(this, argu);
		PigletPrinter.println("");
		if (tMethod.isParam(name) && idx >= 18)
			PigletPrinter.println("HSTORE TEMP 19 " + 4 * (idx - 18) + " TEMP " + tNum);

		return null;
	}

}
