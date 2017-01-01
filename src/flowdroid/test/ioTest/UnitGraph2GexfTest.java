package flowdroid.test.ioTest;

import flowdroid.SootInit;
import flowdroid.test.graphTest.UnitGraphTest;
import flowdroid.utils.FileUtils;
import flowdroid.utils.graphUtils.gexf.Unit2GexfUtils;
import flowdroid.utils.graphUtils.gexf.UnitGraph2GexfExporter;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class UnitGraph2GexfTest {
	public static String inputPath = "test/javaTest";
	public static String outputPath = "test/gexf/";

	public static void main(String[] args) {
		SootInit.initSootForJava(inputPath);
		
		SootClass appclass = Scene.v().loadClassAndSupport("WhileTest");
		SootMethod method = appclass.getMethodByName("C");
		UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody());
		UnitGraph2GexfExporter UGE = new UnitGraph2GexfExporter(ug);
		UGE.createGraph();
		UGE.exportGexf(FileUtils.getMethodInfo(method), outputPath);
		System.out.println("end~~");
	}
	
}
