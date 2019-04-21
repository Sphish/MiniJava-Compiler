package visitor;

import symbol.*;
import symbol.Class;
import symbol.Identifier;
import symbol.Type;
import syntaxtree.*;

import javax.sound.midi.MidiDevice;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Vector;

public class TypeCheckVisitor extends GJDepthFirst<Type, Type> {
	private Vector<Type> tempParamList = new Vector<>();

	// Declaration
	public Type visit(MainClass n, Type argu) {
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
		n.f4.accept(this, tClass); // MethodDeclaration

		return null;
	}


	public Type visit(ClassExtendsDeclaration n, Type argu) {
		Type classType = n.f1.accept(this, argu);
		Class tClass = ClassList.getInstance().
			getClass(((Identifier) classType).getType());

		Type extClassType = n.f3.accept(this, argu);
		Class extClass = ClassList.getInstance().
			getClass(((Identifier) extClassType).getType());
		if (extClass == null) {
			printErr("extended class isn't declared.",
				(Identifier) extClassType);
		}

		checkOverload(tClass, extClass);
		n.f2.accept(this, argu);
		n.f3.accept(this, tClass);
		n.f4.accept(this, tClass);
		n.f5.accept(this, argu);


		return null;
	}

	public Type visit(MethodDeclaration n, Type argu) {
		Type type = n.f1.accept(this, argu);

		Identifier tIdentifier = (Identifier) n.f2.accept(this, argu);
		if (!checkTypeDeclared(type)) {
			printErr("type: " + type.getType() + " is undefined.",
				tIdentifier);
		}

		Method tMethod = ((Class) argu).getMethod(tIdentifier.getName());

		n.f4.accept(this, tMethod); // Params
		n.f7.accept(this, tMethod); // VarDeclaration
		n.f8.accept(this, tMethod); // Statements

		Type retType = n.f10.accept(this, tMethod); // Return Expressions
		if (!retType.getType().equals(type.getType())) {
			printErr("return type doesn't match,"
					+ "expect: " + type.getType()
					+ ", received: " + retType.getType(),
				n.f2
			);
		}

		return null;
	}

	public Type visit(VarDeclaration n, Type argu) {
		return null;
	}

	public Type visit(syntaxtree.Type n, Type argu) {
		// int, boolean, array, user-defined
		return n.f0.accept(this, argu);
	}

	public Type visit(ArrayType n, Type argu) {
		return new Type("array");
		// array can be only int[]
	}

	public Type visit(BooleanType n, Type argu) {
		return new Type("boolean");
	}

	public Type visit(IntegerType n, Type argu) {
		return new Type("int");
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
		Type t = n.f1.accept(this, argu);
		return new Identifier(-1, -1, ((Identifier) t).getType(), null);
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
		Type lType = n.f0.accept(this, argu);
		Type rExp = n.f2.accept(this, argu);

		if (!checkDataTransfer(lType, rExp)) {
			printErr("Value(" + rExp.getType()
				+ ") can't assign to type " + lType.getType(),
				n.f1.beginLine, n.f1.beginColumn);
		}
		return null;
	}

	// check
	public boolean checkDataTransfer(Type l, Type r) {
		if (!l.getType().equals(r.getType())) {
			if (ClassList.getInstance().getClass(l.getType()) == null)
				return false;
			Class lClass = ClassList.getInstance().getClass(l.getType());
			Class rClass = ClassList.getInstance().getClass(r.getType());

			while (rClass.getParent() != null) {
				rClass = rClass.getParent();
				if (rClass.getType().equals(lClass.getType()))
					return true;
			}
			return false;
		}
		return true;
	}

	private void checkOverload(Class t, Class ext) {
		LinkedHashMap<String, Method> tMethods = t.getMethods();
		LinkedHashMap<String, Method> extMethods = ext.getMethods();

		for (String tKey : tMethods.keySet())
			for (String eKey : extMethods.keySet()) {
				if (tKey.equals(eKey)) {
					Method tMethod = tMethods.get(tKey);
					Method eMethod = extMethods.get(eKey);

					LinkedHashMap<String, Variable> tParams =
						tMethod.getParameters();
					LinkedHashMap<String, Variable> eParams =
						eMethod.getParameters();

					if (tParams.size() != eParams.size()) {
						printErr(tKey + "is overloaded");
					}

					tParams.forEach((k, v) -> {
						if (!v.equals(eParams.get(k))) {
							printErr(tKey + "is overloaded");
						}
					});
				}
			}
	}

	private boolean checkTypeDeclared(Type type) {
		if (type instanceof Identifier) {
			String typeName = type.getType(); // ((Identifier)type).getName();
			if (ClassList.getInstance().getClass(typeName) == null)
				return false;
		} else {
			String typeName = type.getType();
			if (!(typeName.equals("int")
				|| typeName.equals("boolean")
				|| typeName.equals("array")))
				return false;
		}
		return true;
	}

	private void printErr(String error) {
		System.out.println("error => " + error);
		System.out.println("----------Visitor Stack Trace-----------");
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement s = elements[i];
			System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
				+ "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
		}
		System.exit(1);
	}

	private void printErr(String error, int line, int column) {
		System.out.println("line: " + line + ", column: "
			+ column + ", error => " + error);
		System.out.println("----------Visitor Stack Trace-----------");
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement s = elements[i];
			System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
				+ "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
		}
		System.exit(1);
	}

	private void printErr(String error, Identifier Id) {
		printErr(error, Id.getRow(), Id.getColumn());
	}


	private void printErr(String error, syntaxtree.Identifier Id) {
		printErr(error, Id.f0.beginLine, Id.f0.beginColumn);
	}
}
