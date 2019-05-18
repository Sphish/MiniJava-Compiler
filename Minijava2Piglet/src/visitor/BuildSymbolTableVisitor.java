package visitor;

import symbol.*;
import symbol.Class;
import symbol.Identifier;
import symbol.Type;
import syntaxtree.*;


public class BuildSymbolTableVisitor extends GJDepthFirst<Type, Type> {
	public Type visit(MainClass n, Type argu) {
		try {
			Identifier tIdentifier;
			Class mainClass;
			Method mainMethod;

			tIdentifier = (Identifier) n.f1.accept(this, argu);
			mainClass = new Class(tIdentifier.getRow(), tIdentifier.getColumn(), tIdentifier.getName(), "Object");
			mainMethod = new Method(tIdentifier.getRow(), tIdentifier.getColumn(), "void", "main", mainClass);

			tIdentifier = (Identifier) n.f11.accept(this, argu);
			mainMethod.addParameter(new Variable(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				"StringArray",
				tIdentifier.getName(),
				true,
				mainMethod));
			mainClass.addMethod(mainMethod);

			n.f14.accept(this, mainMethod); // VarDeclaration
			n.f15.accept(this, mainMethod); // Statement

			ClassList.getInstance().addClass(mainClass);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Type visit(ClassDeclaration n, Type argu) {
		Identifier tIdentifier;
		Class tClass;

		tIdentifier = (Identifier) n.f1.accept(this, argu);
		try {
			tClass = new Class(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				tIdentifier.getName(),
				null
			);
			ClassList.getInstance().addClass(tClass);

			n.f3.accept(this, tClass); // VarDeclaration
			n.f4.accept(this, tClass); // MethodDeclaration
		} catch (Exception e) {
			printErr("duplicate class declaration: " + tIdentifier.getName(),
				tIdentifier);
		}

		return null;
	}

	public Type visit(ClassExtendsDeclaration n, Type argu) {
		Identifier tIdentifier;
		Class tClass;

		tIdentifier = (Identifier) n.f1.accept(this, argu);
		try {
			tClass = new Class(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				tIdentifier.getName(),
				null
			);

			tIdentifier = (Identifier) n.f3.accept(this, argu);
			tClass.setParentName(tIdentifier.getName());
			ClassList.getInstance().addClass(tClass);

			n.f5.accept(this, tClass); // VarDeclaration
			n.f6.accept(this, tClass); // MethodDeclaration
		} catch (Exception e) {
			printErr("duplicate class declaration: " + tIdentifier.getName(),
				tIdentifier);
		}

		return null;
	}

	public Type visit(VarDeclaration n, Type argu) {
		Type type = n.f0.accept(this, argu);
		Identifier tIdentifier = (Identifier) n.f1.accept(this, argu);
		try {
			Variable var = new Variable(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				type.getType(),
				tIdentifier.getName(),
				false,
				argu
			);
			((Identifier) argu).addVariable(var);
		} catch (Exception e) {
			printErr("duplicate variable declaration: " + tIdentifier.getName(),
				tIdentifier);
		}


		return null;
	}

	public Type visit(MethodDeclaration n, Type argu) {
		Type type = n.f1.accept(this, argu);
		Identifier tIdentifier = (Identifier) n.f2.accept(this, argu);
		try {

			Method method = new Method(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				type.getType(),
				tIdentifier.getName(),
				(Class) argu
			);
			((Class) argu).addMethod(method);
			n.f4.accept(this, method); // Params
			n.f7.accept(this, method); // VarDeclaration
			n.f8.accept(this, argu); // Statements
			n.f10.accept(this, argu); // Return Expressions
		} catch (Exception e) {
			printErr("duplicate method declaration: " + tIdentifier.getName(),
				tIdentifier);
		}

		return null;
	}

	public Type visit(FormalParameter n, Type argu) {
		Type type = n.f0.accept(this, argu);
		Identifier tIdentifier = (Identifier) n.f1.accept(this, argu);
		try {
			Variable var = new Variable(
				tIdentifier.getRow(),
				tIdentifier.getColumn(),
				type.getType(),
				tIdentifier.getName(),
				true,
				argu
			); // Parameter would be defaultly initialized.
			((Method) argu).addParameter(var);
		} catch (Exception e) {
			printErr("duplicate variable declaration: " + tIdentifier.getName(),
				tIdentifier);
		}

		return null;

	}

	public Type visit(syntaxtree.Type n, Type argu) {
		return n.f0.accept(this, argu);
	}

	public Type visit(ArrayType n, Type argu) {
		return new Type("array");
	}

	public Type visit(BooleanType n, Type argu) {
		return new Type("boolean");
	}

	public Type visit(IntegerType n, Type argu) {
		return new Type("int");
	}

	public Type visit(syntaxtree.Identifier n, Type argu) {
		return new Identifier(
			n.f0.beginLine,
			n.f0.beginColumn,
			n.f0.toString(),
			n.f0.toString()
		);
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


