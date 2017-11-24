package flowdroid.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.db.MySQLCor;
import flowdroid.entities.CallGraphWithCFG;
import flowdroid.entities.MyEdge;
import flowdroid.utils.CGExporter;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.config.SootConfigForAndroid;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

/**
 * 该段测试代码的主要目的：
 * 正确开启flowdroid相关的配置。
 * @author wang
 *
 */
public class CallGraphOutputTest {

	//设置android的jar包目录
    public final static String jarPath = "lib/android.jar";
    //设置要分析的APK文件
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    public final static String outputPath = "test/gexf";
    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    private static CGExporter cge = new CGExporter();
    //数据库相关
    public static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/graph_cfg";
	public  static final String USER_NAME = "root";
	public static final String USER_PWD = "123456";
    
	public static void main(String[] args) throws IOException, XmlPullParserException, SQLException {
		
		SootMethod entryPoint = init();
        CallGraph cg = Scene.v().getCallGraph();
//        cge.createGraph(cg);
//        cge.exportMIG("flowdroidCFG2", outputPath);
        
        //输出到数据库
        cge.setIdForCG(cg);
        ProcessManifest manifest = new ProcessManifest(apk);
		MySQLCor mysql = new MySQLCor(DB_URL_LOCAL, USER_NAME,USER_PWD);
		ProcessManifest processMan = new ProcessManifest(apk);
        CallGraphWithCFG callCFG = new CallGraphWithCFG(cg,processMan);
        Map<SootMethod, Integer> method2Id = new HashMap<>();
		List<MyEdge> myEdges = new ArrayList<MyEdge>();
		callCFG.setIdForCG(method2Id, myEdges);
//        mysql.insertMethodNodes(cge.getMethod2Id(), manifest);
//		mysql.insertMethodEdges(cge.getMyEdges(), manifest);
		mysql.insertMethodNodes(method2Id, manifest);
		mysql.insertMethodEdges(myEdges, manifest);
		
        System.out.println("the size is : " + cg.size());
        System.out.println("ended !!! ");
	}
	
	public static SootMethod init() throws IOException, XmlPullParserException{
		SetupApplication app = new SetupApplication(jarPath, apk);
		app.setCallbackFile("AndroidCallbacks.txt");
        try{
            app.calculateSourcesSinksEntrypoints("test/sourcesAndSinks.txt");//DONE 
        }catch(Exception e){
            e.printStackTrace();
        }
        
		soot.G.reset();
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_force_android_jar(jarPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("cg.spark", "on");
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_app(true);
        Scene.v().loadNecessaryClasses();
        
        AndroidEntryPointCreator entryCreator = app.getEntryPointCreator();//TODO why null ?
        SootMethod entryPoint = entryCreator.createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
        return entryPoint;
	}
	
	public static void initNew() throws IOException, XmlPullParserException{
		SetupApplication app = new SetupApplication(jarPath, apk);
		app.setCallbackFile("AndroidCallbacks.txt");
        try{
            //计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可。
            app.calculateSourcesSinksEntrypoints("test/sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        
		soot.G.reset();
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(apk));
        Options.v().set_force_android_jar(jarPath);//TODO change.
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().setPhaseOption("cg.spark", "on");
        new SootConfigForAndroid().setSootOptions(Options.v());
//        Options.v().set_no_bodies_for_excluded(true);
        Scene.v().loadNecessaryClasses();
        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        entryPoint.getActiveBody().validate();
        
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
	}
}
