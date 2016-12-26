package flowdroid.utils;

import java.io.File;

public class FilePath {
	
	//用来制定androguard解析相关的路径。
	public final static String PATH_PARSER = "python_apk_parse/apk_parse/apk_parse.py"; //实验室封装。
	public final static String PATH_ANDROGUARD_PARSER = "python_apk_parse/apk_parse/androguard/androgexf.py";
	
	//标识文档处理状态的路径
//	public final static String PATH_FILE_HOME = "data/";
	public final static String PATH_FILE_HOME = "data_test/";//test
	public final static String PATH_APK_HOME = PATH_FILE_HOME + "apk_files/";

	public final static String PATH_APK_DIR_TO_DEAL = PATH_APK_HOME + "apk_to_deal/";
	public final static String PATH_APK_DIR_DONE = PATH_APK_HOME + "apk_done/";
	public final static String PATH_APK_CLASS_DONE = PATH_APK_HOME + "apk_class_done/";
	public final static String PATH_APK_DIR_SAVED_DB = PATH_APK_HOME + "apk_saved/";
	public final static String PATH_APK_DIR_INVALID = PATH_APK_HOME + "apk_invalid/";
	public final static String PATH_APK_DIR_SOLVING_BY_OTHERS_OR_YOU = PATH_APK_HOME + "apk_solving_by_others_or_you/";//TODO
	
	// for test
	public final static String PATH_APK_DIR_TEST = PATH_APK_HOME + "apk_test/";
	
	public final static String PATH_GEXF_HOME = PATH_FILE_HOME + "gexf/";
	
	public final static String PATH_GEXF_DIR_TO_DEAL = PATH_GEXF_HOME + "gexf_to_deal/";
	public final static String PATH_GEXF_DIR_DONE = PATH_GEXF_HOME + "gexf_done/";
	public final static String PATH_GEXF_DIR_SAVED_DB = PATH_GEXF_HOME + "gexf_saved/";

	/**
	 * 创建程序处理需要用到的文件夹。
	 */
	public static void createProcessDir(){
		File apkToDeal = new File(PATH_APK_DIR_TO_DEAL);
		apkToDeal.mkdirs();
		File apkDone = new File(PATH_APK_DIR_DONE);
		apkDone.mkdirs();
		File apkClassDone = new File(PATH_APK_CLASS_DONE);
		apkClassDone.mkdirs();
		File apkSavedDB = new File(PATH_APK_DIR_SAVED_DB);
		apkSavedDB.mkdirs();
		File apkInvalid = new File(PATH_APK_DIR_INVALID);
		apkInvalid.mkdirs();
		
		File gexfToDeal = new File(PATH_GEXF_DIR_TO_DEAL);
		gexfToDeal.mkdirs();
		File gexfDone = new File(PATH_GEXF_DIR_DONE);
		gexfDone.mkdirs();
		File gexfSaved = new File(PATH_GEXF_DIR_SAVED_DB);
		gexfSaved.mkdirs();
	}
}
