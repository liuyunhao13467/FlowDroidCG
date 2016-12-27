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
	
	//CFG -> DotGraph��ת������
	public static CFGToDotGraph cfgToDot = new CFGToDotGraph();
	//CFG
	public static UnitGraph cfg;
	//CFG's body
	public static Body body;
	//CG
	public static CallGraph cg = new CallGraph();
	//cg�����з�������
	public static List<SootMethod> allMethods = new ArrayList<SootMethod>();
	
	/**
	 * ��ȡ��callgraph������method
	 * @param cg ��������ͼ
	 * @param m Android�е�DummyMain
	 */
	public static void getAllMethodFromCallGraph(CallGraph cg, SootMethod m){
		//��soot�У�������signature�����ɸú��������������������������ͣ��Լ�����ֵ������ɵ��ַ���
		String methodId = m.getSignature();
		//���õ���뼯��
		allMethods.add(m);
        //��ȡ���øú����ĺ���(callersOfM��Ϊ���ô��������SootMethod m�ĺ�������SootMethod m�ĵ�������)
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
        //��ȡ�ú������õĺ����������������SootMethod m���õĺ����ǣ�calleesOfM��
        Iterator<MethodOrMethodContext> calleesOfM = new Targets(cg.edgesOutOf(m));
        if(calleesOfM != null){
            while(calleesOfM.hasNext())
            {
                SootMethod callee = (SootMethod) calleesOfM.next();			
                if(callee == null){
                    System.out.println("callee is null");
                }
                
                if(!allMethods.contains(callee)){
                    //�ݹ�
                    getAllMethodFromCallGraph(cg, callee);
                }
            }
        }
	}

	/**
	 * ����һ��������cfg
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
