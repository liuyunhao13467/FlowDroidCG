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
 * ����Ŀ�ģ�
 * 1.ͨ���ݹ�ķ�ʽ��Ϊÿ��UnitGraph�е� ������IP��
 * 
 * �������⣺
 * 1.�ݹ�Ľṹ���ڸ��ӣ��߼����ҡ���������Ե��ԣ�
 * 2.û�������˽�UnitGraph�еĽṹ��
 * 
 * ���ۣ�
 * ���������ַ�����
 * @author wang
 *
 */
public class setIPForUnitInUnitGraph {
	// ��¼ÿ������λ����Ϣ��
	public static Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// �������λ����Ϣ��
	public static Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// ��������ĵ�����䣬ת��ΪsootMethod����λ�á� ��һ��method�����ڶദ������ ----> list������λ����Ϣ����
	public static Map<SootMethod, List<Position>> methodPosi = new HashMap<SootMethod, List<Position>>();// �洢ÿ�����ù�ϵ��λ�á�

	// �Ƿ���Ի���ö�٣�
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

			if (stmt.containsInvokeExpr()) {
				invokePosi.put(stmt, currentPos);// TODO ����position����Ϣ
			}

			// ������Ƿ�֧��Ĭ��ֻ��һ����̡����� ��(Stmt)ug.getSuccsOf(stmt).get(0) ��
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
			current.positionInBranch = 1;// λ�ó�ʼ��Ϊ1
			current.layerAndBranch.put(current.layer, current.currentBranch);

			setOneMethodCallIP(ug, (Stmt) sons.get(i), current);
		}
	}

	public static boolean canSetPisition(UnitGraph ug, Stmt stmt) {
		// ����ǰ���Ƿ񶼽����� λ�ñ�ǡ�
		// ���������Կ�ʼΪ�������� λ�ñ�ǡ�
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

		// 1.�ҵ�ǰ��������max layer.
		for (Unit preUnit : ug.getPredsOf(stmt)) {

			if (maxPrePosition == null) {
				maxPrePosition = invokePosi.get(preUnit);
				maxLayerUnit = preUnit;
			} else if (maxPrePosition.layer > invokePosi.get(preUnit).layer) {
				maxPrePosition = invokePosi.get(preUnit);
				maxLayerUnit = preUnit;
			}
		}

		// 2.�����ǰ���п�ʼ���е�����Ѱ����һ������ε�λ�á�(use maxLayerUnit)
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

	// �������溯����λ����Ϣ��
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
