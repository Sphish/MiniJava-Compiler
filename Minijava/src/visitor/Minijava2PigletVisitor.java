package visitor;

import printer.PigletPrinter;

import symbol.*;
import symbol.Class;
import symbol.Identifier;
import symbol.Type;
import syntaxtree.*;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Minijava2PigletVisitor extends GJDepthFirst<Type, Type> {
	private ClassList classList;
	private HashMap <String, String> tmpVar = new HashMap<>();
	private int tmpIdx = 20;

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
			getClass(((Identifier) classType).getType());
		Method tMethod = tClass.getMethod("main");

		n.f11.accept(this, tMethod);
		n.f14.accept(this, tMethod);
		n.f15.accept(this, tMethod);

		return null;
	}

	public Type visit(ClassDeclaration n, Type argu) {
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass(((Identifier) classType).getType());

		n.f3.accept(this, tClass); // VarDeclaration
		n.f4.accept(this, tClass); // Methodeclaration

		return null;
	}


	public Type visit(ClassExtendsDeclaration n, Type argu) {
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass(((Identifier) classType).getType());

		Type extClassType = n.f3.accept(this, argu);
		Class extClass = ClassList.getInstance().
			getClass(((Identifier) extClassType).getType());

		n.f2.accept(this, argu);
		n.f3.accept(this, tClass);
		n.f4.accept(this, tClass);
		n.f5.accept(this, argu);


		return null;
	}

	public Type visit(MethodDeclaration n, Type argu) {
		Type type = n.f1.accept(this, argu);

		Identifier tIdentifier = (Identifier) n.f2.accept(this, argu);
		String name = tIdentifier.getName();

		Method tMethod = ((Class) argu).getMethod(name);
		int paraNum = tMethod.getParameters().size();
		PigletPrinter.printMethod(((Class) argu).getName(), name, paraNum + 1);
		PigletPrinter.printBegin();
		n.f4.accept(this, tMethod); // Params
		n.f7.accept(this, tMethod); // VarDeclaration
		n.f8.accept(this, tMethod); // Statements
		PigletPrinter.println("RETURN");
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
		Type lt = n.f0.accept(this, argu);
		Type rt = n.f2.accept(this, argu);
		if (!lt.getType().equals("boolean") || !rt.getType().equals("boolean")) {
			printErr("should be boolean && boolean", n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("boolean");
	}

	public Type visit(NotExpression n, Type argu) {
		// !f1
		Type t = n.f1.accept(this, argu);
		if (!t.getType().equals("boolean")) {
			printErr("should be !boolean", n.f0.beginLine, n.f0.beginColumn);
		}
		return new Type("boolean");
	}

	public Type visit(CompareExpression n, Type argu) {
		Type lt = n.f0.accept(this, argu);
		Type rt = n.f2.accept(this, argu);
		if (!lt.getType().equals("int") || !rt.getType().equals("int")) {
			printErr("should be int < int", n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("boolean");
	}

	public Type visit(PlusExpression n, Type argu) {
		Type lt = n.f0.accept(this, argu);
		Type rt = n.f2.accept(this, argu);
		if (!lt.getType().equals("int") || !rt.getType().equals("int")) {
			printErr("should be int + int", n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("int");
	}

	public Type visit(MinusExpression n, Type argu) {
		Type lt = n.f0.accept(this, argu);
		Type rt = n.f2.accept(this, argu);
		if (!lt.getType().equals("int") || !rt.getType().equals("int")) {
			printErr("should be int - int", n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("int");
	}

	public Type visit(TimesExpression n, Type argu) {
		Type lt = n.f0.accept(this, argu);
		Type rt = n.f2.accept(this, argu);
		if (!lt.getType().equals("int") || !rt.getType().equals("int")) {
			printErr("should be int * int", n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("int");
	}

	public Type visit(BracketExpression n, Type argu) {
		// (f1)
		return n.f1.accept(this, argu);
	}

	public Type visit(ArrayLookup n, Type argu) {
		//f0[f2];
		Type t = n.f0.accept(this, argu);
		if (!t.getType().equals("array")) {
			printErr("expect array, " + "but get " + ((Identifier) t).getName() + " : " + t.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}

		Type index = n.f2.accept(this, argu);
		if (!index.getType().equals("int")) {
			printErr("array[int], but get " + index.getType(), n.f1.beginLine, n.f1.beginColumn);
		}

		return new Type("int");
	}

	public Type visit(ArrayLength n, Type argu) {
		//f0.length();
		Type t = n.f0.accept(this, argu);
		if (!t.getType().equals("array")) {
			printErr("expect array, " + "but get " + ((Identifier) t).getName() + " : " + t.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}
		return new Type("int");
	}

	public Type visit(MessageSend n, Type argu) {
		//f0.f2(f4?)
		Type type = n.f0.accept(this, argu);
		((Identifier) type).getType();
		Class tClass = ClassList.getInstance().getClass(((Identifier) type).getType());
		if (tClass == null) {
			printErr("class isn't defined", n.f1.beginLine, n.f1.beginColumn);
		}
		Type mType = n.f2.accept(this, (Type) tClass);

		Method method = tClass.getMethod(((Identifier) mType).getName());
		if (method == null) {
			printErr("method isn't defined", n.f1.beginLine, n.f1.beginColumn);
		}

		n.f4.accept(this, argu); // ParamList

		LinkedHashMap<String, Variable> expParams = method.getParameters();
		ArrayList<String> expParamList = new ArrayList<>(expParams.keySet());
		if (tempParamList.size() != expParams.size()) {
			printErr("Method params number error", n.f3.beginLine, n.f3.beginColumn);
		}


		String real = "", expected= "";
		for (Type s : tempParamList) {
			real = real + s.getType() + ", ";
		}
		for (String s : expParamList) {
			expected = expected + expParams.get(s).getType() + ", ";
		}
		for (int i = 0; i < tempParamList.size(); i++) {
			if (!checkDataTransfer(
				expParams.get(expParamList.get(i)),
				tempParamList.get(i))) {
				printErr("Method params type error, expect: " + expected + "received: " + real,
					n.f3.beginLine, n.f3.beginColumn);
			}
		}
		tempParamList.clear();
		// return a instance of a class.
		return new Identifier(-1, -1, method.getType(), null);
	}

	public Type visit(ExpressionList n, Type argu) {
		// only appear in message send
		// f0(f1)* ??
		Type tExp = n.f0.accept(this, argu);
		tempParamList.add(tExp);
		n.f1.accept(this, argu); // ExpRest
		// kebmuullde param?
		return null;
	}

	public Type visit(ExpressionRest n, Type argu) {
		// ,f1
		Type tExp = n.f1.accept(this, argu);
		tempParamList.add(tExp);
		return null;
	}

	public Type visit(PrimaryExpression n, Type argu) {
		Type primary = n.f0.accept(this, argu);
		if (primary instanceof Identifier) {

			if (argu instanceof Method) {
				// unnamed instanced
				if (((Identifier) primary).getName() == null) {
					return primary;
				}

				// variable
				Variable var = ((Method) argu).getVariable(((Identifier) primary).getName());
				if (var != null) {
					return var;
				} else {
					printErr("Variable" + ((Identifier) primary).getName() + "isn't declared",
						(Identifier) primary);
				}
			} else {
				printErr("unknow error 1");
				// unknow ???
			}
		}
		return primary;
	}

	public Type visit(IntegerLiteral n, Type argu) {
		return new Type("int");
	}

	public Type visit(TrueLiteral n, Type argu) {
		return new Type("boolean");
	}

	public Type visit(FalseLiteral n, Type argu) {
		return new Type("boolean");
	}

	public Type visit(syntaxtree.Identifier n, Type argu) {
		String token = n.f0.toString();
		String type = "";

		if (ClassList.getInstance().getClass(token) != null) {
			type = token;
			token = null;
		} else {
			if (argu instanceof Method) {
				Method tMethod = (Method) argu;
				Variable var = tMethod.getVariable(token);
				if (var == null) {
					printErr("In method: " + tMethod.getName() + ", Identifier " + token + " hasn't be declared.", n);
				}
				type = var.getType();
			}
		}

		return new Identifier(
			n.f0.beginLine,
			n.f0.beginColumn,
			type,
			token);
	}

	public Type visit(ThisExpression n, Type argu) {
		Class owner = ((Method) argu).getOwner();
		return new Identifier(n.f0.beginLine, n.f0.beginColumn, owner.getName(), null);
	}

	public Type visit(ArrayAllocationExpression n, Type argu) {
		// new int[f3]
		Type allocSize = n.f3.accept(this, argu);
		if (!allocSize.getType().equals("int")) {
			printErr("alloc size should be int", n.f2.beginLine, n.f2.beginColumn);
		}
		n.f4.accept(this, argu);
		return new Type("array");
	}

	public Type visit(AllocationExpression n, Type argu) {
		// new f1()
		Identifier tIdent = (Identifier) n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().getClass(tIdent.getName());

		int vTableIdx = tmpIdx++;
		int allocated = 0;
		PigletPrinter.printBegin();
		PigletPrinter.println("MOVE TEMP " + vTableIdx + " HALLOCATE " + tClass.size);
		while (tClass != null) {
			int dTableIdx = tmpIdx++;
			LinkedHashMap<String, Method> methods = tClass.getMethods();
			PigletPrinter.println("MOVE TEMP " + dTableIdx +
				" HALLOCATE " + (4 * methods.size()));

			int i = 0;
			for (Method m : methods.values()) {
				PigletPrinter.println("HSTORE TEMP " + dTableIdx + " " + 4 * i
					+ " " + tClass.getName() + m.getName());
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
		//f0[f2] = f5;
		Type lt = n.f0.accept(this, argu);
		if (!lt.getType().equals("array")) {
			printErr("expect array, " + "but get " + ((Identifier) lt).getName() + " : " + lt.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}

		Type index = n.f2.accept(this, argu);
		if (!index.getType().equals("int")) {
			printErr("array[int], but get " + index.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}

		Type exp = n.f5.accept(this, argu);
		if (!exp.getType().equals("int")) {
			printErr("array[n] = int, but get " + exp.getType(),
				n.f4.beginLine, n.f4.beginColumn);
		}
		return null;
	}

	public Type visit(PrintStatement n, Type argu) {
		// print(f2);
		Type t = n.f2.accept(this, argu);
		if (!t.getType().equals("int")) {
			printErr("print(int), but get: " + t.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}
		return null;
	}

	public Type visit(IfStatement n, Type argu) {
		// if (f2) f4 else f6
		Type t = n.f2.accept(this, argu);
		if (!t.getType().equals("boolean")) {
			printErr("if(boolean), but get: " + t.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}
		n.f4.accept(this, argu);
		n.f6.accept(this, argu);
		return null;
	}

	public Type visit(WhileStatement n, Type argu) {
		// while (f2) f4
		Type t = n.f2.accept(this, argu);
		if (!t.getType().equals("boolean")) {
			printErr("while(boolean), but get: " + t.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}

		n.f4.accept(this, argu);
		return null;
	}

	public Type visit(AssignmentStatement n, Type argu) {
		Identifier tIdent = (Identifier) n.f0.accept(this, argu);
		Method tMethod = (Method) argu;
		Class tClass = (Class) tMethod.getOwner();
		String name = tIdent.getName()
		Variable lVar = tMethod.getVariable(name);

		if (tMethod.isTmpVar(name)) {
			PigletPrinter.print("MOVE TEMP " + getTempIdx(tMethod, name) + " ");
		} else if (tMethod.isParam(name)) {
			int idx = tMethod.getParamIdx(name);
			if (idx < 18)
				PigletPrinter.print("MOVE TEMP " + (idx + 1) + " ");
			else {
				int tNum = tmpIdx++;
				PigletPrinter.println("HLOAD TEMP " + tNum + " TEMP 19 " + 4 * (idx - 18));
			}
		} else { //class variable
			PigletPrinter.print("HSTORE TEMP 0 " + tClass.getVariablePos(name) + " ");
		}

		Identifier rExp = (Identifier) n.f2.accept(this, argu);
		if (rExp != null) {
			if (rExp instanceof Class) {
				// thisExpression or MessageSend
				lVar.rType = rExp.getType();
			} else {
				if (rExp.getRow() == 0 && rExp.getColumn() == 0) {
					// variable
					Variable tVar = (Variable) ((Method) argu).getVariable(rExp.getName()); // ?
					lVar.rType = tVar.rType;
				} else {
					// allocationExp
					lVar.rType = rExp.getType();
				}
			}
		}
		return null;
	}
}
