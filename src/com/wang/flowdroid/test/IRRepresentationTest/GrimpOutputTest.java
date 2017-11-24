package flowdroid.test.IRRepresentationTest;

import java.util.Collections;

import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

public class GrimpOutputTest {
	public static final String path = "test/javaTest";
	public static void main(String[] args) {
		
		initialSoot(path);
//		String[] sootArgs = {"-process-dir",path, "-d","sootOutput/Grimple","-f","G"};
//		soot.Main.main(sootArgs);
		
		System.out.println("end~~");
	}
	
	
	public static void initialSoot(String apkPath) {
		soot.G.reset();
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_validate(true);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_output_dir("sootOutput");
		Options.v().set_src_prec(Options.src_prec_java);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_whole_program(true);
		Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.Thread", SootClass.SIGNATURES);
		Options.v().set_no_bodies_for_excluded(true);
		
		PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
		Scene.v().loadNecessaryClasses();
		 PackManager.v().writeOutput(); 
//		addTransform();
//		addGrimpTrans();
		
	}
}
