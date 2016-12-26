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

	// ��¼ÿ������λ����Ϣ��
	protected Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// �������λ����Ϣ��
	protected Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// ��������ĵ�����䣬ת��ΪsootMethod����λ�á� ��һ��method�����ڶദ������ ----> list������λ����Ϣ����
	protected Map<SootMethod, List<Position>> calleePosi = new HashMap<SootMethod, List<Position>>();// �洢ÿ�����ù�ϵ��λ�á�

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
			// ���ÿ����. ���·���λ�á�1.ȷ���߲��λ�á� 2.��ԭ���߲�ε�λ�ý����޸ġ�
			if (canSetPisition(ug, stmt) && !canCombine) {
				Position prePosition = findPrePosition(ug, stmt);
				Position mPosition = prePosition.deepCopy();
				mPosition.positionInBranch++;
				setOneMethodCallIP(ug, stmt, mPosition, COMBINE_YES);
				return;
			}
		}

		// ������Ϣ��
		stmtPosi.put(stmt, currentPos);

		// ���ַ��������ڷ�֧�еĲ�Σ������ķ�֧���Լ��ڷ�֧�е�λ�á�(��Ϊֻ��һ����ͷ��ֻ��һ����ʼ���)
		if (CallGraphParserTools.isIfOrSwitch(stmt)) {
			dealBranches(ug, stmt, currentPos);
		} else {

			if (stmt.containsInvokeExpr() && stmt instanceof InvokeStmt) {
				invokePosi.put(stmt, currentPos);// TODO ����position����Ϣ
			}

			// ������Ƿ�֧��Ĭ��ֻ��һ����̡����� ��(Stmt)ug.getSuccsOf(stmt).get(0) ��
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
			current.positionInBranch = 1;// λ�ó�ʼ��Ϊ1
			current.layerAndBranch.put(current.layer, current.currentBranch);

			setOneMethodCallIP(ug, (Stmt) sons.get(i), current);
		}
	}

	protected boolean canSetPisition(UnitGraph ug, Stmt stmt) {
		// ����ǰ���Ƿ񶼽����� λ�ñ�ǡ�
		// ���������Կ�ʼΪ�������� λ�ñ�ǡ�
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

		// 1.�ҵ�ǰ��������max layer.
		for (Unit preUnit : ug.getPredsOf(stmt)) {

			if (maxPrePosition == null) {
				maxPrePosition = stmtPosi.get(preUnit);
				maxLayerUnit = preUnit;
			} else if (maxPrePosition.layer > stmtPosi.get(preUnit).layer) {
				maxPrePosition = stmtPosi.get(preUnit);
				maxLayerUnit = preUnit;
			}
		}

		// 2.�����ǰ���п�ʼ���е�����Ѱ����һ������ε�λ�á�(use maxLayerUnit)
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
		// key Ϊ��������Ρ�value Ϊ���ڲ�εķ�֧��
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
