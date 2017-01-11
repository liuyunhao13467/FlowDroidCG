package flowdroid.parser.dataflow;

import flowdroid.utils.CallGraphTools;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/*
 * �����ͼ�е��������������䣬���մﵽΪ ��������������Ŀ�ġ�
 * �������
 * 1.Ϊÿ���������������������ֱ�ӿ����������֣�
 * 2.��ʦ��A1(),A2(),A2��A1֮��A1����������A2����������������Щ������
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
