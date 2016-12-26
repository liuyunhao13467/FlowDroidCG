package flowdroid.test.ioTest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.test.GetOneApkCGTest;
import flowdroid.utils.FileUtils;
import flowdroid.utils.UnitGraphExporter;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * 测试将整个apk的UnitGraph变为gexf形式。
 * @author wang
 *
 */
public class OutputWholeApkUnitGraph2gexf {
	
	//设置android的jar包目录
    public final static String jarPath = "lib/android.jar";
    //设置要分析的APK文件
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    public final static String outputDir = "test/gexf/";
	public static Map<String, Integer> fileCount = new HashMap<>();

    
	public static void main(String[] args) throws IOException, XmlPullParserException {
		// TODO Auto-generated method stub
		GetOneApkCGTest.init(jarPath, apk);
		ProcessManifest manifest = new ProcessManifest(apk);
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<MethodOrMethodContext> methodIt = cg.sourceMethods();
		while(methodIt.hasNext()){
			MethodOrMethodContext methodContext = methodIt.next();
			SootMethod method = methodContext.method();
			UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody()) ;
			UnitGraphExporter UGE = new UnitGraphExporter(ug);
			UGE.createGraph();
			UGE.setFileCount(fileCount);
			UGE.exportMIG(FileUtils.getMethodInfo(method), outputDir + manifest.getPackageName());
		}
	}
}
