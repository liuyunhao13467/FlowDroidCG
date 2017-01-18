package flowdroid.test.dbTest;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.SootInit;
import flowdroid.db.MySQLCor;
import flowdroid.entities.InvokeWithCondition;
import flowdroid.entities.MyEdge;
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
//    public final static String myApk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    public final static String myApk = "test/game_strategy_apk";
    public static MySQLCor mysql;
    
	public static void main(String[] args) throws SQLException, IOException, XmlPullParserException {
		
		    mysql = new MySQLCor(MySQLCor.DB_URL_LOCAL, MySQLCor.USER_NAME,MySQLCor.USER_PWD);
			
			File apks = new File(myApk);
			if(!apks.isDirectory()){
				throw new IOException(apks.getAbsolutePath() + "  不是一个文件夹！");
			}
			
			for(File apk :apks.listFiles()){
				parseOneApk(apk.getAbsolutePath());
			}
			
			
	}
	
	public static void parseOneApk(String apkPath) throws IOException, XmlPullParserException, SQLException{

		
		SootInit.initSootForApk(SootInit.ANDROID_JAR_PATH,apkPath);
		ProcessManifest manifest = new ProcessManifest(apkPath);
		CallGraph cg = Scene.v().getCallGraph();
		Map<SootMethod,Integer> method2Id = new HashMap<>();
		List<MyEdge> myEdges = new ArrayList<>();
		CallGraphTools.setIdForCG(method2Id, myEdges, cg);
		mysql.insertMethodNodes(method2Id, manifest);
//		mysql.insertMethodEdges(myEdges, manifest);
		for(Iterator<MethodOrMethodContext> methods =cg.sourceMethods();methods.hasNext();){
			SootMethod current = methods.next().method();
			InvokeWithCondition invoke = new InvokeWithCondition(current);
			invoke.insertEdges(method2Id, manifest, mysql);
			System.out.println("end ~~~~");
		}
		
	}
	
}
