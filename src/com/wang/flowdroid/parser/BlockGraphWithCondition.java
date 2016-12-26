package flowdroid.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import flowdroid.entities.graph.MyBriefBlockGraph;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.IfStmt;
import soot.jimple.SwitchStmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;

public class BlockGraphWithCondition {
	protected  BlockGraph bg; 
	protected  Map<Block, StringBuilder> block2Conditions;
	protected  Queue<Block> todoBlcok;// 可以换成队列。
	protected  Map<SootMethod,StringBuilder> method2Conditions;

	public BlockGraphWithCondition(BlockGraph bg){
		this.bg = bg;
	}
	
	protected  void initConditions(BlockGraph bg) {

		for (Block block : bg.getBlocks()) {
			StringBuilder condition = new StringBuilder();

			if (bg.getPredsOf(block).size() == 1) {
				// 查看前驱结点是否有分支条件语句。
				Unit currentHead = block.getHead();
				Block preBlock = bg.getPredsOf(block).get(0);
				Unit preTail = preBlock.getTail();

				if (preTail instanceof IfStmt) {
					// 分配条件
					if (currentHead == ((IfStmt) preTail).getTarget()) {
						condition.append(((IfStmt) preTail).getCondition());
					} else {
						condition.append("! " + ((IfStmt) preTail).getCondition());
					}

				} else if (preTail instanceof SwitchStmt) {
					// TODO 分配条件(Switch 有多种情况)

				}
			}
			block2Conditions.put(block, condition);
		}
	}
	
	protected  void recordConditins(BlockGraph bg) {
		block2Conditions = new HashMap<>(); // 记录block条件之间的对应关系。
		todoBlcok = new LinkedList<>();

		// 迭代确定条件信息。
		Block headBlock = bg.getHeads().get(0);
		todoBlcok.addAll(bg.getBlocks());
		todoBlcok.remove(headBlock);
		while (todoBlcok.size() > 0) {

			Block current = todoBlcok.poll();
			StringBuilder meetCondition = computeMeetConditions(bg, current);
			if (!block2Conditions.get(current).toString().equals(meetCondition)) {
				todoBlcok.addAll(bg.getSuccsOf(current));
			}
			block2Conditions.put(current, meetCondition);
		}
	}

	protected StringBuilder computeMeetConditions(BlockGraph bg, Block block) {

		// 计算交汇处的信息。（假设 已经去除了环）
		StringBuilder meet = new StringBuilder();
		if (bg.getPredsOf(block).size() == 1) {
			meet.append(block2Conditions.get(bg.getPredsOf(block).get(0)));
			return meet;
		}
		//需要先找到the lowest common ancestor (LCA) .
		initVarForLCA();
		findLCA(bg, bg.getHeads().get(0),block, bg.getPredsOf(block));

		// 获取条件
		meet.append(block2Conditions.get(LCA));
		return meet;
	}

	protected  Block LCA;
	protected  Set<Block> visited;
	protected  void initVarForLCA() {
		visited = new HashSet<>();
	}

	protected  Block getLCA() {
		return LCA;
	}

	/*
	 * 寻找最大的公共逆向路径汇合点。
	 */
	protected  Set<Block> findLCA(BlockGraph bg, Block current, Block end, List<Block> tgts) {
		if (current == null || current == end) {
			return null;
		}

		Set<Block> blockFinded = new HashSet<>();
		visited.add(current);
		for (Block tgt : tgts) {
			if (current == tgt) {
				blockFinded.add(current);
			}
		}

		Set<Block> findFromSucc;
		for (Block succ : bg.getSuccsOf(current)) {
			if (visited.contains(succ)) {
				continue;
			}

			findFromSucc = findLCA(bg, succ, end, tgts);
			if(findFromSucc == null ){
				continue;
			}
			
			if ( findFromSucc.size() == tgts.size()) {
				return findFromSucc;
			}
			blockFinded.addAll(findFromSucc);
		}

		if (blockFinded.size() == tgts.size()) {
			LCA = current;
		}
		return blockFinded;
	}

	protected  boolean isIfOrSwitchInBlock(Block block) {
		Unit tail = block.getTail();
		if ((tail instanceof IfStmt) || (tail instanceof SwitchStmt)) {
			return true;
		}
		return false;
	}

	protected  void setCondition(BlockGraph bg, Block block) {
		// 对语句进行分析（分支【多后继】，汇点【多前驱】）
		if (bg.getSuccsOf(block) != null && bg.getSuccsOf(block).size() >= 2) {

		}
		if (bg.getPredsOf(block) != null && bg.getPredsOf(block).size() >= 2) {

		}
	}

}
