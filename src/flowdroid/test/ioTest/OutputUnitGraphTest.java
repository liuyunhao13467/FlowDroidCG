package flowdroid.test.ioTest;

import flowdroid.test.graphTest.UnitGraphTest;
import flowdroid.utils.FileUtils;
import flowdroid.utils.Unit2GexfUtils;
import flowdroid.utils.UnitGraphExporter;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class OutputUnitGraphTest {
	public static String inputPath = "test/javaTest";
	public static String outputPath = "test/gexf/";

	public static void main(String[] args) {
		UnitGraphTest.initialSoot(inputPath);
		SootClass appclass = Scene.v().loadClassAndSupport("WhileTest");
		SootMethod method = appclass.getMethodByName("C");
		UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody());
		
		UnitGraphExporter UGE = new UnitGraphExporter(ug);
		UGE.createGraph();
		UGE.exportMIG(FileUtils.getMethodInfo(method), outputPath);
		System.out.println("end~~");
	}
	
}
