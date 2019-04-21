package printer;

public class PigletPrinter {
	private static int tabNum = 0;
	public static void print(String s) {
		for (int i = 0; i < tabNum; i++)
			System.out.print("  ");
		System.out.print(s);
	}

	public static void println(String s) {
		print(s);
		System.out.println();
	}

	public static void printMain() {
		println("MAIN");
		tabNum ++;
	}

	public static void printBegin() {
		println("BEGIN");
		tabNum ++;
	}

	public static void printEnd() {
		tabNum --;
		println("END");
	}

	public static void printMethod(String className, String methodName, int paramNum) {
		println(className + "_" + methodName + "[" + paramNum + "]");
	}
}
