package flowdroid.test.ioTest;

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
import flowdroid.utils.graphUtils.gexf.CGExporter;
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

public class CallGraph2GexfTest {
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";//设置要分析的APK文件
    public final static String outputPath = "test/gexf";
    
	public static void main(String[] args) throws IOException, XmlPullParserException, SQLException {
		
		SootInitForOneApk.initSootForApk(SootInitForOneApk.ANDROID_JAR_PATH, apk);
        CallGraph cg = Scene.v().getCallGraph();
        CGExporter cge = new CGExporter();
        cge.createGraph(cg);
        cge.exportMIG("flowdroidCFG2", outputPath);
	}
}
