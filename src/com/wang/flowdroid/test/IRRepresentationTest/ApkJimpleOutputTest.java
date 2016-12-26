package flowdroid.test.IRRepresentationTest;

import java.util.Collections;

import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.jimple.infoflow.android.SetupApplication;
import soot.options.Options;

public class ApkJimpleOutputTest {

	//设置android的jar包目录
    public final static String jarPath = "lib/android.jar";
    //设置要分析的APK文件
    public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
    public final static String outputDir = "sootOutput/";
    
	public static void main(String[] args) {
		initialSoot1(apk);
	}
	
	public static void initialSoot1(String apkPath) {
		SetupApplication app = new SetupApplication(jarPath, apk);
		app.setCallbackFile("AndroidCallbacks.txt");
        try{
            //计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可。
            app.calculateSourcesSinksEntrypoints("test/sourcesAndSinks.txt");
        }catch(Exception e){
            e.printStackTrace();
        }
        
		soot.G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_output_dir(outputDir + "testApk");
		Options.v().set_src_prec(Options.src_prec_apk);
		 Options.v().set_force_android_jar(jarPath);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_whole_program(true);
		Options.v().set_no_bodies_for_excluded(true);
		
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		Scene.v().loadNecessaryClasses();
		//启动输出中间代码
		PackManager.v().writeOutput();
	}
}
