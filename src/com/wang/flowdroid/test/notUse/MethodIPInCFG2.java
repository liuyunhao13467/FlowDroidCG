package flowdroid.test.notUse;

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

import flowdroid.tools.ParserTools.CallGraphParserTools;
import flowdroid.utils.FileUtils;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class MethodIPInCFG2 {

	protected SootMethod caller;

	// 记录每条语句的位置信息。
	protected Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// 保存调用位置信息。
	protected Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// 根据上面的调用语句，转化为sootMethod调用位置。 【一个method可能在多处被调用 ----> list来保存位置信息。】
	protected Map<SootMethod, List<Position>> calleePosi = new HashMap<SootMethod, List<Position>>();// 存储每个调用关系的位置。

	// TODO
	protected Queue<Unit> worklistMultiSucc = new LinkedList<Unit>();
	protected List<Unit> worklistMultiPre = new LinkedList<Unit>();

	public static final int LAYER_START = 1;
	public static final int BRANCH_START = 1;
	public static final int POSITION_IN_BRANCH_START = 1;
	public static final boolean COMBINE_YES = true;

	public MethodIPInCFG2(SootMethod method) {
		try {
			caller = method;
			getOneMethodCallWithCFG(method);
			stmtPosi.keySet();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected void getOneMethodCallWithCFG(SootMethod method) throws Exception {
		JimpleBody body = (JimpleBody) method.retrieveActiveBody();
		UnitGraph ug = new BriefUnitGraph(body);

		// !!!个人认为，其他的地方是除了第一个头之外，其他的头应该都是不可达的。【只关注可达】(wrong)
		worklistMultiSucc.add(ug.getHeads().get(0));
		Position unusedPos = new Position(1, 1, 1);
		unusedPos.layerAndBranch.put(1, 1);
		stmtPosi.put(ug.getHeads().get(0), unusedPos);
		setOneMethodCallIP(ug);
	}

	enum WorklistType{worklistSucc_in,worklistPre_in,worklistSucc_out,worklistPre_out}
	WorklistType succListChangeType;
	WorklistType preListChangeType;
	protected void setOneMethodCallIP(UnitGraph ug) throws Exception {
		// TODO
		/**
		 * 1.放入 worklistMultiSucc 的队列中。
		 * 2.对于队列开始进行处理。（until worklistMultiSucc 和
		 * worklistMultiPre 都为空）
		 * 
		 * （在 worklistMultiSucc 队列中）
		 *  2.1 判断结点的类型。 
		 *  2.1.1 多分枝，单分支后继 --> 在worklistMultiSucc中进行处理。【分配位置信息】
		 *  2.1.2 多前驱结点 -->  不放入worklistMultiSucc，而是放入worklistMultiPre中。
		 *  
		 * （在worklistMultiSucc 为空后，开始处理 worklistMultiPre 中的内容）
		 *  2.2 为worklistMultiPre 中的元素分配位置信息。（分配规则 how?）
		 * 		2.2.1 找到前驱结点中层次最高的结点。 
		 * 		2.2.2找到前驱结点中高一层的结点，如果没有，则认为是，本层的结点。
		 * 2.3 worklistMultiPre 中的元素，按顺序放入到worklistMultiSucc 中。
		 */
		
		while (worklistMultiSucc.size() != 0 || worklistMultiPre.size() != 0 ) {
			
			succListChangeType = WorklistType.worklistSucc_out;
			preListChangeType = WorklistType.worklistPre_out;

			Unit tmpInMultiSucc ;
			while (worklistMultiSucc.size() != 0) {
				succListChangeType = WorklistType.worklistSucc_in;
				tmpInMultiSucc = worklistMultiSucc.poll();
				
				for(Unit u : ug.getSuccsOf(tmpInMultiSucc)){
					if(ug.getPredsOf(u).size() != 1 && !worklistMultiPre.contains(u) && !stmtPosi.containsKey(u)){//可能存在未被标记的情况u的前驱中可能有不可达点。
						worklistMultiPre.add(u);//不可重複加入。
						continue;
					}
				}
				
				//设置位置，并且加入到worklist中。
				if(ug.getSuccsOf(tmpInMultiSucc).size() >= 2){
					dealBranches(ug, tmpInMultiSucc);
				}else if(ug.getSuccsOf(tmpInMultiSucc).size() == 1 && !worklistMultiPre.contains(ug.getSuccsOf(tmpInMultiSucc).get(0))){
					Position current = stmtPosi.get(tmpInMultiSucc).deepCopy();
					current.positionInBranch ++;
					stmtPosi.put(ug.getSuccsOf(tmpInMultiSucc).get(0), current);
					worklistMultiSucc.add(ug.getSuccsOf(tmpInMultiSucc).get(0));
				}
			}
			
			if(succListChangeType == WorklistType.worklistSucc_in){
				System.out.println("worked in  worklist_succ");
			}

			Unit tmpMultiPre;
			Unit maxPre;
			for (int i = 0;i < worklistMultiPre.size();i++) {
				preListChangeType =WorklistType.worklistPre_in;
				tmpMultiPre = worklistMultiPre.get(i);
				
				boolean isAllFrontDone = true;
				//确保所有的前驱结点已经被处理。【多个头的情况，可能造成死循环！！】
				for(Unit u:ug.getSuccsOf(tmpMultiPre)){
					if(!stmtPosi.containsKey(u)){
						isAllFrontDone = false;
						break;
					}
				}
				if(!isAllFrontDone){
					break;
				}
				
				worklistMultiPre.remove(i);
				i--;//由于后面的元素会前移，所以还是下次还是处理该位置。
				
				maxPre = findMaxPreLayerUnit(ug, tmpMultiPre);
				Position maxFront =  findPossibleFrontMaxPosition(ug, maxPre);
				//set the current position.（不考虑不可达的）
				Position current = maxFront.deepCopy();
				current.positionInBranch ++;
				stmtPosi.put(tmpMultiPre, current);
				worklistMultiSucc.add(tmpMultiPre);
			}

			if(preListChangeType == WorklistType.worklistPre_in){
				System.out.println("worked in worklist_pre");
			}
			
			//终止大循环。
			if(succListChangeType == WorklistType.worklistSucc_out && preListChangeType == WorklistType.worklistPre_in){
				//TODO
				
				break;
			}
		}
	}

	protected void dealBranches(UnitGraph ug, Unit father) throws Exception {
		Position prePosition = stmtPosi.get(father);
		Position current;
		List<Unit> sons = ug.getSuccsOf(father);
		for (int i = 0; i < sons.size(); i++) {
			current = prePosition.deepCopy();
			current.layer++;
			current.currentBranch = i + 1;//branch分支起始位置为1.
			current.positionInBranch = 1;// 位置初始都为1
			current.layerAndBranch.put(current.layer, current.currentBranch);
			stmtPosi.put(sons.get(i), current);
		}
		worklistMultiSucc.addAll(sons);
	}
	
	protected Unit findMaxPreLayerUnit(UnitGraph ug, Unit unit){
		Unit maxUnit = null ;
		int topLayer = Integer.MAX_VALUE;
		
		for(Unit pre : ug.getPredsOf(unit)){
			if(stmtPosi.containsKey(pre)){
				if(topLayer > stmtPosi.get(pre).layer){
					topLayer = stmtPosi.get(pre).layer;
					maxUnit = pre;
				}
			}
		}
		return maxUnit;
	}
	
	protected Position findPossibleFrontMaxPosition(UnitGraph ug, Unit unit) {
		Queue<Unit> workList = new LinkedList<Unit>();
		Position max  =  stmtPosi.get(unit);
		Unit front;
		
		workList.add(unit);
		while(!workList.isEmpty()){
			front = workList.poll();
			if(stmtPosi.containsKey(front)){
				if(max.layer > stmtPosi.get(front).layer){
					max = stmtPosi.get(front);
					break;
				}else{
					workList.addAll(ug.getPredsOf(front));
				}
			}
		}
		
		return max;
	}

	protected boolean canSetPisition(UnitGraph ug, Stmt stmt) {
		// 看其前驱是否都进行了 位置标记。
		// 都标记则可以开始为此语句进行 位置标记。
		for (Unit preUnit : ug.getPredsOf(stmt)) {
			if (!stmtPosi.keySet().contains(preUnit)) {
				return false;
			}
		}
		return true;
	}

	public static class Position implements Serializable {
		public int layer;
		public int currentBranch;
		public int positionInBranch;
		// key 为类所处层次。value 为所在层次的分支。
		public Map<Integer, Integer> layerAndBranch = new HashMap<Integer, Integer>();

		Position(int layer, int branch, int positionInBranch) {
			this.layer = layer;
			this.currentBranch = branch;
			this.positionInBranch = positionInBranch;
		}

		public Position deepCopy() throws Exception {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);

			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (Position) ois.readObject();
		}
	}
}
