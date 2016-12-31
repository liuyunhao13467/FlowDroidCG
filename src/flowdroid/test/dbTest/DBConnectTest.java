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
import flowdroid.entities.ApkFromSql;
import flowdroid.entities.MethodFromSql;
import flowdroid.entities.MethodID;
import flowdroid.entities.MyEdge;

public class DBConnectTest {
	public static void main(String[] args) throws SQLException {
		MySQLCor mysql = new MySQLCor(MySQLCor.DB_URL_LOCAL, MySQLCor.USER_NAME,MySQLCor.USER_PWD);
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
	
	//test.
	public static ApkFromSql data2ApkEntity(MySQLCor mysql) throws SQLException{
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
		ApkFromSql tmpApk = fillApk(apkName, apkVersion, rsMethod, rsCall);
		//TODO　　test results.
//		seeAllMethods(tmpApk);
		
		return tmpApk;
	}
	public static ApkFromSql fillApk(String apkName,String apkVersion,ResultSet rsMethod,ResultSet rsCall) throws SQLException{
		ApkFromSql apk = new ApkFromSql();
		apk.setApkName(apkName);
		apk.setApkVersion(apkVersion);
		
		//fill the method.
		Set<MethodFromSql> methods = apk.getMethods();
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
	
	public static void fillOneMethod(ResultSet rsMethod,Set<MethodFromSql> methods) throws SQLException{
		MethodFromSql tmpMethod = null;
		MethodID id = null;
		tmpMethod = new MethodFromSql();
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

	public static Map<Integer, List<Integer>>  getOutEdges(ApkFromSql apk){
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
	public static void seeAllMethods(ApkFromSql apk){
		System.out.println("show the result : ");
		System.out.println(apk.getApkName());
		System.out.println(apk.getApkVersion());
		
		Iterator<MethodFromSql> methodIt = apk.getMethods().iterator();
		MethodFromSql tmpMethod;
		System.out.println("see all the methods inside : "+ apk.getApkName());
		while(methodIt.hasNext()){
			tmpMethod = methodIt.next();
			System.out.println(tmpMethod.getMethodID().getIDInApk() + " : " + tmpMethod.getMethodName());
		}
	}
}
