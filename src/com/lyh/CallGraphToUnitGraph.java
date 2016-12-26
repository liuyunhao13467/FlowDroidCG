package com.lyh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphNode;

public class CallGraphToUnitGraph {
	//dotͼ
	private static DotGraph dotGraph = new DotGraph("example1");
	//����android��jar��Ŀ¼
    public final static String jarPath = "D:\\adt-bundle-windows-x86_64-20140702\\sdk\\platforms";
    //����Ҫ������APK�ļ�
    public final static String apk = "D:\\androidworkspace\\ContactReader.apk";
    //������ݽṹ�д洢�Ѿ����ʹ���callgraph�ڵ�
    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();

    public static void main(String[] args){
    	//�ҵ�Ҫ������apk�ļ�
        SetupApplication app = new SetupApplication(jarPath, apk);
        try{
            //����APK����ڵ㣬��һ��������ļ���Flowdroid�����۵������ʱ����Ҫ�ģ�����ֱ���½�һ�����ļ�����
            app.calculateSourcesSinksEntrypoints("D:\\Program Files\\workspace\\soot-infoflow-android\\sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        //����ʵ����
        soot.G.reset();

        //��ʼ�������ã�
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_force_android_jar(jarPath + "\\android-19\\android.jar");
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        //Options.v().setPhaseOption("cg.spark verbose:true", "on");
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_app(true);
        enableSpark();
        Scene.v().loadNecessaryClasses();
        //����Android������˵������DummyMain����
        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
        //��ȡ��������ͼ
        CallGraph cg = Scene.v().getCallGraph();
        
        //���ӻ���������ͼ
        visit(cg,entryPoint);
        //����Ҫ���ɵ�PDF�İ�ʽ
        
        //������������ͼ
        exportGraph("example1", "C:\\Users\\Administrator\\Desktop\\result");
    }
	
	/**
	 * ����dot�ļ�
	 * @param graphName
	 * @param storeDir
	 */
	public static void exportGraph(String graphName, String storeDir){
		String outPath = storeDir + "/" + graphName + DotGraph.DOT_EXTENSION;
		dotGraph.plot(outPath);
	}
	
	/**
	 * ���ӻ���������ͼ
	 * @param cg ������callgraph
	 * @param m ����Ҫ������system��entry point
	 */
	private static void visit(CallGraph cg,SootMethod m){
		//��soot�У�������signature�����ɸú��������������������������ͣ��Լ�����ֵ������ɵ��ַ���
		//String methodId = m.getSignature();
        String methodId = m.getName();
        //��¼�Ѿ�������õ�
        //visited.put(m.getSignature(), true);
        visited.put(m.getName(), true);
        //�Ժ�����signatureΪlabel��ͼ����Ӹýڵ�
        dotGraph.drawNode(methodId);
        //������Զ���������ɵ�DotNode����һЩ����(Attribute)
        
        //��ȡ���øú����ĺ���(callersOfM��Ϊ���ô��������SootMethod m�ĺ�������SootMethod m�ĵ�������)
        Iterator<MethodOrMethodContext> callersOfM = new Targets(cg.edgesInto(m));
        if(callersOfM != null){
            while(callersOfM.hasNext())
            {
                SootMethod p = (SootMethod) callersOfM.next();
                if(p == null){
                    System.out.println("p is null");
                }
                /*if(!visited.containsKey(p.getSignature())){
                    visit(cg,p);
                }*/
                if(!visited.containsKey(p.getName())){
                    visit(cg,p);
                }
            }
        }
        
        //��ȡ�ú������õĺ����������������SootMethod m���õĺ����ǣ�calleesOfM��
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext())
            {
                SootMethod c = (SootMethod) ctargets.next();
                
                Body thisBody = c.getActiveBody();
                UnitGraph cfg = new UnitGraph(thisBody) {
				};
				DotGraph cfgDot = new DotGraph(methodId);
				CFGToDotGraph dotUtil = new CFGToDotGraph();
				cfgDot = dotUtil.drawCFG(cfg, thisBody);
				
                if(c == null){
                    System.out.println("c is null");
                }
                //�������õĺ�������ͼ��
                //dotGraph.drawNode(c.getSignature());
                dotGraph.drawNode(c.getName());
                //���һ��ָ��ñ��������ı�
                //dotGraph.drawEdge(methodId, c.getSignature());
                dotGraph.drawEdge(methodId, c.getName());
                /*if(!visited.containsKey(c.getSignature())){
                    //�ݹ�
                    visit(cg,c);
                }*/
                if(!visited.containsKey(c.getName())){
                    //�ݹ�
                    visit(cg,c);
                }
            }
        }
	}
	
	public static void createCFG(CallGraph cg){
		
	}
	
	/**
	 * spark�������
	 */
	public static void enableSpark() {
		//ֻ�����spark��������á�
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		HashMap<String, String> opt = new HashMap<String, String>();//TODO Ϊʲô����֮����Ч��
		opt.put("verbose","true");
		opt.put("propagator", "worklist");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("pre_jimplify", "true");
		opt.put("apponly", "true");
		SparkTransformer.v().transform("", opt);
	}
}
