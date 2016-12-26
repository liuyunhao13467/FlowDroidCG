package flowdroid.test.IRRepresentationTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import flowdroid.entities.graph.SquenceCallGraphInOneMethod;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.grimp.Grimp;
import soot.grimp.GrimpBody;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

//失败
public class GrimpTest {
	public static final String path = "test/javaTest";
	public static void main(String[] args) {
		initialSoot(path);
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");// 若无法找到，则生成一个。
	    SootMethod method = appclass.getMethodByName("C");
//	    GrimpBody body = Grimp.v().newBody(method.retrieveActiveBody(),"jb");
//	    Scene.v().getCallGraph();
//	    Body body = method.retrieveActiveBody();
	    changeToGrimp(method);
		System.out.println(method.getActiveBody().toString());
		
	}
	
	public static void initialSoot(String apkPath) {
		soot.G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_output_format(Options.output_format_grimp);
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_whole_program(true);
		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.Thread", SootClass.SIGNATURES);
		Options.v().set_no_bodies_for_excluded(true);
		
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		Scene.v().loadNecessaryClasses();
//		addTransform();
//		addGrimpTrans();
	}
	public static void enableSpark() {
		HashMap<String, String> opt = new HashMap<String, String>();
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
	
	public static void addTransform(){
		PackManager.v().getPack("jtp").add(new Transform("jtp.me", new BodyTransformer() {
			
			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
				System.out.println("shit ----");
			}
		}));
		PackManager.v().runPacks();
	}

	public static void addGrimpTrans(){
		PackManager.v().getPack("gb").add(new Transform("gb.getBody", new BodyTransformer() {
			
			@Override
			protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
				System.out.println("in the Grimp ----");
				System.out.println("method name : "+ b.getMethod().getName());
				SootMethod tmp = b.getMethod();
				GrimpBody gBody = Grimp.v().newBody(tmp);
				tmp.setActiveBody(gBody);
			}
		}));
		PackManager.v().runPacks();
	}

	public static void changeToGrimp(SootMethod m){
		 Body jb  = m.retrieveActiveBody();
		 GrimpBody gb = Grimp.v().newBody(jb,"gb");//这个应该可能是可以达到效果的，只不过jimple与grimp十分相近
		 m.setActiveBody(gb);
         PackManager.v().getPack("gop").apply(m.getActiveBody());
         PackManager.v().runPacks();
	}
}
