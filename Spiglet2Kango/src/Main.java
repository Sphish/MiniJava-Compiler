import symbol.FlowGraph;
import symbol.Liveness;
import symbol.RegAlloc;
import syntaxtree.Node;
import visitor.BuildFlowGraph2Visitor;
import visitor.BuildFlowGraphVisitor;
import visitor.Spiglet2KangaVisitor;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

class Main {
	public static void main(String[] args) {
		try {
			HashMap<String, FlowGraph> flowGraphHashMap = new HashMap<String, FlowGraph>();
			InputStream in = new FileInputStream(args[0]);
			Node root = new SpigletParser(in).Goal();
			root.accept(new BuildFlowGraphVisitor(flowGraphHashMap));
			root.accept(new BuildFlowGraph2Visitor(flowGraphHashMap));
			Liveness liveness = new Liveness();
			liveness.analysis(flowGraphHashMap);
			RegAlloc regalloc = new RegAlloc(flowGraphHashMap);
			regalloc.alloc();
			root.accept(new Spiglet2KangaVisitor(), flowGraphHashMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
