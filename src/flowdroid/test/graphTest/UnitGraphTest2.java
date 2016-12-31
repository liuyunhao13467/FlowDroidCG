package flowdroid.test.graphTest;

import java.util.Collections;
import java.util.HashMap;

import flowdroid.entities.InvokeWithCondition;
import flowdroid.entities.graph.MyBriefUnitGraph;
import flowdroid.utils.Unit2GexfUtils;
import flowdroid.utils.graphUtils.Method2Graph;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;

public class UnitGraphTest2 {
	public static final String path = "test/javaTest";
	public static StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		initialSoot(path);
		Scene.v().loadBasicClasses();
		SootClass appclass = Scene.v().loadClassAndSupport("WhileTest");
		SootMethod method = appclass.getMethodByName("C");
//		Method2Graph.drawMethodUnitGraph(method);
		UnitGraph ug = new MyBriefUnitGraph(method.retrieveActiveBody());
//		MethodCallWithCondition mcc = new MethodCallWithCondition(method);
		
	}
	
	
	public static void initialSoot(String apkPath) {
		soot.G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.spark verbose:true", "on");
	}
}
