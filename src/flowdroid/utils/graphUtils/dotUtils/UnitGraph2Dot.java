package flowdroid.utils.graphUtils.dotUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flowdroid.entities.graph.MyBriefUnitGraph;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;

public class UnitGraph2Dot {
	private static String outputDir = "D:/";

	public static void createDotGraph(String dotFormat, String fileName) {
		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.add(dotFormat);
		gv.addln(gv.end_graph());
		// String type = "gif";
		String type = "png";
		// String type = "pdf";
		// gv.increaseDpi();
		gv.decreaseDpi();
		gv.decreaseDpi();
		gv.decreaseDpi();
		File out = new File(outputDir + fileName + "." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		System.out.println("graph created !!!");
	}

	public static void drawMethodUnitGraph(SootMethod method) {
		drawMethodUnitGraph(method, "v1");
	}

	public static void drawMethodUnitGraph(SootMethod method, String phase) {
		StringBuilder dotSb = new StringBuilder();
		System.out.println(method.retrieveActiveBody().toString());
		UnitGraph2Dot.dotify(method, dotSb);
		UnitGraph2Dot.createDotGraph(dotSb.toString(),
				method.getDeclaringClass() + "_" + method.getName() + "_" + phase);
	}

	public static void drawMethodUnitGraph(UnitGraph ug) {
		drawMethodUnitGraph(ug, "v1");
	}
	public static void drawMethodUnitGraph(UnitGraph ug,String phaseTag){
		StringBuilder dotSb = new StringBuilder();
		putNodesToDot(ug, dotSb);
		putEdgesToDot(ug, dotSb);
		UnitGraph2Dot.createDotGraph(dotSb.toString(),
				ug.getBody().getMethod().getDeclaringClass() + "_" + ug.getBody().getMethod().getName()+"_"+phaseTag);
	}

	public static void dotify(SootMethod method, StringBuilder dotResult) {
		System.out.println("dotify : " + method.getName());
		 UnitGraph ug = new BriefUnitGraph(method.retrieveActiveBody());
		// UnitGraph ug = new ExceptionalUnitGraph(method.retrieveActiveBody());
		// UnitGraph ug = new TrapUnitGraph(method.retrieveActiveBody());
		// UnitGraph ug = new EnhancedUnitGraph(method.retrieveActiveBody());
//		UnitGraph ug = new MyBriefUnitGraph(method.retrieveActiveBody());//存在问题！！！
		PatchingChain<Unit> units = method.retrieveActiveBody().getUnits();
		// 放入结点的信息。
		putNodesToDot(units, dotResult);
		// 放入边的信息。
		putEdgesToDot(units, ug, dotResult);
	}

	public static void dotify(Set<Unit> units, UnitGraph ug, StringBuilder dotResult) {
		// 放入结点的信息。
		putNodesToDot(units, dotResult);
		// 放入边的信息。
		putEdgesToDot(units, ug, dotResult);
	}

	public static void dotify2Bigraph(UnitGraph ug, Map<Unit, Set<Unit>> unitConditions, StringBuilder dotResult) {
		SootMethod caller = ug.getBody().getMethod();
		// 插入结点的信息。
		dotResult.append(caller.hashCode()).append(" [label=\"").append(caller.toString().replaceAll("\"", "\\\\\""))
				.append("\"];\n");
		for (Unit u : unitConditions.keySet()) {
			if ((Stmt) u instanceof InvokeStmt) {
				// 针对调用方法进行单独的处理。
				dotResult.append(u.hashCode()).append(" [label=\"").append(u.toString().replaceAll("\"", "\\\\\""))
						.append("\"];\n");

				if (unitConditions.get(u).size() == 0) {
					continue;
				}
				// 为条件信息分配结点
				dotResult.append(unitConditions.get(u).hashCode()).append(" [label=\"")
						.append(getFormalCondition(unitConditions.get(u))).append("\"").append(" shape=").append("box")
						.append(" color=").append("red").append("];\n");
			}
		}

		// 插入边信息。
		for (Unit u : unitConditions.keySet()) {
			if ((Stmt) u instanceof InvokeStmt) {
				if (unitConditions.get(u).size() == 0) {
					// 拼接边
					dotResult.append(caller.hashCode()).append(" -> ").append(u.hashCode()).append(";\n");
				} else {
					// 调用者 --> 条件
					dotResult.append(caller.hashCode()).append(" -> ").append(unitConditions.get(u).hashCode())
							.append(";\n");
					// 条件 --> 被调用者
					dotResult.append(unitConditions.get(u).hashCode()).append(" -> ").append(u.hashCode())
							.append(";\n");

				}
			}
		}
	}

	// format the conditions.
	public static StringBuilder getFormalCondition(Set<Unit> conditions) {
		StringBuilder sb = new StringBuilder();
		Iterator<Unit> conIt = conditions.iterator();
		while (conIt.hasNext()) {
			Stmt condition = (Stmt) conIt.next();
			if (condition instanceof IfStmt) {
				sb.append(((IfStmt) condition).getCondition().toString().replaceAll("\"", "\\\\\"")).append("\n");
				System.out.println("the condition is : " + ((IfStmt) condition).getCondition());
				System.out.println("the condition box is : " + ((IfStmt) condition).getConditionBox());
			} else if (condition instanceof SwitchStmt) {
				// TODO switch语句应该怎么样处理。
				// ((SwitchStmt)condition).get
			}
		}
		return sb;
	}

	public static void putNodesToDot(UnitGraph ug, StringBuilder dotResult) {
		Iterator<Unit> unitsIt = ug.iterator();
		while (unitsIt.hasNext()) {
			Unit unit = unitsIt.next();
			dotResult.append(unit.hashCode()).append(" [label=\"").append(unit.toString().replaceAll("\"", "\\\\\""))
					.append("\"];\n");
		}
	}

	public static void putNodesToDot(PatchingChain<Unit> units, StringBuilder dotResult) {
		for (Unit unit : units) {
			dotResult.append(unit.hashCode()).append(" [label=\"").append(unit.toString().replaceAll("\"", "\\\\\""))
					.append("\"];\n");
		}
	}

	public static void putNodesToDot(Set<Unit> units, StringBuilder dotResult) {
		Iterator<Unit> unitsIt = units.iterator();
		while (unitsIt.hasNext()) {
			Unit unit = unitsIt.next();
			dotResult.append(unit.hashCode()).append(" [label=\"").append(unit.toString().replaceAll("\"", "\\\\\""))
					.append("\"];\n");
		}
	}

	public static void putEdgesToDot(UnitGraph ug, StringBuilder dotResult) {
		Iterator<Unit> unitsIt = ug.iterator();
		while (unitsIt.hasNext()) {
			Unit unit = unitsIt.next();
			for (Unit son : ug.getSuccsOf(unit)) {
				dotResult.append(unit.hashCode()).append(" -> ").append(son.hashCode()).append(";\n");
			}
		}
	}

	public static void putEdgesToDot(PatchingChain<Unit> units, UnitGraph ug, StringBuilder dotResult) {
		for (Unit unit : units) {
			for (Unit son : ug.getSuccsOf(unit)) {
				dotResult.append(unit.hashCode()).append(" -> ").append(son.hashCode()).append(";\n");
			}
		}
	}

	public static void putEdgesToDot(Set<Unit> units, UnitGraph ug, StringBuilder dotResult) {
		Iterator<Unit> unitsIt = units.iterator();
		while (unitsIt.hasNext()) {
			Unit unit = unitsIt.next();
			for (Unit son : ug.getSuccsOf(unit)) {
				dotResult.append(unit.hashCode()).append(" -> ").append(son.hashCode()).append(";\n");
			}
		}
	}
}
