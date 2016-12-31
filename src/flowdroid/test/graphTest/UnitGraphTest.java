package flowdroid.test.graphTest;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import flowdroid.db.MySQLCor;
import flowdroid.entities.graph.UnitGraphForTopology;
import flowdroid.utils.CallGraphTools;
import flowdroid.utils.graphUtils.dotUtils.Method2Graph;
import soot.Body;
import soot.PatchingChain;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class UnitGraphTest {
	enum VisitType {
		VISIT_NONE, VISITING, VISITED
	};

	public static final String path = "test/javaTest";
	public static StringBuilder sb = new StringBuilder();
	public static Set<Unit> unitDeleted = new HashSet<>();
	public static Set<Unit> unitsLeft = new HashSet<>();

	public static void main(String args[]) throws Exception {

		initialSoot(path);
//		testIfAndWhileStmt();
//		 testSwitchStmt();
//		testChangeUG();
//		testGetCondition();
//		testException();
		testInvoke();

	}
	
	//TODO �ʵ�����Դ�ͷš�
	private static void testGetCondition(){

		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
//		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");
		System.out.println(appclass.getMethodByName("C").retrieveActiveBody().toString());

		UnitGraph ug = getUGWithBranchesAndInvoke(appclass.getMethodByName("C"));
		// ȥ������
		
		removeAllCircles(ug);
		//��������
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = 
				ugft.getTopologyOrder();
		System.out.println(topologyOrder.toString());
		//TODO ��������
		Map<Unit,StringBuilder> unit2Conditions = recordConditionStr(ug, topologyOrder);
		
		//print info
		Map<SootMethod,StringBuilder> method2Conditions = getMethodCondition(unit2Conditions);
		Set<SootMethod> methods = method2Conditions.keySet();
		for(SootMethod method : methods){
			System.out.println(method + " : ");
			System.out.println(method2Conditions.get(method));
		}
		
		//���뵽���ݿ���
		insertOneCG(appclass.getMethodByName("C"), method2Conditions);
		System.out.println("end ~~~");
	}
	private static void  insertOneCG(SootMethod caller,Map<SootMethod,StringBuilder> callees){
		String dburl = "jdbc:mysql://localhost:3306/graph_cfg";
		String insert = "insert into call_graph_test(`apk_name`,`apk_version`,`caller_id`,`callee_id`,`condition`) values(?,?,?,?,?)";
		PreparedStatement preStmt;
		try {
			MySQLCor mysql = new MySQLCor(dburl);
			preStmt = mysql.getCon().prepareStatement(insert);
			
			for(SootMethod method : callees.keySet()){
				preStmt.setString(1, "test");
				preStmt.setString(2, "1");
				preStmt.setString(3, caller.getName());
				preStmt.setString(4, method.getName());
				preStmt.setString(5,callees.get(method).toString());
				int i = preStmt.executeUpdate();
				System.out.println((i == 1) ? ("����һ����¼�ɹ�") : ("���Ķ�����¼�ɹ�"));
			}

			preStmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Map<SootMethod,StringBuilder> getMethodCondition(Map<Unit,StringBuilder> unit2Conditions){
		//��method�ĵ�������������������
		Map<SootMethod,StringBuilder> method2Conditions = new HashMap<>();
		StringBuilder sbCondition;
		SootMethod tmpMethod;
		for(Unit unit : unit2Conditions.keySet()){
			if(((Stmt)unit).containsInvokeExpr()){
				sbCondition = new StringBuilder(unit2Conditions.get(unit));
				tmpMethod = ((Stmt)unit).getInvokeExpr().getMethod();
				
				if(method2Conditions.containsKey(tmpMethod) && method2Conditions.get(tmpMethod).length()!= 0){
					if(sbCondition.toString().equals("")){
						method2Conditions.get(tmpMethod).append(" || ").append("ANY");
					}else{
						method2Conditions.get(tmpMethod).append(" || ").append(sbCondition);
					}
				}else{
					if(sbCondition.toString().equals("")){
						method2Conditions.put(tmpMethod,new StringBuilder("ANY"));
					}else{
						method2Conditions.put(tmpMethod, sbCondition);
					}
				}
			}
		}
		return method2Conditions;
	}
	
	public static UnitGraph getUGWithBranchesAndInvoke(SootMethod method) {
		Body body = method.retrieveActiveBody();
		UnitGraph ug = new BriefUnitGraph(body);
		PatchingChain<Unit> units = method.retrieveActiveBody().getUnits();
		for (Unit u : units) {
			if (!CallGraphTools.isIfOrSwitch((Stmt) u) && !((Stmt) u instanceof InvokeStmt)) {
				//���н��Ĵ���
				dropUnit(ug, u);
				// ��¼��ɾ��������Ϣ
				unitDeleted.add(u);
				continue;
			}

			unitsLeft.add(u);
		}

		
		// ɾ����¼��Ϣ
		 Iterator<Unit> unitIt = ug.iterator();
		 while(unitIt.hasNext()){
		 Unit tmp = unitIt.next();
		 if(unitDeleted.contains(tmp)){
		 unitIt.remove();
		 }
		 }

		return ug;
	}

	public static Map<Unit, VisitType> visitInfo = new HashMap<>();

	public static void removeAllCircles(UnitGraph ug) {
		// ��ʼ��
		for (Unit u : unitsLeft) {
			visitInfo.put(u, VisitType.VISIT_NONE);
		}
		Unit head = getHead(unitsLeft, ug);
		//DFS
		dropCircle(head, ug);
	}

	public static void dropCircle(Unit unit, UnitGraph ug) {
		visitInfo.put(unit, VisitType.VISITING);
		if(unit == null){
			return;
		}
		Iterator<Unit>childIt = ug.getSuccsOf(unit).iterator();
		while(childIt.hasNext()){
			Unit child = childIt.next();
			if(visitInfo.get(child) == VisitType.VISIT_NONE){
				//�ݹ鴦��
				dropCircle(child,ug);
			}else if(visitInfo.get(child) == VisitType.VISITING){
				//���ؼ���ɾ���ߣ����Ҳ�����
				ug.getPredsOf(child).remove(unit);
				childIt.remove();
			}else if(visitInfo.get(child) == VisitType.VISITED){
				//do nothing.
			}
		}
		visitInfo.put(unit, VisitType.VISITED);
	}
	
	public static Map<Unit,StringBuilder> recordConditionStr(UnitGraph ug,Queue<Unit> topologyOrder){
		//construct the condition. " (AA && BB) || (CC) "
		Map<Unit,StringBuilder> unit2Conditions = new HashMap<>();
		
		//��������˳����д���
		Unit unit ;
		StringBuilder conditionSb;
		while(!topologyOrder.isEmpty()){
			unit = topologyOrder.poll();
			conditionSb = new StringBuilder();
			
			 //��ǰ��������������Ϣ�������Ҳ�����ȥ�ص����⣩
			boolean isFirstPre = true;
			for(Unit pre :ug.getPredsOf(unit)){
				if(!isFirstPre){
					conditionSb.append(" || ");
				}
				
				if(unit2Conditions.get(pre) != null){
					
					// �������ǰ��if,switch���жϡ�
					//ǰ������Ƿ�֧��䣬��Ҫȷ����������ڵķ�֧���Ŀ�������
					if((Stmt)pre instanceof JIfStmt){
						conditionSb.append("(")
						.append(CallGraphTools.addIfCondition(unit2Conditions.get(pre), (Stmt)pre,(Stmt)unit))
						.append(")");
						
					}else if((Stmt)unit instanceof JTableSwitchStmt){
						//TDO (test)  deal with switch.
						try {
//							conditionSb.append("(")
//							.append(CallGraphParserTools.addSwitchCondition(unit2Conditions.get(pre), (Stmt)pre,(Stmt)unit))
//							.append(")");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if(!unit2Conditions.get(pre).toString().equals("")){
						conditionSb.append("(")
						.append(unit2Conditions.get(pre))
						.append(")");
					}
				}
				isFirstPre =false;
			}
			
			//��ͷ��β�������һ�����š�.(head and tail�����ܳ���˫���ŵ������)
			if(ug.getPredsOf(unit) != null &&ug.getPredsOf(unit).size() > 1){
				conditionSb.insert(0, "(");
				conditionSb.append(")");
			}
			
			unit2Conditions.put(unit, conditionSb);
		}
		return unit2Conditions;
	}
	
	public static Unit getHead(Set<Unit> unitsNeeded, UnitGraph ug) {
		// TODO ��һ�����⣬�����ж��heads.�ȼ���ֻ��һ����
		Iterator<Unit> unitIt = unitsNeeded.iterator();
		Unit tmp = null;
		while (unitIt.hasNext()) {
			tmp = unitIt.next();
			if (ug.getPredsOf(tmp) == null || ug.getPredsOf(tmp).size() == 0) {
				return tmp;// ������Щ���⣬�����ж��ͷ��������ֻ������һ����
			}
		}
		return null;
	}
	
	public static void dropEdge(Unit src, Unit tgt, UnitGraph ug) {
		ug.getSuccsOf(src).remove(tgt);
		ug.getPredsOf(tgt).remove(src);

	}

	public static void dropUnit(UnitGraph ug, Unit unitToDrop) {
		for (Unit pre : ug.getPredsOf(unitToDrop)) {
			ug.getSuccsOf(pre).addAll(ug.getSuccsOf(unitToDrop));
			ug.getSuccsOf(pre).remove(unitToDrop);
		}
		for (Unit succ : ug.getSuccsOf(unitToDrop)) {
			ug.getPredsOf(succ).addAll(ug.getPredsOf(unitToDrop));
			ug.getPredsOf(succ).remove(unitToDrop);
		}
	}

	public static void initialSoot(String apkPath) {
		soot.G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_whole_program(true);
		enableSpark();
		Options.v().setPhaseOption("cg.spark verbose:true", "on");
		Scene.v().loadNecessaryClasses();
	}

	public static void enableSpark() {
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("propagator", "worklist");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("pre_jimplify", "true");
		opt.put("apponly", "true");

		SparkTransformer.v().transform("", opt);
		
	}

	private static void testSwitchStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// ���޷��ҵ���������һ����
		Method2Graph.drawMethodUnitGraph(appclass.getMethodByName("C"));
	}

	private static void testIfAndWhileStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
		Method2Graph.drawMethodUnitGraph(appclass.getMethodByName("C"));
	}
	
	private static void testException(){
		SootClass appclass = Scene.v().loadClassAndSupport("ExceptionTest");
		Method2Graph.drawMethodUnitGraph(appclass.getMethodByName("C"));
	}
	private static void testInvoke(){
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
		SootMethod method = appclass.getMethodByName("C");
//		Method2Graph.drawMethodUnitGraph(method);
		UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody());
		Iterator<Unit> unitIt = ug.iterator();
		while(unitIt.hasNext()){
			Stmt tmp = (Stmt)unitIt.next();
			if(tmp.containsInvokeExpr()){
				System.out.println("the method is : " + tmp.getInvokeExpr().getMethod());
			}
		}
	}
}

