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
	protected Map<SootMethod, StringBuilder> calleeWithCondition;// 记录被调用函数所处的条件。

	public MethodCallWithCondition(SootMethod method) {
		caller = method;
		calleeWithCondition = new HashMap<SootMethod, StringBuilder>();
		//记录调用者的生成（关键）
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
		
		//debug -- 查看是否去除了异常等信息。
		if(canShowGraph){
			Method2Graph.drawMethodUnitGraph(ug,"dropHead");
		}		
		//debug -- 查看是否移除了环。
		if(canShowGraph){
			Method2Graph.drawMethodUnitGraph(ug,"removeCycle");
		}
		
		//拓扑排序   TODO 错误??
		UnitGraphForTopology ugft = new UnitGraphForTopology(ug);
		Queue<Unit> topologyOrder = ugft.getTopologyOrder();
		
		//条件处理
		Map<Unit,StringBuilder> unit2Conditions = recordConditionStr(ug, topologyOrder);
		System.out.println("getConditions end ~~");
		return getMethodCondition(unit2Conditions);
	}
	
	
	public  Map<Unit,StringBuilder> recordConditionStr(UnitGraph ug,Queue<Unit> topologyOrder){
		
		System.out.println("recordConditionStr start ~~");
		//construct the condition. " (AA && BB) || (CC) "
		Map<Unit,StringBuilder> unit2Conditions = new HashMap<>();
		Set<Unit> done = new HashSet<>();
		
		//按照拓扑顺序进行处理。
		Unit unit ;
		StringBuilder conditionSb;
		while(!topologyOrder.isEmpty()){
			unit = topologyOrder.poll();
			done.add(unit);
			
			conditionSb = new StringBuilder();
			
			 //从前驱那里获得条件信息。（暂且不考虑去重的问题）
			boolean isFirstPre = true;
			for(Unit pre :ug.getPredsOf(unit)){
				if(!isFirstPre){
					conditionSb.append(" || ");
				}
				if(unit2Conditions.get(pre) != null){
					// 加入对于前驱if,switch的判断。
					//前驱如果是分支语句，需要确定此语句所在的分支处的控制流。
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
			
			//在头部尾部在添加一次括号。.(head and tail【可能出现双括号的情况】)
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
	 * 用来删除不会在使用的unit与conditions之间的对应关系。
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
		//将method的调用与条件保留下来。
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
