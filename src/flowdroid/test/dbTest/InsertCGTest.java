package flowdroid.test.dbTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.SootInitForOneApk;
import flowdroid.db.MySQLCor;
import flowdroid.entities.MyEdge;
import flowdroid.parser.callgraph.CallGraphParser;
import flowdroid.utils.CallGraphTools;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;

/*
 * 尝试将带条件的语句插入到 数据库 中。
 */
public class InsertCGTest {
    public final static String myApk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    
	public static void main(String[] args) throws SQLException, IOException, XmlPullParserException {
		
			MySQLCor mysql = new MySQLCor(MySQLCor.DB_URL_LOCAL, MySQLCor.USER_NAME,MySQLCor.USER_PWD);
			SootInitForOneApk.initSootForApk(SootInitForOneApk.ANDROID_JAR_PATH,myApk);
			
			ProcessManifest manifest = new ProcessManifest(myApk);
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
