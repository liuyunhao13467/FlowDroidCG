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

import flowdroid.tools.ParserTools.CallGraphParserTools;
import flowdroid.utils.FileUtils;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class MethodIPInCFG {

	protected SootMethod caller;

	// 记录每条语句的位置信息。
	protected Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// 保存调用位置信息。
	protected Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// 根据上面的调用语句，转化为sootMethod调用位置。 【一个method可能在多处被调用 ----> list来保存位置信息。】
	protected Map<SootMethod, List<Position>> calleePosi = new HashMap<SootMethod, List<Position>>();// 存储每个调用关系的位置。

	public static final int LAYER_START = 1;
	public static final int BRANCH_START = 1;
	public static final int POSITION_IN_BRANCH_START = 1;
	public static final boolean COMBINE_YES = true;

	public MethodIPInCFG(SootMethod method) {
		try {
			caller = method;
			getOneMethodCallWithCFG(method);
			setCalleeWithPosition();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected void getOneMethodCallWithCFG(SootMethod method) throws Exception {
		JimpleBody body = (JimpleBody) method.retrieveActiveBody();
		UnitGraph ug = new BriefUnitGraph(body);
		for (Unit u : ug.getHeads()) {

			Position unusedPos = new Position(1, 1, 1);
			unusedPos.layerAndBranch.put(1, 1);
			setOneMethodCallIP(ug, (Stmt) u, unusedPos);
		}

	}

	protected void setCalleeWithPosition() {
		// TODO test.
		SootMethod tmpMethod;
		for (Unit invoke : invokePosi.keySet()) {
			tmpMethod = ((InvokeStmt) invoke).getInvokeExpr().getMethod();
			if (calleePosi.containsKey(tmpMethod)) {
				calleePosi.get(tmpMethod).add(invokePosi.get(invoke));
			} else {
				List<Position> positions = new ArrayList<Position>();
				positions.add(invokePosi.get(invoke));
				calleePosi.put(tmpMethod, positions);
			}
		}
	}

	protected void setOneMethodCallIP(UnitGraph ug, Stmt stmt, Position path) {
		try {
			setOneMethodCallIP(ug, stmt, path, false);
		} catch (Exception e) {
			FileUtils.outputImportantExceptionInfo("test/error_log/error.txt", e);
			System.exit(0);
		}
	}

	protected void setOneMethodCallIP(UnitGraph ug, Stmt stmt, Position currentPos, boolean canCombine)
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

			if (stmt.containsInvokeExpr() && stmt instanceof InvokeStmt) {
				invokePosi.put(stmt, currentPos);// TODO 更新position中信息
			}

			// 如果不是分支，默认只有一个后继。所以 ：(Stmt)ug.getSuccsOf(stmt).get(0) 。
			if (ug.getSuccsOf(stmt) != null && ug.getSuccsOf(stmt).size() != 0) {
				Position nextPosition = currentPos.deepCopy();
				setOneMethodCallIP(ug, (Stmt) ug.getSuccsOf(stmt).get(0), nextPosition);
			}
		}
	}

	protected void dealBranches(UnitGraph ug, Stmt stmt, Position prePosition) throws Exception {

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

	protected Position findPrePosition(UnitGraph ug, Stmt stmt) {
		Position maxPrePosition = null;
		Unit maxLayerUnit = null;

		// 1.找到前驱中最大的max layer.
		for (Unit preUnit : ug.getPredsOf(stmt)) {

			if (maxPrePosition == null) {
				maxPrePosition = stmtPosi.get(preUnit);
				maxLayerUnit = preUnit;
			} else if (maxPrePosition.layer > stmtPosi.get(preUnit).layer) {
				maxPrePosition = stmtPosi.get(preUnit);
				maxLayerUnit = preUnit;
			}
		}

		// 2.从最大前驱中开始进行迭代，寻找上一个个层次的位置。(use maxLayerUnit)
		return findPreLayerLast(ug, maxLayerUnit, maxPrePosition);
	}

	protected Position findPreLayerLast(UnitGraph ug, Unit unit, Position maxPos) {

		if (unit == null) {
			return maxPos;
		}

		if (CallGraphParserTools.isIfOrSwitch((Stmt) unit)
				&& (stmtPosi.get(unit).layer == 1 || stmtPosi.get(unit).layer < maxPos.layer)) {
			return stmtPosi.get(unit);
		}

		Unit preUnit = null;
		if (ug.getPredsOf(unit) != null && ug.getPredsOf(unit).size() != 0) {
			preUnit = ug.getPredsOf(unit).get(0);
		}
		return findPreLayerLast(ug, preUnit, maxPos);

	}

	public Map<Unit, Position> getStmtPosi() {
		return stmtPosi;
	}

	public Map<Unit, Position> getInvokePosi() {
		return invokePosi;
	}

	public Map<SootMethod, List<Position>> getMethodPosi() {
		return calleePosi;
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
