package flowdroid.entities.graph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import flowdroid.utils.CallGraphTools;
import flowdroid.utils.FileUtils;
import flowdroid.utils.graphUtils.dotUtils.Block2Graph;
import flowdroid.utils.graphUtils.dotUtils.UnitGraph2Dot;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class SquenceCallGraphInOneMethod {

	protected SootMethod caller;
	protected Map<Block, List<Unit>> invokeBlocks = new HashMap<Block, List<Unit>>();
	protected Map<Block, List<SootMethod>> methodInBlocks = new HashMap<Block, List<SootMethod>>();//TODO not use yet.
	protected BlockGraph bg;
	StringBuilder sbBlockDot = new StringBuilder();
	
	public SquenceCallGraphInOneMethod(SootMethod method) {
		this.caller = method;
		bg = new MyBriefBlockGraph(method.retrieveActiveBody());
		recordAllBlocksWithMethod(bg);
		changeBlockGraph();
	}

	protected void changeBlockGraph(){
		List<Block> blocksToRemove = new LinkedList<>();
		
		for(Block block : bg.getBlocks()){
			if(!invokeBlocks.containsKey(block)){
				dropConnectionOfBlockWithoutMethod(block, bg);
				blocksToRemove.add(block);
			}
		}
		//删除无关节点。
		for(Block removeBlock : blocksToRemove){
			bg.getBlocks().remove(removeBlock);
		}
	}
	protected void dropConnectionOfBlockWithoutMethod(Block block,BlockGraph bg){
		//删除没有含有方法调用的块。
		for(Block preBlock : bg.getPredsOf(block)){
			bg.getSuccsOf(preBlock).addAll(bg.getSuccsOf(block));
			bg.getSuccsOf(preBlock).remove(block);
		}
		
		for(Block sucBlock : bg.getSuccsOf(block)){
			bg.getPredsOf(sucBlock).addAll(bg.getPredsOf(block));
			bg.getPredsOf(sucBlock).remove(block);
		}
	}
	
	public void recordAllBlocksWithMethod(BlockGraph bg) {
		for (Block block : bg.getBlocks()) {
			findBlockWithMethodCall(block);
		}
		System.out.println("block graph done !!!");
	}

	protected void findBlockWithMethodCall(Block block) {
		Unit tmp = block.getHead();

		if (isInvokeStmt(tmp)) {
			insertBlockMap(block, tmp);
		}

		while (tmp != block.getTail()) {// "==" 与equals 区别。
			tmp = block.getSuccOf(tmp);
			if (isInvokeStmt(tmp)) {
				insertBlockMap(block, tmp);
			}
		}
	}

	protected boolean isInvokeStmt(Unit u) {
		if (u instanceof InvokeStmt) {
			return true;
		}
		return false;
	}

	protected void insertBlockMap(Block block, Unit u) {
		// 暂时先不去重。
		if (!invokeBlocks.containsKey(block) || invokeBlocks.get(block) == null) {
			invokeBlocks.put(block, new LinkedList<Unit>());
		}
		invokeBlocks.get(block).add(u);
	}

	protected void insertMethodInBlock(Block block, Unit u) {
		if (!methodInBlocks.containsKey(block) || invokeBlocks.get(block) == null) {
			methodInBlocks.put(block, new LinkedList<SootMethod>());
		}
		methodInBlocks.get(block).add(((InvokeStmt) u).getInvokeExpr().getMethod());
	}
	//画图
	public void drawGraph(){
		Block2Graph.dotify(bg, invokeBlocks, sbBlockDot);
		UnitGraph2Dot.createDotGraph(sbBlockDot.toString(), "BlockTest_C");
	}

	public SootMethod getMethod(){
		return this.caller;
	}
	
	public Map<Block, List<Unit>> getInvokeBlocks(){
		return this.invokeBlocks;
	}
	
	public Map<Block, List<SootMethod>> getMethodBlocks(){
		return this.methodInBlocks;
	}
	
	public BlockGraph getBlockGraph(){
		return this.bg;
	}
}
