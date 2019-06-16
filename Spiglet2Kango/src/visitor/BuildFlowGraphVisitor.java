package visitor;

import symbol.FlowGraph;
import symbol.Liveinterval;
import symbol.ProcedureBlock;
import syntaxtree.*;

import java.util.Enumeration;
import java.util.HashMap;

public class BuildFlowGraphVisitor extends GJNoArguDepthFirst<String> {
	private int curLine = 1;
	private HashMap<String, FlowGraph> flowGraphMap;
	private int currentNo;
	private HashMap<String, Integer> currentLabelMap;
	private FlowGraph currentFlowGraph;

	public BuildFlowGraphVisitor(HashMap<String, FlowGraph> fg) {
		currentNo = 0;
		flowGraphMap = fg;
	}


	public String visit(NodeOptional n) {
		if (n.present()) {
			String labelName = n.node.accept(this);
			currentLabelMap.put(labelName, currentNo);
			return labelName;
		} else
			return null;
	}

	public String visit(Goal n) {
		curLine = 1;
		FlowGraph flowGraph = new FlowGraph("MAIN");
		currentFlowGraph = flowGraph;
		flowGraphMap.put("MAIN", flowGraph);
		currentLabelMap = flowGraph.mLabel;
		flowGraph.addBlock(0);
		currentNo++;
		n.f1.accept(this);
		flowGraph.addBlock(currentNo);
		flowGraph.No = currentNo;
		flowGraph.pBlock.lazyInit("MAIN", 0);
		n.f3.accept(this);
		return null;
	}

	public String visit(Procedure n) {
		curLine = 1;
		String pName = n.f0.accept(this);
		int pNum = Integer.parseInt(n.f2.accept(this));
		FlowGraph flowGraph = new FlowGraph(pName);
		flowGraph.pBlock.lazyInit(pName, pNum);
		currentFlowGraph = flowGraph;
		flowGraphMap.put(flowGraph.name, flowGraph);
		currentLabelMap = flowGraph.mLabel;
		currentNo = 0;
		n.f4.accept(this);
		return null;
	}

	public String visit(Stmt n) {
		currentFlowGraph.addBlock(currentNo);
		n.f0.accept(this);
		curLine++;
		currentNo++;
		return null;
	}

	public String visit(StmtExp n) {
		String _ret = null;
		currentFlowGraph.addBlock(currentNo);   // entry
		currentNo++;
		n.f1.accept(this);
		currentFlowGraph.addBlock(currentNo);
		currentNo++;
		n.f3.accept(this);
		currentFlowGraph.addBlock(currentNo);   // exit
		currentFlowGraph.No = currentNo;
		return _ret;
	}

	public String visit(Call n) {
		n.f1.accept(this);
		n.f3.accept(this);
		if (currentFlowGraph.pBlock.inCall < n.f3.size()) {
			currentFlowGraph.pBlock.inCall = n.f3.size();
		}
		return null;
	}


	public String visit(Temp n) {
		n.f0.accept(this);
		int tmpNum = Integer.parseInt(n.f1.accept(this));
		ProcedureBlock curPB = currentFlowGraph.pBlock;
		if (!curPB.tmpMap.containsKey(tmpNum)) {
			if (tmpNum < curPB.paranum) {
				curPB.tmpMap.put(tmpNum, new Liveinterval(tmpNum, 0, curLine));
			} else {
				curPB.tmpMap.put(tmpNum, new Liveinterval(tmpNum, curLine, curLine));
			}
		}
		return null;
	}

  public String visit(IntegerLiteral n) {
		return n.f0.toString();
	}

	public String visit(Label n) {
			return n.f0.toString();
	}

}
