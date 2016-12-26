package flowdroid.entities.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.UnitGraph;

public class MyBriefBlockGraph extends BriefBlockGraph {
	
	public MyBriefBlockGraph(Body body) {
		this(new MyBriefUnitGraph(body));
		//使一些成员可读。
		changeBlockModifiable();
		
	}
	public MyBriefBlockGraph(BriefUnitGraph unitGraph) {
		super(unitGraph);
	}
	
	
	protected void buildBlockGraphHeadsAndTails(){
		//建立头部与尾部。(not used)
		mHeads =  new ArrayList<Block>();
		mTails =  new ArrayList<Block>();
		for(Block block : mBlocks){
			if(block.getPreds() == null || block.getPreds().size() == 0){
				mHeads.add(block);
			}
			if(block.getSuccs() == null || block.getSuccs().size() == 0){
				mTails.add(block);
			}
		}
	}

	protected void changeBlockModifiable(){
		List<Block> tgtBlocks = new LinkedList<>();
		tgtBlocks.addAll(this.mBlocks);
		this.mBlocks = tgtBlocks;
		for(Block block : this.mBlocks){
			changeOneBlock(block);
		}
	}
	
	protected void changeOneBlock(Block block){
		//将不可改变的内容，变得可以改变。
		List<Block> srcPre = block.getPreds();
		List<Block> tgtPre = new LinkedList<Block>();
		List<Block> srcSucc = block.getSuccs();
		List<Block> tgtSucc = new LinkedList<Block>();
		
		tgtPre.addAll(srcPre);
		tgtSucc.addAll(srcSucc);
		
		block.setPreds(tgtPre);
		block.setSuccs(tgtSucc);
	}

}
