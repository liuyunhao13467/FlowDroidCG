package flowdroid.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import soot.SootMethod;

public class FileUtils {
	
	/**
	 * 查看是否是符合后缀要求的文件。
	 * @param fileName  例：“achute.pixelsnake-3.gexf”
	 * @param requiredExtention 例：“.gexf”
	 */
	public static boolean isRequiredExtention(String fileName, String requiredExtention){
		if (fileName.lastIndexOf(requiredExtention) != -1 && 
				//确保 该扩展名 位置在最后。
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
			throw new IOException("没有要移动的文件 : " + sourceFile.getPath());
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
//			sourceFile.delete(); //删除重复的apk
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
			//e对象中是拥有一个输出流的，这里我们将其设置为了** 我们的IO流 **。
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
			StackTraceElement stackTraceElement= e.getStackTrace()[0];// 得到异常棧的首个元素
			System.out.println("File="+stackTraceElement.getFileName());// 打印文件名
			System.out.println("Line="+stackTraceElement.getLineNumber());// 打印出错行号
			System.out.println("Method="+stackTraceElement.getMethodName());// 打印出错方法 
			PrintStream print = new PrintStream(out.getAbsolutePath());
		
		}catch(Exception fe){
			fe.printStackTrace();
			System.exit(0);
		}
	}

	/*
	 * 对于拥有重复名字的文件，生成一个新的带数字标示的文件名。
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
