package flowdroid.parser.callgraph;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class CallGraphParser {
	public static final String apkPath = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
	public static final String androidJar = "lib/android.jar";
	public static final String sourceAndSinks = "test/sourcesAndSinks.txt";

	public static void main(String[] args) {
		System.out.println("started ....");
		File file = new File(apkPath);
		SetupApplication app = new SetupApplication(androidJar, file.getAbsolutePath());
		try {
			// 计算APK的入口点，这一步导入的文件是Flowdroid进行污点分析的时候需要的，这里直接新建一个空文件即可
			app.calculateSourcesSinksEntrypoints(sourceAndSinks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		initial(apkPath);
		// SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		// Options.v().set_main_class(entryPoint.getSignature());
		// Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		PackManager.v().runPacks();
		// 获取函数调用图
		System.out.println("get call graph ...");
		CallGraph cg = Scene.v().getCallGraph();
		seeCallGraph(cg);
	}
	private static void initial(String apkPath) {
		soot.G.reset();
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList(apkPath));
		Options.v().set_force_android_jar(androidJar);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().setPhaseOption("cg.spark verbose:true", "on");
		// Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
		// Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
		Scene.v().loadNecessaryClasses();
	}
	// TODO test the UnitGraph.
	private static void controlUnitGraph(Body body) {

		UnitGraph unitGraph = new BriefUnitGraph(body);
		List<Unit> unitsOfOneMethod = unitGraph.getHeads();
		Iterator<Unit> UnitIn = unitsOfOneMethod.iterator();
		while (UnitIn.hasNext()) {
			Unit tmpUnit = UnitIn.next();
			List<UnitBox> unitBoxs = tmpUnit.getUnitBoxes();
			Iterator<UnitBox> unitBoxsIn = unitBoxs.iterator();
			while (unitBoxsIn.hasNext()) {
				UnitBox tmpUnitBox = unitBoxsIn.next();
				tmpUnitBox.isBranchTarget();// TODO what is the meaning?
			}
		}
	}
	public static void seeCallGraph(CallGraph cg) {
		Iterator<Edge> edgeIn = cg.iterator();
		int i = 0;
		Edge edgeTmp;
		while (edgeIn.hasNext()) {
			edgeTmp = edgeIn.next();
			++i;
			System.out.println(
					i + " : " + edgeTmp.getSrc().method().getName() + " ----> " + edgeTmp.getTgt().method().getName());
			SootMethod method = edgeTmp.getSrc().method();
			Body body = method.getActiveBody();
			List<UnitBox> units = body.getAllUnitBoxes();
			// TODO 深入CFG内部。
			JimpleBasedInterproceduralCFG jmpCFG = new JimpleBasedInterproceduralCFG();
			controlUnitGraph(body);
			BiDiInterproceduralCFG<Unit, SootMethod> cfg;
		}
	}
}
