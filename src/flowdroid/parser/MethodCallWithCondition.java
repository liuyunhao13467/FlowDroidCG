package flowdroid.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import flowdroid.entities.Method;
import flowdroid.entities.graph.MyBriefUnitGraph;
import flowdroid.entities.graph.UnitGraphForTopology;
import flowdroid.utils.CallGraphTools;
import flowdroid.utils.graphUtils.Method2Graph;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.internal.AbstractSwitchStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.toolkits.graph.UnitGraph;

public class MethodCallWithCondition {
	
	

	protected SootMethod caller;
	protected Map<SootMethod, StringBuilder> calleeWithCondition;// ��¼�����ú���������������

	public MethodCallWithCondition(SootMethod method) {
		caller = method;
		calleeWithCondition = new HashMap<SootMethod, StringBuilder>();
		//��¼�����ߵ����ɣ��ؼ���
		this.calleeWithCondition = dealConditions(method);
	}
	
	public Map<SootMethod, StringBuilder> getConditions(){
		return calleeWithCondition;
	}
	
	public SootMethod getCaller(){
		return this.caller;
	}
	
	
	private  Map<SootMethod, StringBuilder> dealConditions(SootMethod method){
		//debug
		boolean canShowGraph = false;
		if(method.getName().equals("generate")){
			canShowGraph = true;
		}
		
		System.out.println("getConditions start ~~");
		UnitGraph ug = new MyBriefUnitGraph(method.retrieveActiveBody());
		
		//debug -- �鿴�Ƿ�ȥ�����쳣����Ϣ��
		if(canShowGraph){
			Method2Graph.drawMethodUnitGraph(ug,"dropHead");
		}		
		//debug -- �鿴�Ƿ��Ƴ��˻���
		if(canShowGraph){
			Method2Graph.drawMethodUnitGraph(ug,"removeCycle");
		}
		
		//��������   TODO ����??
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = ugft.getTopologyOrder();
		
		//��������
		Map<Unit,StringBuilder> unit2Conditions = recordConditionStr(ug, topologyOrder);
		System.out.println("getConditions end ~~");
		return getMethodCondition(unit2Conditions);
	}
	
	
	public  Map<Unit,StringBuilder> recordConditionStr(UnitGraph ug,Queue<Unit> topologyOrder){
		
		System.out.println("recordConditionStr start ~~");
		//construct the condition. " (AA && BB) || (CC) "
		Map<Unit,StringBuilder> unit2Conditions = new HashMap<>();
		Set<Unit> done = new HashSet<>();
		
		//��������˳����д���
		Unit unit ;
		StringBuilder conditionSb;
		while(!topologyOrder.isEmpty()){
			unit = topologyOrder.poll();
			done.add(unit);
			
			conditionSb = new StringBuilder();
			
			 //��ǰ��������������Ϣ�������Ҳ�����ȥ�ص����⣩
			boolean isFirstPre = true;
			for(Unit pre :ug.getPredsOf(unit)){
				if(!isFirstPre){
					conditionSb.append(" || ");
				}
				if(unit2Conditions.get(pre) != null){
					// �������ǰ��if,switch���жϡ�
					//ǰ������Ƿ�֧��䣬��Ҫȷ����������ڵķ�֧���Ŀ�������
					if((Stmt)pre instanceof JIfStmt){
						conditionSb.append("(")
						.append(CallGraphTools.addIfCondition(unit2Conditions.get(pre), (Stmt)pre,(Stmt)unit))
						.append(")");
						
					}else if(pre instanceof SwitchStmt){
							try {
								conditionSb.append("(")
								.append(CallGraphTools.addSwitchCondition(unit2Conditions.get(pre), (Stmt)pre,(Stmt)unit))
								.append(")");
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(0);
							}
					}else if(!unit2Conditions.get(pre).toString().equals("")){
						conditionSb.append("(")
						.append(unit2Conditions.get(pre))
						.append(")");
					}
				}
				isFirstPre =false;
			}
			
			//��ͷ��β�������һ�����š�.(head and tail�����ܳ���˫���ŵ������)
			if(ug.getPredsOf(unit) != null &&ug.getPredsOf(unit).size() > 1){
				conditionSb.insert(0, "(");
				conditionSb.append(")");
			}
			unit2Conditions.put(unit, conditionSb);
		}
		
		System.out.println("recordConditionStr end ~~");
		return unit2Conditions;
	}
	
	/**
	 * ����ɾ��������ʹ�õ�unit��conditions֮��Ķ�Ӧ��ϵ��
	 * @param ug
	 * @param unit2Conditions
	 * @param done
	 */
	protected void clearUnit2Conditions(UnitGraph ug,Map<Unit,StringBuilder> unit2Conditions,Set<Unit> done){
		Iterator<Unit> unitsIt = unit2Conditions.keySet().iterator();
		Unit current;
		while(unitsIt.hasNext()){
			current = unitsIt.next();
			if(((Stmt)current).containsInvokeExpr()){
				continue;
			}
			
			Boolean canDelete = true;
			for(Unit succ : ug.getSuccsOf(current)){
				if(!done.contains(succ)){
					canDelete = false;
					break;
				}
			}
			
			if(canDelete){
				unitsIt.remove();
			}
			
		}
		
	}
	
	private  Map<SootMethod,StringBuilder> getMethodCondition(Map<Unit,StringBuilder> unit2Conditions){
		System.out.println("getMethodCondition start ~~");
		//��method�ĵ�������������������
		Map<SootMethod,StringBuilder> method2Conditions = new HashMap<>();
		StringBuilder sbCondition;
		SootMethod tmpMethod;
		for(Unit unit : unit2Conditions.keySet()){
			if(((Stmt)unit).containsInvokeExpr()){
				sbCondition = new StringBuilder(unit2Conditions.get(unit));
				tmpMethod = ((Stmt)unit).getInvokeExpr().getMethod();
				
				if(method2Conditions.containsKey(tmpMethod) && method2Conditions.get(tmpMethod).length()!= 0){
					if(sbCondition.toString().equals("")){
						method2Conditions.get(tmpMethod).append(" || ").append("ANY");
					}else{
						method2Conditions.get(tmpMethod).append(" || ").append(sbCondition);
					}
				}else{
					if(sbCondition.toString().equals("")){
						method2Conditions.put(tmpMethod,new StringBuilder("ANY"));
					}else{
						method2Conditions.put(tmpMethod, sbCondition);
					}
				}
			}
		}
		
		System.out.println("getMethodCondition end ~~");
		return method2Conditions;
	}
}
