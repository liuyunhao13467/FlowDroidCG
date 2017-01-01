package flowdroid.test.ioTest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.SootInit;
import flowdroid.utils.FileUtils;
import flowdroid.utils.graphUtils.gexf.UnitGraph2GexfExporter;
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
 */
public class OutputWholeApkUnitGraph2gexf {
	
    //设置要分析的APK文件
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    public final static String outputDir = "test/gexf/";
	public static Map<String, Integer> fileCount = new HashMap<>();
    
	public static void main(String[] args) throws IOException, XmlPullParserException {
		SootInit.initSootForApk(SootInit.ANDROID_JAR_PATH, apk);
		ProcessManifest manifest = new ProcessManifest(apk);
		
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<MethodOrMethodContext> methodIt = cg.sourceMethods();
		while(methodIt.hasNext()){
			MethodOrMethodContext methodContext = methodIt.next();
			SootMethod method = methodContext.method();
			UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody()) ;
			UnitGraph2GexfExporter UGE = new UnitGraph2GexfExporter(ug);
			UGE.createGraph();
			UGE.setFileCount(fileCount);
			UGE.exportGexf(FileUtils.getMethodInfo(method), outputDir + manifest.getPackageName());
		}
	}
}
