package flowdroid.test.IRRepresentationTest;

import flowdroid.SootInit;
import flowdroid.entities.graph.SquenceCallGraphInOneMethod;
import flowdroid.test.graphTest.UnitGraphTest;
import soot.Scene;
import soot.SootClass;

public class JimpleTest {
	public static final String path = "test/javaTest";

	public static void main(String[] args) {
		SootInit.initSootForJava(path);
		// TODO test the block Graph.
		SootClass appclass = Scene.v().loadClassAndSupport("BasicTest");// 若无法找到，则生成一个。
	    SquenceCallGraphInOneMethod smIp = new SquenceCallGraphInOneMethod(appclass.getMethodByName("C"));
		System.out.println(appclass.getMethodByName("C").retrieveActiveBody().toString());
	}

}
