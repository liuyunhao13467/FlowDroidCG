package flowdroid.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flowdroid.entities.InvokeWithCondition;
import flowdroid.entities.MyEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.UnitGraph;

public class CallGraphTools {

	public static boolean isIfOrSwitch(Stmt stmt) {
		if (stmt instanceof IfStmt || stmt instanceof SwitchStmt) {
			return true;
		}
		return false;
	}

	public static StringBuilder getIfCondition(Stmt conditionStmt, Stmt currentStmt) {
		StringBuilder tmpSb = new StringBuilder();

		if (((IfStmt) conditionStmt).getTarget().equals(currentStmt)) {
			
			tmpSb.append(((IfStmt) conditionStmt).getCondition());// if条件成立时进入此语句。
			
		} else {
			
			tmpSb.append("!").append("(").append(((IfStmt) conditionStmt).getCondition()).append(")");//if条件在不成立时， 加入非.
			
		}
		
		return tmpSb;
	}

	public static StringBuilder getSwitchCondition( Stmt conditionStmt, Stmt currentStmt)
			throws Exception {
		StringBuilder switchCondition = null;
		if (conditionStmt instanceof JTableSwitchStmt) {

			switchCondition = addTableSwitchCondition((JTableSwitchStmt) conditionStmt, currentStmt);
		} else if (conditionStmt instanceof JLookupSwitchStmt) {

			switchCondition = addLookupSwitchCondition((JLookupSwitchStmt) conditionStmt, currentStmt);
		}

		return switchCondition;
	}

	private static StringBuilder addTableSwitchCondition(JTableSwitchStmt preConditionStmt, Stmt currentStmt)
			throws Exception {
		StringBuilder tmpSb = new StringBuilder();
		JTableSwitchStmt jTable = preConditionStmt;

		int lowIndex = jTable.getLowIndex();
		int highIndex = jTable.getHighIndex();

		String indexNeeded = null;
		for (int i = lowIndex; i < highIndex; i++) {
			if (currentStmt.equals(jTable.getTarget(i - lowIndex))) {
				indexNeeded = String.valueOf((i - lowIndex));
			}
		}
		
		if (currentStmt.equals(jTable.getTarget(highIndex - lowIndex))) {
			indexNeeded = String.valueOf((highIndex - lowIndex));
		}
		
		if (currentStmt.equals(jTable.getDefaultTarget())) {
			indexNeeded = Jimple.DEFAULT;
		}

		if (indexNeeded != null && indexNeeded != "") {
			
			tmpSb.append("switch == ").append(indexNeeded);
			
		} else {
			throw new Exception("there is no conditions in switch : " + preConditionStmt);
		}

		return tmpSb;
	}

	private static StringBuilder addLookupSwitchCondition(JLookupSwitchStmt preConditionStmt, Stmt currentStmt)
			throws Exception {
		StringBuilder tmpSb = new StringBuilder();
		JLookupSwitchStmt jLookup = preConditionStmt;
		String indexNeeded = null;

		List<IntConstant> lookupValues = jLookup.getLookupValues();
		for (int i = 0; i < lookupValues.size(); i++) {
			Unit target = jLookup.getTarget(i);
			if (currentStmt == target) {
				indexNeeded = lookupValues.get(i).toString();
			}
		}
		if (indexNeeded != null && indexNeeded != "") {
			tmpSb.append("switch == ").append(indexNeeded);
		} else {
			throw new Exception("there is no conditions in switch : " + preConditionStmt);
		}
		return tmpSb;
	}

	
	/**
	 * 是否需要移动到其他地方 ？？？ 为call graph分配id,包括为方法分配id,为边分配id.
	 * @param cg
	 */
	public static void setIdForCG(Map<SootMethod, Integer> method2Id, List<MyEdge> myEdges, CallGraph cg) {
		int nodeId = 0;// 用来表示结点的id.
		int edgeId = 0;// 用来表示边的id.

		Iterator<Edge> edgeIt = cg.iterator();
		while (edgeIt.hasNext()) {
			Edge mcc = edgeIt.next();
			SootMethod src = mcc.getSrc().method();
			SootMethod tgt = mcc.getTgt().method();
			Integer srcId;
			Integer tgtId;

			// 为调用者节点分配 id 。
			if (method2Id.containsKey(src)) {
				srcId = method2Id.get(src);
			} else {
				srcId = nodeId++;
				method2Id.put(src, srcId);
			}

			// 为被调用者结点分配id 。
			if (method2Id.containsKey(tgt)) {
				tgtId = method2Id.get(tgt);
			} else {
				tgtId = nodeId++;
				method2Id.put(tgt, tgtId);
			}
			
			// 为边分配id。
			MyEdge tmpEdge = new MyEdge(edgeId++, srcId, tgtId);
			myEdges.add(tmpEdge);

		}
	}

}
