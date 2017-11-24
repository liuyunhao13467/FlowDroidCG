package flowdroid.test.notUse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import flowdroid.tools.ParserTools.CallGraphParserTools;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * 测试目的：
 * 1.通过递归的方式，为每个UnitGraph中的 语句分配IP。
 * 
 * 存在问题：
 * 1.递归的结构过于复杂，逻辑混乱。（造成难以调试）
 * 2.没有深入了解UnitGraph中的结构。
 * 
 * 结论：
 * 不考虑这种方法。
 * @author wang
 *
 */
public class setIPForUnitInUnitGraph {
	// 记录每条语句的位置信息。
	public static Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// 保存调用位置信息。
	public static Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// 根据上面的调用语句，转化为sootMethod调用位置。 【一个method可能在多处被调用 ----> list来保存位置信息。】
	public static Map<SootMethod, List<Position>> methodPosi = new HashMap<SootMethod, List<Position>>();// 存储每个调用关系的位置。

	// 是否可以换成枚举？
	public static final int LAYER_START = 1;
	public static final int BRANCH_START = 1;
	public static final int POSITION_IN_BRANCH_START = 1;

	public static final boolean COMBINE_YES = true;

	public static void main(String[] args) {

		// test in another class.

	}

	public static void getOneMethodCallWithCFG(SootMethod method) throws Exception {
		JimpleBody body = (JimpleBody) method.retrieveActiveBody();
		UnitGraph ug = new BriefUnitGraph(body);
		for (Unit u : ug.getHeads()) {

			Position unusedPos = new Position(1, 1, 1);
			unusedPos.layerAndBranch.put(1, 1);
			setOneMethodCallIP(ug, (Stmt) u, unusedPos);
		}
	}

	public static void setOneMethodCallIP(UnitGraph ug, Stmt stmt, Position path) throws Exception {
		setOneMethodCallIP(ug, stmt, path, false);
	}

	public static void setOneMethodCallIP(UnitGraph ug, Stmt stmt, Position currentPos, boolean canCombine)
			throws Exception {

		if (stmt == null) {
			return;
		}

		if (ug.getPredsOf(stmt).size() > 1) {
			// 作用块结束. 重新分配位置。1.确定高层次位置。 2.在原来高层次的位置进行修改。
			if (canSetPisition(ug, stmt) && !canCombine) {
				Position prePosition = findPrePosition(ug, stmt);
				Position mPosition = prePosition.deepCopy();
				mPosition.positionInBranch++;
				setOneMethodCallIP(ug, stmt, mPosition, COMBINE_YES);
				return;
			}
		}

		// 保存信息。
		stmtPosi.put(stmt, currentPos);

		// 区分方法调用在分支中的层次，所处的分支，以及在分支中的位置。(认为只有一个开头，只有一个开始语句)
		if (CallGraphParserTools.isIfOrSwitch(stmt)) {
			dealBranches(ug, stmt, currentPos);
		} else {

			if (stmt.containsInvokeExpr()) {
				invokePosi.put(stmt, currentPos);// TODO 更新position中信息
			}

			// 如果不是分支，默认只有一个后继。所以 ：(Stmt)ug.getSuccsOf(stmt).get(0) 。
			if (ug.getSuccsOf(stmt) != null && ug.getSuccsOf(stmt).size() != 0) {
				Position nextPosition = currentPos.deepCopy();
				setOneMethodCallIP(ug, (Stmt) ug.getSuccsOf(stmt).get(0), nextPosition);
			}
		}
	}

	public static void dealBranches(UnitGraph ug, Stmt stmt, Position prePosition) throws Exception {

		Position current;
		List<Unit> sons = ug.getSuccsOf(stmt);
		for (int i = 0; i < sons.size(); i++) {
			current = prePosition.deepCopy();
			current.layer++;
			current.currentBranch = i + 1;
			current.positionInBranch = 1;// 位置初始都为1
			current.layerAndBranch.put(current.layer, current.currentBranch);

			setOneMethodCallIP(ug, (Stmt) sons.get(i), current);
		}
	}

	public static boolean canSetPisition(UnitGraph ug, Stmt stmt) {
		// 看其前驱是否都进行了 位置标记。
		// 都标记则可以开始为此语句进行 位置标记。
		for (Unit preUnit : ug.getPredsOf(stmt)) {
			if (!stmtPosi.keySet().contains(preUnit)) {
				return false;
			}
		}
		return true;
	}

	public static Position findPrePosition(UnitGraph ug, Stmt stmt) {
		Position maxPrePosition = null;
		Unit maxLayerUnit = null;

		// 1.找到前驱中最大的max layer.
		for (Unit preUnit : ug.getPredsOf(stmt)) {

			if (maxPrePosition == null) {
				maxPrePosition = invokePosi.get(preUnit);
				maxLayerUnit = preUnit;
			} else if (maxPrePosition.layer > invokePosi.get(preUnit).layer) {
				maxPrePosition = invokePosi.get(preUnit);
				maxLayerUnit = preUnit;
			}
		}

		// 2.从最大前驱中开始进行迭代，寻找上一个个层次的位置。(use maxLayerUnit)
		return findPreLayerLast(ug, maxLayerUnit, maxPrePosition);
	}

	public static Position findPreLayerLast(UnitGraph ug, Unit unit, Position maxPos) {

		if (unit == null) {
			return maxPos;
		}

		if (CallGraphParserTools.isIfOrSwitch((Stmt) unit) && stmtPosi.get(unit).layer < maxPos.layer) {
			return stmtPosi.get(unit);
		}

		Unit preUnit = ug.getPredsOf(unit).get(0);
		return findPreLayerLast(ug, preUnit, maxPos);

	}

	public static Position getCurrentPosition(Position prePosition) {
		Position currentPos = prePosition;
		// TODO

		return currentPos;
	}

	public static class CallGraph {
		// TODO
	}

	// 用来保存函数的位置信息。
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
