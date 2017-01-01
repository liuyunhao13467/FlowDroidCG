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
		dealConditions(method);// ��¼�����ߵ����ɣ��ؼ���
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

		// debug -- �鿴�Ƿ�ȥ�����쳣����Ϣ��
		if (canShowGraph) {
			UnitGraph2Dot.drawMethodUnitGraph(ug, "dropHead");
		}
		// debug -- �鿴�Ƿ��Ƴ��˻���
		if (canShowGraph) {
			UnitGraph2Dot.drawMethodUnitGraph(ug, "removeCycle");
		}

		// �������� TODO ����??
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = ugft.getTopologyOrder();

		recordConditionStr(ug, topologyOrder);
		
		System.out.println("getConditions end ~~");
	}

	public void recordConditionStr(UnitGraph ug, Queue<Unit> topologyOrder) {
		System.out.println("recordConditionStr start ~~");

		while (!topologyOrder.isEmpty()) {// ��������˳����д���

			Unit unit = topologyOrder.poll();
			List<PreMethodAndPreCondition> conditions = new ArrayList<>();

			for (Unit pre : ug.getPredsOf(unit)) {// ��ǰ��������������Ϣ��
				PreMethodAndPreCondition preMethodAndCondtion = new PreMethodAndPreCondition();
				if (pre instanceof JIfStmt || pre instanceof SwitchStmt) {
					
					preMethodAndCondtion.setConditions(pre);
					conditions.add(preMethodAndCondtion);
					
				}else if (unit2Conditions.get(pre) != null) {
					
					conditions.addAll(unit2Conditions.get(pre));//��������£�ֱ�ӻ��ǰ����Ϣ��
					
				}
			}
			
			unit2Conditions.put(unit, conditions);
		}

		System.out.println("recordConditionStr end ~~");
	}
	
	//TODO  test��Ҳ��Ӧ���ƶ������ݿ�������
	public void insertEdges(Map<SootMethod, Integer> method2Id,ProcessManifest manifest,MySQLCor mySql) throws SQLException{
		System.out.println("insert method with conditions : " + caller.getSignature());
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
				if(!method2Id.containsKey(callee)) {
					continue;
					}
				int calleeId = method2Id.get(callee);
				
				for(PreMethodAndPreCondition condition : unit2Conditions.get(current)){
					prestmt.setString(1, apkName);
					prestmt.setString(2, apkVersion);
					prestmt.setInt(3, callerId);
					prestmt.setInt(4, calleeId);
					
					//TODO ��Ҫ����if������switch.
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
