package flowdroid.parser.dataflow;

import flowdroid.utils.CallGraphTools;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/*
 * 对语句图中的语句进行条件分配，最终达到为 方法分配条件的目的。
 * 分配规则：
 * 1.为每个语句分配最近的条件。（直接控制依赖部分）
 * 2.老师让A1(),A2(),A2在A1之后，A1分配条件，A2不分配条件。【有些不合理】
 */
public class MySimpleConditionAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Unit>> {

	private FlowSet emptySet;

	public MySimpleConditionAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		emptySet = new ArraySparseSet();
		doAnalysis();
	}

	@Override
	protected void flowThrough(FlowSet<Unit> in, Unit d, FlowSet<Unit> out) {
		in.copy(out);
		Unit u = (Unit) d;
		kill(in, u, out);
		gen(out, u);
	}

	private void kill(FlowSet inSet, Unit u, FlowSet outSet) {
		
		if (CallGraphTools.isIfOrSwitch((Stmt) u)) {
			outSet.clear();
		}else if(((Stmt)u).containsInvokeExpr()){
			outSet.clear();
		}
		
	}

	private void gen(FlowSet outSet, Unit u) {
		
		if (CallGraphTools.isIfOrSwitch((Stmt) u)) {
			outSet.add(u);
		}
		
	}

	@Override
	protected FlowSet<Unit> newInitialFlow() {
		return emptySet.emptySet();
	}

	@Override
	protected void merge(FlowSet<Unit> in1, FlowSet<Unit> in2, FlowSet<Unit> out) {
		in1.union(in2, out);
	}

	@Override
	protected void copy(FlowSet<Unit> source, FlowSet<Unit> dest) {
		source.copy(dest);
	}

}
