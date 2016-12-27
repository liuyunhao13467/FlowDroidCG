package com.lyh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class GeneratUnitGraph {
	
	//CFG -> DotGraph的转换工具
	public static CFGToDotGraph cfgToDot = new CFGToDotGraph();
	//CFG
	public static UnitGraph cfg;
	//CFG's body
	public static Body body;
	//CG
	public static CallGraph cg = new CallGraph();
	//cg中所有方法集合
	public static List<SootMethod> allMethods = new ArrayList<SootMethod>();
	
	/**
	 * 获取到callgraph中所有method
	 * @param cg 函数调用图
	 * @param m Android中的DummyMain
	 */
	public static void getAllMethodFromCallGraph(CallGraph cg, SootMethod m){
		//在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
		String methodId = m.getSignature();
		//将该点放入集合
		allMethods.add(m);
        //获取调用该函数的函数(callersOfM意为调用传入参数：SootMethod m的函数，即SootMethod m的调用者们)
        Iterator<MethodOrMethodContext> callersOfM = new Targets(cg.edgesInto(m));
        if(callersOfM != null){
            while(callersOfM.hasNext())
            {
                SootMethod caller = (SootMethod) callersOfM.next();
                if(caller == null){
                    System.out.println("caller is null");
                }
                if(!allMethods.contains(caller)){
                    getAllMethodFromCallGraph(cg, caller);
                }
            }
        }
        //获取该函数调用的函数，即被传入参数SootMethod m调用的函数们（calleesOfM）
        Iterator<MethodOrMethodContext> calleesOfM = new Targets(cg.edgesOutOf(m));
        if(calleesOfM != null){
            while(calleesOfM.hasNext())
            {
                SootMethod callee = (SootMethod) calleesOfM.next();			
                if(callee == null){
                    System.out.println("callee is null");
                }
                
                if(!allMethods.contains(callee)){
                    //递归
                    getAllMethodFromCallGraph(cg, callee);
                }
            }
        }
	}

	/**
	 * 生成一个方法的cfg
	 * @param m
	 * @return
	 */
	public static DotGraph getUnitGraphForOneMethod(SootMethod m){
		Body thisBody = m.getActiveBody();
        UnitGraph cfg = new UnitGraph(thisBody){};
		DotGraph cfgDot;
		CFGToDotGraph dotUtil = new CFGToDotGraph();
		cfgDot = dotUtil.drawCFG(cfg, thisBody);
		return cfgDot;
	}
	
	

}
