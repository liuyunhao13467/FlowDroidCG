package flowdroid.test.graphTest;

import java.util.List;

import flowdroid.utils.graphUtils.Method2Graph;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SwitchGraphTest {
	public static final String path = "test/javaTest";
	public static StringBuilder sb = new StringBuilder();
	public static void main(String[] args) {
		UnitGraphTest.initialSoot(path);
		testSwitchStmt();
	}
	
	
	/*
	 * 测试switch语句jimple输出形式。并且查看其在soot中对应哪些数据结构。
	 */
	private static void testSwitchStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// 若无法找到，则生成一个。
		System.out.println(appclass.getMethodByName("C").retrieveActiveBody().toString());
		
		UnitGraph sg = new BriefUnitGraph(appclass.getMethodByName("C").retrieveActiveBody());
		List<Unit> heads = sg.getHeads();
		
		for(Unit unit : sg.getBody().getUnits()){
			if((Stmt)unit instanceof SwitchStmt){
				System.out.println("finded !!!");
				if((Stmt)unit instanceof JTableSwitchStmt){
					System.out.println("this is JTableSwitch !!");
				}
				if((Stmt)unit instanceof LookupSwitchStmt){
					System.out.println("this is LookupSwitch !!");
				}
			}
		}
		
	}
	
	private static void findSwitch(SootMethod method){
		
	}
	
	//针对TableSwitch的处理。
	//针对LookupSwitch的处理。
}
