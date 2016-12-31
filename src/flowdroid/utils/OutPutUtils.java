package flowdroid.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;


/*
 * ���һЩ��Ϣ��
 */
public class OutPutUtils {

	public static void printToFile(String outputPath){
		
	}
	
	/**
	 * �÷�����Ҫ��������apk�Ľ�����call graph�������������ı��С�
	 * @param apkOutputPath  ��������·����
	 * @param callGraph ��ʾ��Ҫ��������ĺ������ù�ϵͼ��
	 * @param processMan ��apk���Ӧ�Ļ�����Ϣ��
	 */
	public void outputApkMethodInfo(String apkOutputPath,CallGraph callGraph,ProcessManifest processMan){
		Iterator<MethodOrMethodContext> it = callGraph.sourceMethods();
		String ownPath = apkOutputPath + "own_method.txt";
		String androidPath =  apkOutputPath + "android_method.txt";
		String javaPath = apkOutputPath + "java_method.txt";
		String thirdPath =  apkOutputPath + "third_method.txt";
		 
		File outputDir = new File(apkOutputPath);
		if(!outputDir.exists()){
			outputDir.mkdirs();
		}
		
		File ownFile = new File(ownPath);
		File androidFile = new File(androidPath);
		File javaFile = new File(javaPath);
		File thirdFile  = new File(thirdPath);
		
               try {
					ownFile.createNewFile();
					androidFile.createNewFile();
					javaFile.createNewFile();
					thirdFile.createNewFile();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
               
               PrintStream ownPrint = null;
               PrintStream androidPrint = null;
               PrintStream javaPrint = null;
               PrintStream thirdPrint = null;
               
			try {
				ownPrint = new PrintStream(ownFile.getAbsolutePath());
				androidPrint = new PrintStream(androidFile.getAbsolutePath());
				javaPrint = new PrintStream(javaFile.getAbsolutePath());
				thirdPrint = new PrintStream(thirdFile.getAbsolutePath());
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		SootMethod tmpMethod = null;
		while (it.hasNext()) {
			tmpMethod = it.next().method();
			if(ParserUtils.isOwnMethod(tmpMethod, processMan.getPackageName())){//TODO ��android����Ҳ�ų�����
				System.setOut(ownPrint);
		           
			}else if (tmpMethod.isJavaLibraryMethod()){
				System.setOut(javaPrint);
			}else if (ParserUtils.isAndroidMethod(tmpMethod)){
				System.setOut(androidPrint);
			}else{
				System.setOut(thirdPrint);
			}
			
			System.out.println(tmpMethod.getDeclaringClass().getName() + " ---> " + tmpMethod.getName());
		}
	
		ownPrint.close();
		javaPrint.close();
		androidPrint.close();
		thirdPrint.close();
	}


}
