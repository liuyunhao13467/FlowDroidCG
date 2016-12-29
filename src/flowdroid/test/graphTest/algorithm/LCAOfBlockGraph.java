package flowdroid.test.graphTest.algorithm;

import java.awt.print.Printable;

import flowdroid.parser.BlockGraphWithCondition;
import flowdroid.test.graphTest.UnitGraphTest;
import soot.Scene;
import soot.SootClass;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;

public class LCAOfBlockGraph {

	public static final String path = "test/javaTest";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UnitGraphTest.initialSoot(path);
		SootClass appclass = Scene.v().loadClassAndSupport("TestMain2");// 若无法找到，则生成一个。
		BlockGraph bg = new BriefBlockGraph(appclass.getMethodByName("C").retrieveActiveBody());
		Block todo = null ;
		for(Block block : bg.getBlocks()){
			if(bg.getSuccsOf(block) != null && bg.getPredsOf(block).size() >= 2){
			todo = block;	
			}
		}
		
//		BlockGraphWithCondition.initVarForLCA();
//		BlockGraphWithCondition.findLCA(bg, bg.getHeads().get(0),todo, bg.getPredsOf(todo));
		System.out.println("src is :");
		System.out.println(todo);
		System.out.println("result is : ");
//		System.out.println(BlockGraphWithCondition.getLCA());
		
	}

}
