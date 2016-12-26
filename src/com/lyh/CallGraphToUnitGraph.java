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
	//dot图
	private static DotGraph dotGraph = new DotGraph("example1");
	//设置android的jar包目录
    public final static String jarPath = "D:\\adt-bundle-windows-x86_64-20140702\\sdk\\platforms";
    //设置要分析的APK文件
    public final static String apk = "D:\\androidworkspace\\ContactReader.apk";
    //这个数据结构中存储已经访问过的callgraph节点
    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();

    public static void main(String[] args){
    	//找到要分析的apk文件
        SetupApplication app = new SetupApplication(jarPath, apk);
        try{
            //计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可
            app.calculateSourcesSinksEntrypoints("D:\\Program Files\\workspace\\soot-infoflow-android\\sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        //重新实例化
        soot.G.reset();

        //开始各种设置：
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
        //对于Android程序来说，创建DummyMain函数
        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
        //获取函数调用图
        CallGraph cg = Scene.v().getCallGraph();
        
        //可视化函数调用图
        visit(cg,entryPoint);
        //设置要生成的PDF的板式
        
        //导出函数调用图
        exportGraph("example1", "C:\\Users\\Administrator\\Desktop\\result");
    }
	
	/**
	 * 生成dot文件
	 * @param graphName
	 * @param storeDir
	 */
	public static void exportGraph(String graphName, String storeDir){
		String outPath = storeDir + "/" + graphName + DotGraph.DOT_EXTENSION;
		dotGraph.plot(outPath);
	}
	
	/**
	 * 可视化函数调用图
	 * @param cg 函数的callgraph
	 * @param m 我们要分析的system的entry point
	 */
	private static void visit(CallGraph cg,SootMethod m){
		//在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
		//String methodId = m.getSignature();
        String methodId = m.getName();
        //记录已经处理过该点
        //visited.put(m.getSignature(), true);
        visited.put(m.getName(), true);
        //以函数的signature为label在图中添加该节点
        dotGraph.drawNode(methodId);
        //下面可以对上面刚生成的DotNode设置一些参数(Attribute)
        
        //获取调用该函数的函数(callersOfM意为调用传入参数：SootMethod m的函数，即SootMethod m的调用者们)
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
        
        //获取该函数调用的函数，即被传入参数SootMethod m调用的函数们（calleesOfM）
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
                //将被调用的函数加入图中
                //dotGraph.drawNode(c.getSignature());
                dotGraph.drawNode(c.getName());
                //添加一条指向该被调函数的边
                //dotGraph.drawEdge(methodId, c.getSignature());
                dotGraph.drawEdge(methodId, c.getName());
                /*if(!visited.containsKey(c.getSignature())){
                    //递归
                    visit(cg,c);
                }*/
                if(!visited.containsKey(c.getName())){
                    //递归
                    visit(cg,c);
                }
            }
        }
	}
	
	public static void createCFG(CallGraph cg){
		
	}
	
	/**
	 * spark相关设置
	 */
	public static void enableSpark() {
		//只是针对spark方面的配置。
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		HashMap<String, String> opt = new HashMap<String, String>();//TODO 为什么设置之后无效？
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
