package flowdroid;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;

import flowdroid.parser.CallGraphWithCFG;
import soot.Local;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.toolkits.scalar.FlowSet;
import soot.util.Chain;

/**
 * 该段测试代码的主要目的： 完成对于soot的基本信息的一些配置，供其他模块使用。
 * @author wang
 */
public class SootInitForOneApk {
	public final static String ANDROID_JAR_PATH = "lib/android.jar";// 设置android的jar包目录
	
	public static void initSootForJava(){
		//TODO 为java文件的soot使用提供配置。
	}

	public static void initSootForApk(String jarPath, String apk) throws IOException, XmlPullParserException {
		SetupApplication app = new SetupApplication(jarPath, apk);
		app.setCallbackFile("AndroidCallbacks.txt");
		try {
			// 计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可。
			app.calculateSourcesSinksEntrypoints("test/sourcesAndSinks.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}

		soot.G.reset();
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList(apk));
		Options.v().set_force_android_jar(jarPath);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		// Options.v().setPhaseOption("cg.spark", "on");
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_app(true);
		enableSpark();
		Scene.v().loadNecessaryClasses();

		AndroidEntryPointCreator entryCreator = app.getEntryPointCreator();
																			
		SootMethod entryPoint = entryCreator.createDummyMain();
		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		PackManager.v().runPacks();
	}

	
	
	protected static void enableSpark() {// 只是针对spark方面的配置。
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		HashMap<String, String> opt = new HashMap<String, String>();// TODO 为什么设置之后无效？
		opt.put("verbose", "true");
		opt.put("propagator", "worklist");
		opt.put("simple-edges-bidirectional", "false");
		opt.put("on-fly-cg", "true");
		opt.put("set-impl", "double");
		opt.put("double-set-old", "hybrid");
		opt.put("double-set-new", "hybrid");
		opt.put("pre_jimplify", "true");
		opt.put("apponly", "true");
		SparkTransformer.v().transform("", opt);
	}

	
	
	
	
	/********
	 * test
	 *******
	 */
	public final static String apk = "test/test_apk/aa.ex.B_K_K_AD-67.apk";	// 设置要分析的APK文件
	public static void main(String[] args) throws IOException, XmlPullParserException {

		initSootForApk(ANDROID_JAR_PATH, apk);
		CallGraph cg = Scene.v().getCallGraph();
		Chain<SootClass> classes = Scene.v().getClasses();
		Chain<SootClass> phtomClasses = Scene.v().getPhantomClasses();
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		Chain<SootClass> libraryClass = Scene.v().getLibraryClasses();
		ProcessManifest processMan = new ProcessManifest(apk);
		CallGraphWithCFG callCFG = new CallGraphWithCFG(cg, processMan);
		System.out.println("the size is : " + cg.size());
	}
}
