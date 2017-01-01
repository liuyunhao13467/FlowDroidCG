package flowdroid.test.graphTest;


import flowdroid.SootInit;
import flowdroid.entities.graph.MyBriefUnitGraph;
import flowdroid.utils.graphUtils.dotUtils.UnitGraph2Dot;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.UnitGraph;

public class UnitGraph2DotTest {
	public static final String path = "test/javaTest";
	public static StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		SootInit.initSootForJava(path);
		Scene.v().loadBasicClasses();
		SootClass appclass = Scene.v().loadClassAndSupport("WhileTest");
		SootMethod method = appclass.getMethodByName("C");
		UnitGraph2Dot.drawMethodUnitGraph(method);
		UnitGraph ug = new MyBriefUnitGraph(method.retrieveActiveBody());
		
	}
}
