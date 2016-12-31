package flowdroid.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import soot.SootMethod;

public class FileUtils {
	
	//��ʶ�ĵ�����״̬��·��
    //public final static String PATH_FILE_HOME = "data/";
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
	 * ������������Ҫ�õ����ļ��С�
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

	
	/**
	 * �鿴�Ƿ��Ƿ��Ϻ�׺Ҫ����ļ���
	 * @param fileName  ������achute.pixelsnake-3.gexf��
	 * @param requiredExtention ������.gexf��
	 */
	public static boolean isRequiredExtention(String fileName, String requiredExtention){
		if (fileName.lastIndexOf(requiredExtention) != -1 && 
				//ȷ�� ����չ�� λ�������
				requiredExtention.length() + fileName.lastIndexOf(requiredExtention) == fileName.length() ){
			return true;
		}
		return false;
	}
	
	public static String dropFileExtention(String fileName, String extention) {
		String result = fileName.substring(0, fileName.lastIndexOf(extention));
		return result;
	}
	
	public static void dropPrefix(File file, String prefixToDrop, String destDir) {
		String name = file.getName();
		if(name.indexOf(prefixToDrop) == -1){
			return;
		}
		String nameDone = name.substring(name.indexOf(prefixToDrop) + prefixToDrop.length());
		file.renameTo(new File(destDir + nameDone));
		System.out.println("the name done is : " + nameDone);
	}
	
	public static void moveFileToAnother(File sourceFile,String destDir) throws IOException{
		if(!sourceFile.exists()){
			throw new IOException("û��Ҫ�ƶ����ļ� : " + sourceFile.getPath());
		}
		File destDirFile = new File(destDir);
		if(!destDirFile.exists()){
			destDirFile.mkdir();
		}
		
		if(sourceFile.renameTo(new File(destDir + sourceFile.getName()))){
			//TODO why?
	        System.out.println("move success :" + sourceFile.getPath());
		}else{
			System.out.println("move have done before " );
//			sourceFile.delete(); //ɾ���ظ���apk
		}
	}
	
	public static void clearGexfFile(String DirToClear){
		FileUtils.clearFile(DirToClear, ".gexf");
	}
	
	public static void clearFile(String DirToClear,String extentionOfFile){
		File dir = new File(DirToClear);
		File files[] = dir.listFiles();
		for(int i = 0;i < files.length ; i++){
			if(isRequiredExtention(files[i].getName(), extentionOfFile)){
				files[i].delete();
			}
		}
	}
	
	public static void outputException(String path,Exception e){
		File out = new File(path);
		try{
			if (!out.exists()) {
				out.createNewFile();
			}
			PrintStream print = new PrintStream(out.getAbsolutePath());
			//e��������ӵ��һ��������ģ��������ǽ�������Ϊ��** ���ǵ�IO�� **��
			e.printStackTrace(print);
		}catch(Exception fe){
			fe.printStackTrace();
			System.exit(0);
		}
		
	}

	public static void outputImportantExceptionInfo(String path,Exception e){
		File out = new File(path);
		try{
			if (!out.exists()) {
				out.createNewFile();
			}
			StackTraceElement stackTraceElement= e.getStackTrace()[0];// �õ��쳣�����׸�Ԫ��
			System.out.println("File="+stackTraceElement.getFileName());// ��ӡ�ļ���
			System.out.println("Line="+stackTraceElement.getLineNumber());// ��ӡ�����к�
			System.out.println("Method="+stackTraceElement.getMethodName());// ��ӡ������ 
			PrintStream print = new PrintStream(out.getAbsolutePath());
		
		}catch(Exception fe){
			fe.printStackTrace();
			System.exit(0);
		}
	}

	/*
	 * ����ӵ���ظ����ֵ��ļ�������һ���µĴ����ֱ�ʾ���ļ�����
	 */
	public static String getNewName(String path, String suffix, int count) {
		String newPath = path.substring(0, path.lastIndexOf(suffix));
		StringBuilder sb = new StringBuilder();
		sb.append(newPath).append("(").append(count).append(")").append(suffix);
		return sb.toString();
	}

	public static String getMethodInfo(SootMethod method){
		StringBuilder sb = new StringBuilder();
		sb.append(method.getDeclaringClass().getName());
		sb.append("_");
		sb.append(method.getName());
		return sb.toString();
	}
}
