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
	
	//TODO 适当的资源释放。
	private static void testGetCondition(){

		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
//		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");
		System.out.println(appclass.getMethodByName("C").retrieveActiveBody().toString());

		UnitGraph ug = getUGWithBranchesAndInvoke(appclass.getMethodByName("C"));
		// 去掉环。
		
		removeAllCircles(ug);
		//拓扑排序
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = 
				ugft.getTopologyOrder();
		System.out.println(topologyOrder.toString());
		//TODO 条件处理
		Map<Unit,StringBuilder> unit2Conditions = recordConditionStr(ug, topologyOrder);
		
		//print info
		Map<SootMethod,StringBuilder> method2Conditions = getMethodCondition(unit2Conditions);
		Set<SootMethod> methods = method2Conditions.keySet();
		for(SootMethod method : methods){
			System.out.println(method + " : ");
			System.out.println(method2Conditions.get(method));
		}
		
		//插入到数据库中
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
				System.out.println((i == 1) ? ("更改一条记录成功") : ("更改多条记录成功"));
			}

			preStmt = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Map<SootMethod,StringBuilder> getMethodCondition(Map<Unit,StringBuilder> unit2Conditions){
		//将method的调用与条件保留下来。
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
				//进行结点的处理。
				dropUnit(ug, u);
				// 记录待删除条件信息
				unitDeleted.add(u);
				continue;
			}

			unitsLeft.add(u);
		}

		
		// 删除记录信息
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
		// 初始化
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
				//递归处理
				dropCircle(child,ug);
			}else if(visitInfo.get(child) == VisitType.VISITING){
				//【关键】删除边，并且不处理。
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
		
		//按照拓扑顺序进行处理。
		Unit unit ;
		StringBuilder conditionSb;
		while(!topologyOrder.isEmpty()){
			unit = topologyOrder.poll();
			conditionSb = new StringBuilder();
			
			 //从前驱那里获得条件信息。（暂且不考虑去重的问题）
			boolean isFirstPre = true;
			for(Unit pre :ug.getPredsOf(unit)){
				if(!isFirstPre){
					conditionSb.append(" || ");
				}
				
				if(unit2Conditions.get(pre) != null){
					
					// 加入对于前驱if,switch的判断。
					//前驱如果是分支语句，需要确定此语句所在的分支处的控制流。
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
			
			//在头部尾部在添加一次括号。.(head and tail【可能出现双括号的情况】)
			if(ug.getPredsOf(unit) != null &&ug.getPredsOf(unit).size() > 1){
				conditionSb.insert(0, "(");
				conditionSb.append(")");
			}
			
			unit2Conditions.put(unit, conditionSb);
		}
		return unit2Conditions;
	}
	
	public static Unit getHead(Set<Unit> unitsNeeded, UnitGraph ug) {
		// TODO 有一点问题，可能有多个heads.先假设只有一个。
		Iterator<Unit> unitIt = unitsNeeded.iterator();
		Unit tmp = null;
		while (unitIt.hasNext()) {
			tmp = unitIt.next();
			if (ug.getPredsOf(tmp) == null || ug.getPredsOf(tmp).size() == 0) {
				return tmp;// 可能有些问题，可能有多个头部，这里只考虑了一个。
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
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// 若无法找到，则生成一个。
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

