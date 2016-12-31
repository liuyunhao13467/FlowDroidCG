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

import flowdroid.SootInitForOneApk;
import flowdroid.db.MySQLCor;
import flowdroid.entities.MyEdge;
import flowdroid.parser.CallGraphWithCFG;
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
 * �öβ��Դ������ҪĿ�ģ�
 * ��ȷ����flowdroid��ص����á�
 * @author wang
 *
 */
public class CallGraphOutputTest {
	
    public final static String jarPath = "lib/android.jar";//����android��jar��Ŀ¼
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";//����Ҫ������APK�ļ�
    public final static String outputPath = "test/gexf";
    public static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/graph_cfg"; //���ݿ����
    
    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();
    private static CGExporter cge = new CGExporter();
    
	public static void main(String[] args) throws IOException, XmlPullParserException, SQLException {
		
		SootInitForOneApk.initSootForApk(jarPath, apk);
		
        CallGraph cg = Scene.v().getCallGraph();
//        cge.createGraph(cg);
//        cge.exportMIG("flowdroidCFG2", outputPath);
        
        //��������ݿ�
        cge.setIdForCG(cg);
        ProcessManifest manifest = new ProcessManifest(apk);
		MySQLCor mysql = new MySQLCor(DB_URL_LOCAL, MySQLCor.USER_NAME,MySQLCor.USER_PWD);
		ProcessManifest processMan = new ProcessManifest(apk);
        CallGraphWithCFG callCFG = new CallGraphWithCFG(cg,processMan);
        Map<SootMethod, Integer> method2Id = new HashMap<>();
		List<MyEdge> myEdges = new ArrayList<MyEdge>();
//      mysql.insertMethodNodes(cge.getMethod2Id(), manifest);
//		mysql.insertMethodEdges(cge.getMyEdges(), manifest);
		mysql.insertMethodNodes(method2Id, manifest);
		mysql.insertMethodEdges(myEdges, manifest);
		
        System.out.println("the size is : " + cg.size() + "   end !!!");
	}
	
	
	
	//TODO �������ͨ���������ã��ı�call graph�漰�Ĺ�ģ��
	public static void initNew() throws IOException, XmlPullParserException{
		SetupApplication app = new SetupApplication(jarPath, apk);
		app.setCallbackFile("AndroidCallbacks.txt");
        try{
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
//      Options.v().set_no_bodies_for_excluded(true);
        Scene.v().loadNecessaryClasses();
        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        entryPoint.getActiveBody().validate();
        
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        PackManager.v().runPacks();
	}
}
