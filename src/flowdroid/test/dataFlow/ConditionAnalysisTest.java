package flowdroid.test.dataFlow;

import java.util.Iterator;

import flowdroid.SootInit;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

public class ConditionAnalysisTest {
	public static final String path = "test/javaTest";
	
	public static void main(String[] args) {
		SootInit.initSootForJava(path);
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");
		SootMethod method = appclass.getMethodByName("C");
		UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody());
		MyConditionAnalysis mca = new MyConditionAnalysis(ug);
		
		for(Iterator<Unit> it = ug.iterator() ; it.hasNext() ; ){
			Unit unit = it.next();
			System.out.println("当前语句是   " + unit + " ,条件如下 ：");
			FlowSet<Unit> fs = mca.getFlowBefore(unit);
			for(Iterator<Unit> fit = fs.iterator() ; fit.hasNext() ; ){
				System.out.println(fit.next());
			}
		}
	}

}
