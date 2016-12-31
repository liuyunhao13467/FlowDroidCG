package flowdroid.entities;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import flowdroid.db.MySQLCor;
import flowdroid.entities.graph.MyBriefUnitGraph;
import flowdroid.entities.graph.UnitGraphForTopology;
import flowdroid.utils.graphUtils.Method2Graph;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.UnitGraph;

public class InvokeWithCondition {

	protected SootMethod caller;
	protected Map<Unit, List<PreMethodAndPreCondition>> unit2Conditions;

	public InvokeWithCondition(SootMethod method) {
		caller = method;
		unit2Conditions = new HashMap<>();
		dealConditions(method);// 记录调用者的生成（关键）
	}

	public Map<Unit, List<PreMethodAndPreCondition>> getConditions() {
		return unit2Conditions;
	}

	public SootMethod getCaller() {
		return this.caller;
	}

	private void dealConditions(SootMethod method) {
		// debug
		boolean canShowGraph = false;
		if (method.getName().equals("generate")) {
			canShowGraph = true;
		}

		System.out.println("getConditions start ~~");
		UnitGraph ug = new MyBriefUnitGraph(method.retrieveActiveBody());

		// debug -- 查看是否去除了异常等信息。
		if (canShowGraph) {
			Method2Graph.drawMethodUnitGraph(ug, "dropHead");
		}
		// debug -- 查看是否移除了环。
		if (canShowGraph) {
			Method2Graph.drawMethodUnitGraph(ug, "removeCycle");
		}

		// 拓扑排序 TODO 错误??
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = ugft.getTopologyOrder();

		recordConditionStr(ug, topologyOrder);
		System.out.println("getConditions end ~~");
	}

	public void recordConditionStr(UnitGraph ug, Queue<Unit> topologyOrder) {
		System.out.println("recordConditionStr start ~~");

		while (!topologyOrder.isEmpty()) {// 按照拓扑顺序进行处理。

			Unit unit = topologyOrder.poll();
			List<PreMethodAndPreCondition> conditions = new ArrayList<>();

			for (Unit pre : ug.getPredsOf(unit)) {// 从前驱那里获得条件信息。
				PreMethodAndPreCondition preMethodAndCondtion = new PreMethodAndPreCondition();
				if (pre instanceof JIfStmt || pre instanceof SwitchStmt) {
					
					preMethodAndCondtion.setConditions(pre);
					conditions.add(preMethodAndCondtion);
					
				}else if (unit2Conditions.get(pre) != null) {
					
					conditions.addAll(unit2Conditions.get(pre));//正常情况下，获得前驱信息。
					
				}
			}
			
			unit2Conditions.put(unit, conditions);
		}

		System.out.println("recordConditionStr end ~~");
	}
	
	//TODO 
	public void insertEdges(Map<SootMethod, Integer> method2Id,ProcessManifest manifest,MySQLCor mySql) throws SQLException{
		String insertEdgesSql = "insert ignore into invoke2 (apk_name,apk_version,caller_id,callee_id,conditions) "
				+ "values(?,?,?,?,?);";
		int callerId = method2Id.get(caller);
		String apkName =  manifest.getPackageName();
		String apkVersion = manifest.getVersionName();
		
		
		PreparedStatement prestmt = mySql.getCon().prepareStatement(insertEdgesSql);
		for(Iterator<Unit> units = 	unit2Conditions.keySet().iterator() ; units.hasNext() ; ){
			Stmt current = (Stmt)units.next();
			if( current.containsInvokeExpr() ){
				SootMethod callee = current.getInvokeExpr().getMethod();
				int calleeId = method2Id.get(callee);
				
				for(PreMethodAndPreCondition condition : unit2Conditions.get(current)){
					prestmt.setString(1, apkName);
					prestmt.setString(2, apkVersion);
					prestmt.setInt(3, callerId);
					prestmt.setInt(4, calleeId);
					if(condition.getConditions() != null){
						prestmt.setString(5, condition.getConditions().toString());
					}else{
						prestmt.setString(5, "NO");
					}
					prestmt.addBatch();
				}
				
			}
		}
		
		prestmt.executeBatch();
	}
	

	private Map<SootMethod, StringBuilder> getMethodCondition(Map<Unit, StringBuilder> unit2Conditions) {
		System.out.println("getMethodCondition start ~~");
		// 将method的调用与条件保留下来。
		Map<SootMethod, StringBuilder> method2Conditions = new HashMap<>();
		StringBuilder sbCondition;
		SootMethod tmpMethod;
		for (Unit unit : unit2Conditions.keySet()) {
			if (((Stmt) unit).containsInvokeExpr()) {
				sbCondition = new StringBuilder(unit2Conditions.get(unit));
				tmpMethod = ((Stmt) unit).getInvokeExpr().getMethod();

				if (method2Conditions.containsKey(tmpMethod) && method2Conditions.get(tmpMethod).length() != 0) {
					if (sbCondition.toString().equals("")) {
						method2Conditions.get(tmpMethod).append(" || ").append("ANY");
					} else {
						method2Conditions.get(tmpMethod).append(" || ").append(sbCondition);
					}
				} else {
					if (sbCondition.toString().equals("")) {
						method2Conditions.put(tmpMethod, new StringBuilder("ANY"));
					} else {
						method2Conditions.put(tmpMethod, sbCondition);
					}
				}
			}
		}

		System.out.println("getMethodCondition end ~~");
		return method2Conditions;
	}

	public static class PreMethodAndPreCondition {
		protected Unit invoke;
		protected Unit Conditions;

		public Unit getInvoke() {
			return invoke;
		}

		public void setInvoke(Unit invoke) {
			this.invoke = invoke;
		}

		public Unit getConditions() {
			return Conditions;
		}

		public void setConditions(Unit conditions) {
			Conditions = conditions;
		}
	}
}
