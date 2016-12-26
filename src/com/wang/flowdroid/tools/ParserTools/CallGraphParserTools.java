package flowdroid.tools.ParserTools;

import java.util.List;
import java.util.Map;

import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.internal.JLookupSwitchStmt;
import soot.jimple.internal.JTableSwitchStmt;
import soot.toolkits.graph.UnitGraph;

public class CallGraphParserTools {

	public static boolean isIfOrSwitch(Stmt stmt) {
		if (stmt instanceof IfStmt || stmt instanceof SwitchStmt) {
			return true;
		}
		return false;
	}

	public static StringBuilder addIfCondition(StringBuilder preConditions, Stmt conditionStmt, Stmt currentStmt) {
		StringBuilder tmpSb = new StringBuilder(preConditions.toString());

		if (tmpSb.length() > 0) {
			tmpSb.append(" && ");
		}
		
		if (((IfStmt) conditionStmt).getTarget().equals(currentStmt)) {// right?
			// if条件成立时进入此语句。
			tmpSb.append(((IfStmt) conditionStmt).getCondition());
		} else {
			//加入非.
			tmpSb.append("!").append("(").append(((IfStmt) conditionStmt).getCondition()).append(")");
		}
		return tmpSb;
	}

	public static StringBuilder addSwitchCondition(StringBuilder preConditions, Stmt conditionStmt, Stmt currentStmt)
			throws Exception {
		StringBuilder switchCondition = null;
		if (conditionStmt instanceof JTableSwitchStmt) {
			
			switchCondition = addTableSwitchCondition((JTableSwitchStmt) conditionStmt, currentStmt);
		} else if (conditionStmt instanceof JLookupSwitchStmt) {
			
			switchCondition = addLookupSwitchCondition((JLookupSwitchStmt) conditionStmt,currentStmt);
		}

		// 处理 switch中的条件。
		StringBuilder tmpSb = new StringBuilder(preConditions.toString());

		if (tmpSb.length() > 0) {
			tmpSb.append(" && ");
		}
		
		tmpSb.append(switchCondition.toString());

		return tmpSb;
	}

	private static StringBuilder addTableSwitchCondition(JTableSwitchStmt preConditionStmt, Stmt currentStmt) throws Exception {
		StringBuilder tmpSb = new StringBuilder();
		JTableSwitchStmt jTable = preConditionStmt;
		
		int lowIndex = jTable.getLowIndex();
		int highIndex = jTable.getHighIndex();

		String indexNeeded = null;
		for (int i = lowIndex; i < highIndex; i++) {
			if (currentStmt.equals(jTable.getTarget(i - lowIndex))) {
				indexNeeded = String.valueOf((i - lowIndex));
			}
		}
		if (currentStmt.equals(jTable.getTarget(highIndex - lowIndex))) {
			indexNeeded = String.valueOf((highIndex - lowIndex));
		}
		if (currentStmt.equals(jTable.getDefaultTarget())) {
			indexNeeded = Jimple.DEFAULT;
		}

		if (indexNeeded != null && indexNeeded != "") {
			tmpSb.append("switch == ").append(indexNeeded);
		} else {
			throw new Exception("there is no conditions in switch : " + preConditionStmt);
		}
		
		return tmpSb;
	}

	private static StringBuilder addLookupSwitchCondition(JLookupSwitchStmt preConditionStmt, Stmt currentStmt) throws Exception {
		StringBuilder tmpSb = new StringBuilder();
		JLookupSwitchStmt jLookup = preConditionStmt;
		String indexNeeded = null;
		
		List<IntConstant> lookupValues = jLookup.getLookupValues();
        for (int i = 0; i < lookupValues.size(); i++) {
            Unit target = jLookup.getTarget(i);
            if(currentStmt == target){
            	indexNeeded = lookupValues.get(i).toString();
            }
          }
        if (indexNeeded != null && indexNeeded != "") {
			tmpSb.append("switch == ").append(indexNeeded);
		} else {
			throw new Exception("there is no conditions in switch : " + preConditionStmt);
		}
        return tmpSb;
	}
}
