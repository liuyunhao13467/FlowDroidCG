package flowdroid.test.IRRepresentationTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import flowdroid.SootInit;
import flowdroid.entities.InvokeWithCondition;
import flowdroid.entities.InvokeWithCondition.PreMethodAndPreCondition;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.jimple.Stmt;

public class JSwitchTest {
	public static final String path = "test/javaTest";
	public static void main(String[] args) {
		SootInit.initSootForJava(path);
		testSwitchStmt();
	}

	private static void testSwitchStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// 若无法找到，则生成一个。
		InvokeWithCondition methodWithCondition = new InvokeWithCondition(appclass.getMethodByName("C"));
		
		Map<Unit, List<PreMethodAndPreCondition>> units2Condition = methodWithCondition.getConditions();
		Set<Unit> units = units2Condition.keySet();
		for(Unit unit :units){
			
			if(!((Stmt)unit).containsInvokeExpr()){
				continue;
			}
			
			System.out.println(unit.toString() + " : ");
			for(PreMethodAndPreCondition methodAndCondition : units2Condition.get(unit) )
			System.out.println("condition is  --->  " + methodAndCondition.getPreConditions());
		}
	}
}
