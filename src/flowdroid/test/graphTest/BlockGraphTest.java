package flowdroid.test.graphTest;

import flowdroid.SootInit;
import flowdroid.entities.graph.MyBriefBlockGraph;
import flowdroid.entities.graph.SquenceCallGraphInOneMethod;
import flowdroid.utils.graphUtils.dotUtils.Block2Graph;
import soot.Scene;
import soot.SootClass;
import soot.toolkits.graph.BlockGraph;

public class BlockGraphTest {
	public static final String path = "test/javaTest";
	public static StringBuilder sb = new StringBuilder();
	
	public static void main(String[] args) {
		SootInit.initSootForJava(path);
		// TODO test the block Graph.
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");// 若无法找到，则生成一个。
//	    SquenceCallGraphInOneMethod smIp = new SquenceCallGraphInOneMethod(appclass.getMethodByName("C"));
		System.out.println(appclass.getMethodByName("C").retrieveActiveBody().toString());
//		drawGraphDetailMethod(smIp);
		
		BlockGraph bg = new MyBriefBlockGraph(appclass.getMethodByName("C").retrieveActiveBody());
		drawCompleteGraph(bg);
	}
	
	public static void drawGraphDetailMethod(SquenceCallGraphInOneMethod smIp){
		Block2Graph.dotifyOnlyMethod(smIp.getBlockGraph(), smIp.getInvokeBlocks(), sb);
		Block2Graph.createDotGraph(sb.toString(), smIp.getMethod().getName() + "4v");
	}
	
	public static void drawGraphwithMethod(SquenceCallGraphInOneMethod smIp){
		Block2Graph.dotify(smIp.getBlockGraph(), smIp.getInvokeBlocks(), sb);
		Block2Graph.createDotGraph(sb.toString(), smIp.getMethod().getName() + "3v");
	}
	
	public static void drawCompleteGraph(SquenceCallGraphInOneMethod smIp){
		Block2Graph.dotifyCompleteBlock(smIp.getBlockGraph(), sb);
		Block2Graph.createDotGraph(sb.toString(), "CompleteBlockTest_C");
	}
	public static void  drawCompleteGraph(BlockGraph bg){
		Block2Graph.dotifyCompleteBlock(bg, sb);
		Block2Graph.createDotGraph(sb.toString(), "CompleteBlockTest_C");
	}
	
}
