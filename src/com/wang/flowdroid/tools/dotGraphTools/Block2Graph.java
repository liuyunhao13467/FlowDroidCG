package flowdroid.tools.dotGraphTools;

import java.io.File;
import java.util.List;
import java.util.Map;

import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Block2Graph {
	private static String outputDir = "D:/";

	public static void createDotGraph(String dotFormat, String fileName) {
		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());
		gv.add(dotFormat);
		gv.addln(gv.end_graph());
		String type = "gif";
		// String type = "pdf";
		// gv.increaseDpi();
		gv.decreaseDpi();
		gv.decreaseDpi();
		gv.decreaseDpi();
		File out = new File(outputDir + fileName + "." + type);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
		System.out.println("graph created !!!");
	}

	public static void dotify(BlockGraph bg, Map<Block, List<Unit>> invokeBlocks, StringBuilder dotResult) {

		System.out.println("dotify ... : " + bg.getBody().getMethod().toString());
		// 放入结点的信息。
		putNodesToDot(bg.getBlocks(), invokeBlocks, dotResult);
		// 放入边的信息。
		putEdgesToDot(bg, dotResult);
		System.out.println("ended ...");
	}

	public static void dotifyOnlyMethod(BlockGraph bg, Map<Block, List<Unit>> invokeBlocks, StringBuilder dotResult) {
		System.out.println("dotify ... : " + bg.getBody().getMethod().toString());
		//TODO
		putMethodInfoToDot(bg.getBlocks(),invokeBlocks,dotResult);
		putEdgesToDot(bg, dotResult);
		System.out.println("ended ...");
	}

	public static void dotifyCompleteBlock(BlockGraph bg, StringBuilder dotResult) {

		System.out.println("dotify ... : " + bg.getBody().getMethod().toString());
		// 放入结点的信息。
		putCompleteBlockToDot(bg.getBlocks(), dotResult);
		// 放入边的信息。
		putEdgesToDot(bg, dotResult);
		System.out.println("ended ...");
	}

	public static void putNodesToDot(List<Block> blocks, Map<Block, List<Unit>> invokeBlocks, StringBuilder dotResult) {

		StringBuilder tmpSb;
		for (Block block : blocks) {
			tmpSb = makeBlockLabel(block, invokeBlocks);
			dotResult.append(block.hashCode()).append(" [label=\"").append(tmpSb.toString()).append("\"];\n");
		}
	}

	public static void putMethodInfoToDot(List<Block> blocks, Map<Block, List<Unit>> invokeBlocks,
			StringBuilder dotResult) {

		StringBuilder tmpSb;
		for (Block block : blocks) {
			tmpSb = makeMethodLabel(block, invokeBlocks);//TODO
			dotResult.append(block.hashCode()).append(" [label=\"").append(tmpSb.toString()).append("\"];\n");
		}
	}

	protected static StringBuilder makeBlockLabel(Block block, Map<Block, List<Unit>> invokeBlocks) {
		StringBuilder tmpSb = new StringBuilder();
		if (invokeBlocks.containsKey(block)) {
			for (Unit u : invokeBlocks.get(block)) {
				tmpSb.append(u.toString().replaceAll("\"", "\\\\\"")).append("\n");
			}
		}

		return tmpSb;
	}
	protected static StringBuilder makeMethodLabel(Block block, Map<Block, List<Unit>> invokeBlocks) {
		StringBuilder tmpSb = new StringBuilder();
		SootMethod method ;
		if (invokeBlocks.containsKey(block)) {
			for (Unit u : invokeBlocks.get(block)) {
				method = ((InvokeStmt)u).getInvokeExpr().getMethod();
				
				tmpSb.append(method.getDeclaringClass().getName())
				.append(" : ")
				.append(method.getDeclaration().replaceAll("\"", "\\\\\""))
				.append("\n");
			}
		}

		return tmpSb;
	}

	public static void putCompleteBlockToDot(List<Block> blocks, StringBuilder dotResult) {
		StringBuilder tmpSb;
		for (Block block : blocks) {
			tmpSb = makeCompleteBlockLabel(block);
			dotResult.append(block.hashCode()).append(" [label=\"").append(tmpSb.toString()).append("\"];\n");
		}
	}

	protected static StringBuilder makeCompleteBlockLabel(Block block) {
		StringBuilder tmpSb = new StringBuilder();

		Unit tmp = block.getHead();
		tmpSb.append(tmp.toString().replaceAll("\"", "\\\\\"")).append("\n");

		while (tmp != block.getTail()) {
			tmp = block.getSuccOf(tmp);
			tmpSb.append(tmp.toString().replaceAll("\"", "\\\\\"")).append("\n");
		}

		return tmpSb;
	}

	public static void putEdgesToDot(BlockGraph bg, StringBuilder dotResult) {
		for (Block block : bg.getBlocks()) {
			for (Block son : bg.getSuccsOf(block)) {
				dotResult.append(block.hashCode()).append(" -> ").append(son.hashCode()).append(";\n");
			}
		}
	}
}
