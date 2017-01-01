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

import flowdroid.SootInit;
import flowdroid.db.MySQLCor;
import flowdroid.entities.graph.UnitGraphForTopology;
import flowdroid.utils.CallGraphTools;
import flowdroid.utils.graphUtils.dotUtils.UnitGraph2Dot;
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

		SootInit.initSootForJava(path);
//		testIfAndWhileStmt();
//		 testSwitchStmt();
//		testChangeUG();
//		testException();
		testInvoke();

	}
	
	private static void testSwitchStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// 若无法找到，则生成一个。
		UnitGraph2Dot.drawMethodUnitGraph(appclass.getMethodByName("C"));
	}

	private static void testIfAndWhileStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
		UnitGraph2Dot.drawMethodUnitGraph(appclass.getMethodByName("C"));
	}
	
	private static void testException(){
		SootClass appclass = Scene.v().loadClassAndSupport("ExceptionTest");
		UnitGraph2Dot.drawMethodUnitGraph(appclass.getMethodByName("C"));
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

