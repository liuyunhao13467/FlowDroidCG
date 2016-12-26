package flowdroid.test.notUse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flowdroid.entities.MyEdge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CGExporter_copy {
	private Gexf gexf;
	private Graph graph;
	private Attribute codeArray;
	private AttributeList attrList;

	public CGExporter_copy() {
		this.gexf = new GexfImpl();
		this.graph = this.gexf.getGraph();
		this.gexf.getMetadata().setCreator("wang").setDescription("App method invoke graph");
		this.gexf.setVisualization(true);
		this.graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
		this.attrList = new AttributeListImpl(AttributeClass.NODE);
		this.graph.getAttributeLists().add(attrList);
		// 可以给每个节点设置一些属性，这里设置的属性名是 codeArray，实际上后面没用到
		this.codeArray = this.attrList.createAttribute("0", AttributeType.STRING, "codeArray");
	}

	/**
	 * 为call graph分配id,包括为方法分配id,为边分配id.
	 * @param cg
	 */
	public void setIdForCG(CallGraph cg) {
		Map<SootMethod, Integer> method2Id = new HashMap<>();
		List<MyEdge> myEdges = new ArrayList<>();
		
		int nodeId = 0;// 用来表示结点的id.
		int edgeId = 0;// 用来表示边的id.

		Iterator<Edge> edgeIt = cg.iterator();
		while (edgeIt.hasNext()) {
			Edge edge = edgeIt.next();
			SootMethod src = edge.getSrc().method();
			SootMethod tgt = edge.getTgt().method();
			Integer srcId;
			Integer tgtId;

			// 为节点分配 id 。
			if (method2Id.containsKey(src)) {
				srcId = method2Id.get(src);
			} else {
				srcId = nodeId++;
				method2Id.put(src, srcId);
			}

			if (method2Id.containsKey(tgt)) {
				tgtId = method2Id.get(tgt);
			} else {
				tgtId = nodeId++;
				method2Id.put(tgt, tgtId);
			}

			// 为边分配id。
			myEdges.add(new MyEdge(edgeId++, srcId, tgtId));
		}
	}

	public void exportMIG(String graphName, String storeDir) {
		String outPath = storeDir + "/" + graphName + ".gexf";
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(outPath);
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(this.gexf, out, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Node getNodeByID(String Id) {
		List<Node> nodes = this.graph.getNodes();
		Node nodeFinded = null;
		for (Node node : nodes) {
			String nodeID = node.getId();
			if (nodeID.equals(Id)) {
				nodeFinded = node;
				break;
			}
		}
		return nodeFinded;
	}

	public void linkNodeByID(String sourceID, String targetID) {
		Node sourceNode = this.getNodeByID(sourceID);
		Node targetNode = this.getNodeByID(targetID);
		if (sourceNode.equals(targetNode)) {
			return;
		}
		if (!sourceNode.hasEdgeTo(targetID)) {
			String edgeID = sourceID + "-->" + targetID;
			sourceNode.connectTo(edgeID, "", EdgeType.DIRECTED, targetNode);
		}
	}

	public void createNode(String m) {
		String id = m;
		String codes = "";
		if (getNodeByID(id) != null) {
			return;
		}
		Node node = this.graph.createNode(id);
		node.setLabel(id).getAttributeValues().addValue(this.codeArray, codes);
		node.setSize(20);
	}
}
