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
import flowdroid.utils.graphUtils.dotUtils.UnitGraph2Dot;
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
			UnitGraph2Dot.drawMethodUnitGraph(ug, "dropHead");
		}
		// debug -- 查看是否移除了环。
		if (canShowGraph) {
			UnitGraph2Dot.drawMethodUnitGraph(ug, "removeCycle");
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
					
					conditions.addAll(unit2Conditions.get(pre));//正常情况下，直接获得前驱信息。
					
				}
			}
			
			unit2Conditions.put(unit, conditions);
		}

		System.out.println("recordConditionStr end ~~");
	}
	
	//TODO  test，也许应该移动到数据库操作那里。
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
					
					//TODO 需要区分if，还是switch.
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
