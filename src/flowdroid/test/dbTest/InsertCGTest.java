package flowdroid.test.dbTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.db.MySQLCor;
import flowdroid.entities.MyEdge;
import flowdroid.parser.callgraph.CallGraphParser;
import flowdroid.test.GetOneApkCGTest;
import flowdroid.utils.CallGraphTools;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;

public class InsertCGTest {
	public static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/graph_cfg";
	public  static final String USER_NAME = "root";
	public static final String USER_PWD = "123456";
	//设置android的jar包目录
    public final static String jarPath = "lib/android.jar";
    //设置要分析的APK文件
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
	public static void main(String[] args) throws SQLException, IOException, XmlPullParserException {
			MySQLCor mysql = new MySQLCor(DB_URL_LOCAL, USER_NAME,USER_PWD);
			GetOneApkCGTest.init(jarPath,apk);
			ProcessManifest manifest = new ProcessManifest(apk);
			CallGraph cg = Scene.v().getCallGraph();
			Map<SootMethod,Integer> method2Id = new HashMap<>();
			List<MyEdge> myEdges = new ArrayList<>();
			CallGraphTools.setIdForCG(method2Id, myEdges, cg);
			
//			mysql.insertMethodNodes(method2Id, manifest);
//			mysql.insertMethodEdges(myEdges, manifest);
			
			for(Iterator<MethodOrMethodContext> methods =cg.sourceMethods();methods.hasNext();){
				SootMethod current = methods.next().method();
				mysql.insertOneUnitGraph(new BriefUnitGraph(current.retrieveActiveBody()), method2Id, manifest);
			}
			
	}
}
