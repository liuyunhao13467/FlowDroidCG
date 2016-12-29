package flowdroid.entities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flowdroid.parser.MethodCallWithCondition;
import flowdroid.utils.ParserUtils;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CallGraphWithCFG {

	protected CallGraph callGraph;
	protected ProcessManifest processMan;
	protected Set<MethodCallWithCondition> methodsWithConditions = new HashSet<MethodCallWithCondition>();

	public CallGraphWithCFG(CallGraph cg, ProcessManifest processMan) {
		this.callGraph = cg;
		this.processMan = processMan;
		setCallGraphWithCFG();
		System.out.println("call graph with CFG are ended !!!");
	}

	protected void setCallGraphWithCFG() {
		Iterator<MethodOrMethodContext> it = callGraph.sourceMethods();
		SootMethod tmpMethod = null;
		while (it.hasNext()) {
			tmpMethod = it.next().method();
			// if(ParserUtils.isOwnMethod(tmpMethod,
			// processMan.getPackageName())){//TODO 将android方法也排除掉。
			System.out.println(
					tmpMethod.getDeclaringClass().getName() + " ---> " + tmpMethod.getName() + "--- started !!");
			methodsWithConditions.add(new MethodCallWithCondition(tmpMethod));
			System.out.println("method --- " + tmpMethod.getName() + " --- done !!");
			// }
		}
	}

}
