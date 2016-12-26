package com.wang.flowdroid.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flowdroid.entities.MyEdge;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class MySQLCor {

	private String dbUrl = "";

	private Connection con = null;

	public MySQLCor() throws SQLException, ClassNotFoundException {
		setAndGetConnection();
	}

	public MySQLCor(String url) throws SQLException {
		this.dbUrl = url;
		setAndGetConnection();
	}

	public MySQLCor(String url, String userName, String pwd) throws SQLException {
		this.dbUrl = url;
		setAndGetConnection(userName, pwd);
	}

	public Connection setAndGetConnection() throws SQLException {
		if (con != null && !con.isClosed()) {
			return con;
		}

		// default user.
		String dbUserName = "root";
//		String dbPassword = "asd123";
//		String dbPassword = "root";
		String dbPassword = "123456";
		return setAndGetConnection(dbUserName, dbPassword);
	}

	public Connection getCon() {
		return con;
	}

	public Connection setAndGetConnection(String dbUserName, String dbPassword)
			throws  SQLException {
		String dbDriver = "com.mysql.jdbc.Driver";
		try {
			Class.forName(dbDriver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		con = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
		System.out.println("connected....  " + dbUrl);
		System.out.println();
		return con;
	}
	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public void closeCon() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void ExceptionLog(Exception e) {
//		StringBuffer write = new StringBuffer("E:\\apk\\apk_detail_string\\exception.txt");
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write.toString()), true));
//
//			writer.write("e.getMessage(): " + e.getMessage());
//			writer.newLine();
//			e.printStackTrace();

//			writer.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
	}

	//test get data.
	public ResultSet select(String sql){
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}
	
	public ResultSet select(String sql, String classname, String sig) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			prestmt.setString(1, classname);
			prestmt.setString(2, sig);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}

	
	public void insertMethodNodes(Map<SootMethod, Integer> method2id,ProcessManifest manifest) throws SQLException{
		String insertNodesSql = "insert ignore into method (apk_name,apk_version,mid,package_name,class_name,method_name,parameter,return_type) "
				+ "values(?,?,?,?,?,?,?,?)";//8个字段。
		PreparedStatement prestmt = con.prepareStatement(insertNodesSql);
		//apk相关的信息。
		String apkName = manifest.getPackageName();
		String apkVersion = manifest.getVersionName();
		
		Iterator<SootMethod> methodIt = method2id.keySet().iterator();
		SootClass sootClass;
		SootMethod method;
		Integer mId;
		System.out.println("正在准备插入apk : " + apkName + "_" + apkVersion);
		while(methodIt.hasNext()){
			method = methodIt.next();
			sootClass = method.getDeclaringClass();
			mId = method2id.get(method);
			String packageName = sootClass.getPackageName();
			String className = sootClass.getName();
			String methodName = method.getName();
			String parameter = method.getBytecodeParms();
			String returnType = method.getReturnType().toString();
			
			//insert into db.
			prestmt.setString(1, apkName);
			prestmt.setString(2, apkVersion);
			prestmt.setInt(3, mId);
			prestmt.setString(4, packageName);
			prestmt.setString(5, className);
			prestmt.setString(6, methodName);
			prestmt.setString(7, parameter);
			prestmt.setString(8, returnType);
			prestmt.addBatch();
		}
		double startTime = System.currentTimeMillis();
		prestmt.executeBatch();
		double endTime = System.currentTimeMillis();
		System.out.println("插入apk结束 : " + apkName + "_" + apkVersion);
		System.out.println("插入结点消耗时间 ： " + (endTime -startTime) + " 毫秒");
	}
	
	
	public void insertMethodEdges(List<MyEdge> myEdges,ProcessManifest manifest) throws SQLException{
		String insertEdgesSql = "insert into invoke (apk_name,apk_version,edge_id,caller_id,callee_id,condition) "
				+ "values(?,?,?,?,?,?)";//未考虑condition字段
		PreparedStatement prestmt = con.prepareStatement(insertEdgesSql);
		String apkName = manifest.getPackageName();
		String apkVersion = manifest.getVersionName();
		System.out.println("正在准备插入apk边 : " + apkName + "_" + apkVersion);
		double startTime = System.currentTimeMillis();
		for(MyEdge myEdge : myEdges){
			prestmt.setString(1, apkName);
			prestmt.setString(2, apkVersion);
			prestmt.setInt(3, myEdge.getIp());
			prestmt.setInt(4, myEdge.getSrc());
			prestmt.setInt(5, myEdge.getTgt());
			prestmt.setString(6, myEdge.getConditions().toString());
			prestmt.addBatch();
		}
		
		prestmt.executeBatch();
		double endTime = System.currentTimeMillis();
		System.out.println("正在准备插入apk边 : " + apkName + "_" + apkVersion);
		System.out.println("插入边消耗时间 ： " + (endTime -startTime) + " 毫秒 ");

	}
	
	// 测试double
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		String testname = "name";
		String dburl = "jdbc:mysql://localhost:3306/permission_code_parser";
		MySQLCor mysql = new MySQLCor(dburl);

	}

}
