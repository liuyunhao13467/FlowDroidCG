package flowdroid.parser;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import flowdroid.utils.FileUtils;

import soot.Scene;
import soot.SootClass;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;


public class ApkInfoParser {
	public final static String INSERT_HIERARCHY = "insert into inheritance(apk_name,apk_version,class_sub,class_super,class_super_type) values(?,?,?,?,?)";
    public final static String INSERT_COMBINATION = "insert into combination(apk_name,apk_version,class_outer,member_type,member_name) values(?,?,?,?,?)";
    public final static String JAR_PATH = "lib/android.jar";

    // top. 用于只对apk 进行类信息的处理。
    public void parseClassesInApks(String apksDir) throws SQLException {
    	ArrayList<String> apkListStr = selectPathStringOfApk(apksDir);
    	for(String apkStr : apkListStr){
			parseOneApkInfo(apkStr);
    	}
	}
    
    public void initial(String apkPath){
		soot.G.reset();
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_src_prec(Options.java_version_1_7);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_force_android_jar(JAR_PATH);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().setPhaseOption("cg.spark verbose:true", "on");
		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		Scene.v().loadNecessaryClasses();
	}

	public void parseOneApkInfo(String apk) throws SQLException{
		System.out.println("the apk to get the class info is : " + apk);
		initial(apk);
		CallGraph callGraph = Scene.v().getCallGraph();
		Iterator<Edge> callIt = callGraph.iterator();
		while(callIt.hasNext()){
			System.out.println("the edge is : " + callIt.next());
		}
	}
	
	private ArrayList<String> selectPathStringOfApk(String apksDir){
		File apksFile = new File(apksDir);
		File apkFiles[] = apksFile.listFiles();
		
		ArrayList<String> apkListStr = new ArrayList<String>();
		for(int i = 0; i < apkFiles.length ; i++){
			if(FileUtils.isRequiredExtention(apkFiles[i].toString(), ".apk")){
				apkListStr.add(apkFiles[i].getPath());
			}
		}
		
		return apkListStr;
	}

}
