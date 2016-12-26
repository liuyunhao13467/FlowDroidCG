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

	// ��¼ÿ������λ����Ϣ��
	protected Map<Unit, Position> stmtPosi = new HashMap<Unit, Position>();
	// �������λ����Ϣ��
	protected Map<Unit, Position> invokePosi = new HashMap<Unit, Position>();
	// ��������ĵ�����䣬ת��ΪsootMethod����λ�á� ��һ��method�����ڶദ������ ----> list������λ����Ϣ����
	protected Map<SootMethod, List<Position>> calleePosi = new HashMap<SootMethod, List<Position>>();// �洢ÿ�����ù�ϵ��λ�á�

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

		// !!!������Ϊ�������ĵط��ǳ��˵�һ��ͷ֮�⣬������ͷӦ�ö��ǲ��ɴ�ġ���ֻ��ע�ɴ(wrong)
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
		 * 1.���� worklistMultiSucc �Ķ����С�
		 * 2.���ڶ��п�ʼ���д�����until worklistMultiSucc ��
		 * worklistMultiPre ��Ϊ�գ�
		 * 
		 * ���� worklistMultiSucc �����У�
		 *  2.1 �жϽ������͡� 
		 *  2.1.1 ���֦������֧��� --> ��worklistMultiSucc�н��д���������λ����Ϣ��
		 *  2.1.2 ��ǰ����� -->  ������worklistMultiSucc�����Ƿ���worklistMultiPre�С�
		 *  
		 * ����worklistMultiSucc Ϊ�պ󣬿�ʼ���� worklistMultiPre �е����ݣ�
		 *  2.2 ΪworklistMultiPre �е�Ԫ�ط���λ����Ϣ����������� how?��
		 * 		2.2.1 �ҵ�ǰ������в����ߵĽ�㡣 
		 * 		2.2.2�ҵ�ǰ������и�һ��Ľ�㣬���û�У�����Ϊ�ǣ�����Ľ�㡣
		 * 2.3 worklistMultiPre �е�Ԫ�أ���˳����뵽worklistMultiSucc �С�
		 */
		
		while (worklistMultiSucc.size() != 0 || worklistMultiPre.size() != 0 ) {
			
			succListChangeType = WorklistType.worklistSucc_out;
			preListChangeType = WorklistType.worklistPre_out;

			Unit tmpInMultiSucc ;
			while (worklistMultiSucc.size() != 0) {
				succListChangeType = WorklistType.worklistSucc_in;
				tmpInMultiSucc = worklistMultiSucc.poll();
				
				for(Unit u : ug.getSuccsOf(tmpInMultiSucc)){
					if(ug.getPredsOf(u).size() != 1 && !worklistMultiPre.contains(u) && !stmtPosi.containsKey(u)){//���ܴ���δ����ǵ����u��ǰ���п����в��ɴ�㡣
						worklistMultiPre.add(u);//�������}���롣
						continue;
					}
				}
				
				//����λ�ã����Ҽ��뵽worklist�С�
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
				//ȷ�����е�ǰ������Ѿ������������ͷ����������������ѭ��������
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
				i--;//���ں����Ԫ�ػ�ǰ�ƣ����Ի����´λ��Ǵ����λ�á�
				
				maxPre = findMaxPreLayerUnit(ug, tmpMultiPre);
				Position maxFront =  findPossibleFrontMaxPosition(ug, maxPre);
				//set the current position.�������ǲ��ɴ�ģ�
				Position current = maxFront.deepCopy();
				current.positionInBranch ++;
				stmtPosi.put(tmpMultiPre, current);
				worklistMultiSucc.add(tmpMultiPre);
			}

			if(preListChangeType == WorklistType.worklistPre_in){
				System.out.println("worked in worklist_pre");
			}
			
			//��ֹ��ѭ����
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
			current.currentBranch = i + 1;//branch��֧��ʼλ��Ϊ1.
			current.positionInBranch = 1;// λ�ó�ʼ��Ϊ1
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
		// ����ǰ���Ƿ񶼽����� λ�ñ�ǡ�
		// ���������Կ�ʼΪ�������� λ�ñ�ǡ�
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
