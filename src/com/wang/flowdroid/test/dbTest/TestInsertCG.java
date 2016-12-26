package flowdroid.test.dbTest;

import java.io.IOException;
import java.sql.SQLException;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.db.MySQLCor;
import flowdroid.test.GetOneApkCGTest;

public class TestInsertCG {
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
			
	}
}
