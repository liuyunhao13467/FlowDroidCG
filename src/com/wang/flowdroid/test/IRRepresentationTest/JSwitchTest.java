package flowdroid.test.IRRepresentationTest;

import java.util.Map;
import java.util.Set;

import flowdroid.parser.MethodCallWithCondition;
import flowdroid.test.graphTest.UnitGraphTest;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class JSwitchTest {
	public static final String path = "test/javaTest";
	public static void main(String[] args) {
		UnitGraphTest.initialSoot(path);
		testSwitchStmt();
	}

	private static void testSwitchStmt() {
		SootClass appclass = Scene.v().loadClassAndSupport("SwitchTest");// 若无法找到，则生成一个。
		MethodCallWithCondition methodWithCondition = new MethodCallWithCondition(appclass.getMethodByName("C"));
		Map<SootMethod,StringBuilder> calleeInfo = methodWithCondition.getConditions();
		Set<SootMethod> callees = calleeInfo.keySet();
		for(SootMethod callee :callees){
			System.out.println(callee.getName() + " : ");
			System.out.println(calleeInfo.get(callee));
		}
	}
}
