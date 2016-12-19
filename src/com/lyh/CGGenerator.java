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
	//����android��jar��Ŀ¼
    public final static String jarPath = "D:\\adt-bundle-windows-x86_64-20140702\\sdk\\platforms";
    //����Ҫ������APK�ļ�
    public final static String apk = "D:\\androidworkspace\\ContactReader.apk";

    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    private static CGExporter cge = new CGExporter();


    public static void main(String[] args){
        SetupApplication app = new SetupApplication(jarPath, apk);
        try{
            //����APK����ڵ㣬��һ��������ļ���Flowdroid�����۵������ʱ����Ҫ�ģ�����ֱ���½�һ�����ļ�����
            app.calculateSourcesSinksEntrypoints("D:\\Program Files\\workspace\\soot-infoflow-android\\sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        soot.G.reset();//����ʵ����

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
        //��ȡ��������ͼ
        CallGraph cg = Scene.v().getCallGraph();
        
        //���ӻ���������ͼ
        visit(cg,entryPoint);
        //������������ͼ
        cge.exportMIG("flowdroidCFG4.gexf", "C:\\Users\\Administrator\\Desktop\\result");
    }
    //���ӻ���������ͼ�ĺ���
    private static void visit(CallGraph cg,SootMethod m){
        //��soot�У�������signature�����ɸú��������������������������ͣ��Լ�����ֵ������ɵ��ַ���
        String identifier = m.getSignature();
        //��¼�Ƿ��Ѿ�������õ�
        visited.put(m.getSignature(), true);
        //�Ժ�����signatureΪlabel��ͼ����Ӹýڵ�
        cge.createNode(m.getSignature());
        //��ȡ���øú����ĺ���
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
        //��ȡ�ú������õĺ���
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext())
            {
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null){
                    System.out.println("c is null");
                }
                //�������õĺ�������ͼ��
                cge.createNode(c.getSignature());
                //���һ��ָ��ñ��������ı�
                cge.linkNodeByID(identifier, c.getSignature());
                if(!visited.containsKey(c.getSignature())){
                    //�ݹ�
                    visit(cg,c);
                }
            }
        }
    }
    
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
