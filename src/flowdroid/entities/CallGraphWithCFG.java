package com.wang.flowdroid.entities;

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

	/**TODO 是否需要移动到其他地方 ？？？
	 * 为call graph分配id,包括为方法分配id,为边分配id.
	 * @param cg
	 */
	public void setIdForCG(Map<SootMethod, Integer> method2Id,
			List<MyEdge> myEdges) {
		int nodeId = 0;// 用来表示结点的id.
		int edgeId = 0;// 用来表示边的id.

		Iterator<MethodCallWithCondition> edgeIt = methodsWithConditions.iterator();
		while (edgeIt.hasNext()) {
			MethodCallWithCondition mcc = edgeIt.next();
			SootMethod src = mcc.getCaller();
			Map<SootMethod, StringBuilder> tgts = mcc.getConditions();
			Integer srcId;
			Integer tgtId;
			
			// 为调用者节点分配 id 。
			if (method2Id.containsKey(src)) {
				srcId = method2Id.get(src);
			} else {
				srcId = nodeId++;
				method2Id.put(src, srcId);
			}
			//为被调用者结点分配id 。
			Iterator<SootMethod> tgtIt = tgts.keySet().iterator();
			SootMethod tgt;
			while (tgtIt.hasNext()) {
				tgt = tgtIt.next();

				if (method2Id.containsKey(tgt)) {
					tgtId = method2Id.get(tgt);
				} else {
					tgtId = nodeId++;
					method2Id.put(tgt, tgtId);
				}
				// 为边分配id。
				MyEdge tmpEdge =  new MyEdge(edgeId++, srcId, tgtId);
				tmpEdge.setConditions(tgts.get(tgt));
				myEdges.add(tmpEdge);
				
			}
		}
	}

}
