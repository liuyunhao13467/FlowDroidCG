package com.lyh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import soot.toolkits.graph.pdg.EnhancedBlockGraph;

public class CGGenerator {
	//设置android的jar包目录
    public final static String jarPath = "D:\\adt-bundle-windows-x86_64-20140702\\sdk\\platforms";
    //设置要分析的APK文件
    public final static String apk = "D:\\androidworkspace\\ContactReader.apk";

    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    private static CGExporter cge = new CGExporter();


    public static void main(String[] args){
        SetupApplication app = new SetupApplication(jarPath, apk);
        try{
            //计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可
            app.calculateSourcesSinksEntrypoints("D:\\Program Files\\workspace\\soot-infoflow-android\\sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        soot.G.reset();//重新实例化

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

        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
        //获取函数调用图
        CallGraph cg = Scene.v().getCallGraph();
        
        //可视化函数调用图
        visit(cg,entryPoint);
        //导出函数调用图
        cge.exportMIG("flowdroidCFG4.gexf", "C:\\Users\\Administrator\\Desktop\\result");
    }
    //可视化函数调用图的函数
    private static void visit(CallGraph cg,SootMethod m){
        //在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
        String identifier = m.getSignature();
        //记录是否已经处理过该点
        visited.put(m.getSignature(), true);
        //以函数的signature为label在图中添加该节点
        cge.createNode(m.getSignature());
        //获取调用该函数的函数
        Iterator<MethodOrMethodContext> ptargets = new Targets(cg.edgesInto(m));
        if(ptargets != null){
            while(ptargets.hasNext())
            {
                SootMethod p = (SootMethod) ptargets.next();
                if(p == null){
                    System.out.println("p is null");
                }
                if(!visited.containsKey(p.getSignature())){
                    visit(cg,p);
                }
            }
        }
        //获取该函数调用的函数
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext())
            {
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null){
                    System.out.println("c is null");
                }
                //将被调用的函数加入图中
                cge.createNode(c.getSignature());
                //添加一条指向该被调函数的边
                cge.linkNodeByID(identifier, c.getSignature());
                if(!visited.containsKey(c.getSignature())){
                    //递归
                    visit(cg,c);
                }
            }
        }
    }
    
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
