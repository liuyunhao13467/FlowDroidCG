package flowdroid.test.dbTest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flowdroid.db.MySQLCor;
import flowdroid.entities.Apk;
import flowdroid.entities.Method;
import flowdroid.entities.MethodID;
import flowdroid.entities.MyEdge;

public class DBConnectTest {
	public static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/test_graph";
	public  static final String USER_NAME = "root";
	public static final String USER_PWD = "123456";
	public static void main(String[] args) throws SQLException {
		MySQLCor mysql = new MySQLCor(DB_URL_LOCAL, USER_NAME,USER_PWD);
		data2ApkEntity(mysql);
	}
	
	public static void testGetData(MySQLCor mysql) throws SQLException{
		String sql = "SELECT apk_name,apk_version from apk_files"; 
		ResultSet rs = mysql.select(sql);
		if(rs.first()){
			System.out.println(rs.getString(1) + " - " + rs.getString(2));
			while(rs.next()){
				System.out.println(rs.getString(1) + " - " + rs.getString(2));
			}
		}
	}
	
	public static Apk data2ApkEntity(MySQLCor mysql) throws SQLException{
		String apkName = "\"com.apps1pro1.word\"";
		String apkVersion ="\"2\"";
		String sql_method = "SELECT mid,package_name,class_name,method_name,parameter,return_type "
				+ "from method_androguard WHERE apk_name = "+ apkName+" AND apk_version = "+apkVersion;
		String sql_call = "SELECT caller_id,callee_id "
				+ "from invoke_androguard WHERE apk_name = "+ apkName+" AND apk_version = "+apkVersion;
		//从数据库获取数据。
		ResultSet rsMethod = mysql.select(sql_method);
		ResultSet rsCall = mysql.select(sql_call);
		//　将数据填充进数据结构中。
		Apk tmpApk = fillApk(apkName, apkVersion, rsMethod, rsCall);
		//TODO　　test results.
//		seeAllMethods(tmpApk);
		
		return tmpApk;
	}
	public static Apk fillApk(String apkName,String apkVersion,ResultSet rsMethod,ResultSet rsCall) throws SQLException{
		Apk apk = new Apk();
		apk.setApkName(apkName);
		apk.setApkVersion(apkVersion);
		
		//fill the method.
		Set<Method> methods = apk.getMethods();
		if (rsMethod.first()) {
			fillOneMethod(rsMethod, methods);
			while(rsMethod.next()){
				fillOneMethod(rsMethod, methods);
			}
		}
		
		//  fill the edge info .
		Set<MyEdge> edges = apk.getEdges();
		if(rsCall.first()){
			fillOneEdge(rsCall, edges);
			while(rsCall.next()){
				fillOneEdge(rsCall, edges);
			}
		}
		return apk;
	}
	
	public static void fillOneMethod(ResultSet rsMethod,Set<Method> methods) throws SQLException{
		Method tmpMethod = null;
		MethodID id = null;
		tmpMethod = new Method();
		tmpMethod.setPackageName(rsMethod.getString("package_name"));
		tmpMethod.setClassName(rsMethod.getString("class_name"));
		id = new MethodID();
		id.setApk(null);
		id.setIDInApk(rsMethod.getInt("mid"));
		tmpMethod.setMethodID(id);
		tmpMethod.setMethodName(rsMethod.getString("method_name"));
		tmpMethod.setParameters(rsMethod.getString("parameter"));
		tmpMethod.setReturnType(rsMethod.getString("return_type"));
		
		methods.add(tmpMethod);
	}
	
	public static void fillOneEdge(ResultSet rsCall ,Set<MyEdge> edges) throws SQLException{
		MyEdge tmpEdge = null;
		
		tmpEdge = new MyEdge();
		tmpEdge.setSrc(rsCall.getInt("caller_id"));
		tmpEdge.setTgt(rsCall.getInt("callee_id"));
		edges.add(tmpEdge);
	}

	public static Map<Integer, List<Integer>>  getOutEdges(Apk apk){
		Map<Integer, List<Integer>> outEdges = new HashMap<Integer,List<Integer>>();
		Iterator<MyEdge> edgeIt = apk.getEdges().iterator();
		MyEdge tmpEdge = null;
		while(edgeIt.hasNext()){
			tmpEdge = edgeIt.next();
			if(outEdges.containsKey(tmpEdge.getSrc()) && outEdges.get(tmpEdge.getSrc()) != null){
				outEdges.get(tmpEdge.getSrc()).add(tmpEdge.getTgt());
			}else{
				List<Integer> calleeNodes = new ArrayList<Integer>();
				calleeNodes.add(tmpEdge.getTgt());
				outEdges.put(tmpEdge.getSrc(), calleeNodes);
			}
		}
		
		return outEdges;
	}
	
	
	// for checking the results.
	public static void seeAllMethods(Apk apk){
		System.out.println("show the result : ");
		System.out.println(apk.getApkName());
		System.out.println(apk.getApkVersion());
		
		Iterator<Method> methodIt = apk.getMethods().iterator();
		Method tmpMethod;
		System.out.println("see all the methods inside : "+ apk.getApkName());
		while(methodIt.hasNext()){
			tmpMethod = methodIt.next();
			System.out.println(tmpMethod.getMethodID().getIDInApk() + " : " + tmpMethod.getMethodName());
		}
	}
}
