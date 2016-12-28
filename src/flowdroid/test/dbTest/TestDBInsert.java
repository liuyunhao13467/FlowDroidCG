package flowdroid.test.dbTest;

import java.sql.SQLException;

import flowdroid.db.MySQLCor;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class TestDBInsert {
	public static final String DB_URL_LOCAL = "jdbc:mysql://localhost:3306/graph_cfg";
	public  static final String USER_NAME = "root";
	public static final String USER_PWD = "123456";
	public static void main(String[] args) throws Exception {
		MySQLCor mysql = new MySQLCor(DB_URL_LOCAL, USER_NAME,USER_PWD);
		String apkJimplePath = "sootOutput/testApk";
		String apkPath = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
		ProcessManifest manifest = new ProcessManifest(apkPath);
		mysql.insertApkClassJimple(apkJimplePath, manifest);
	}

}
