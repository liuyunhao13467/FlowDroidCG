package flowdroid.utils;

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
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

public class UnitGraphExporter {
	private Gexf gexf;
	private Graph graph;
	private Attribute unitType;
	private AttributeList attrList;
	private UnitGraph ug;

	protected Map<Unit, Integer> unit2Id = new HashMap<>();
	protected List<MyEdge> edges = new ArrayList<MyEdge>();
	protected String methodInfo;
	protected Map<String, Integer> fileCount = new HashMap<>();


	public UnitGraphExporter(UnitGraph ug) {
		this.gexf = new GexfImpl();
		this.graph = this.gexf.getGraph();
		this.ug = ug;
		this.gexf.getMetadata().setCreator("wang");
		methodInfo = ug.getBody().getMethod().getSignature();
		this.gexf.getMetadata().setDescription(methodInfo);
		this.gexf.setVisualization(true);
		this.graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
		this.attrList = new AttributeListImpl(AttributeClass.NODE);
		this.graph.getAttributeLists().add(attrList);
		// 可以给每个节点设置一些属性，这里设置的属性名是 codeArray，实际上后面没用到
		this.unitType = this.attrList.createAttribute("0", AttributeType.STRING, "unitType");
	}

	public void createGraph(){
		setIdForUG(ug);
		//画点。
		createNodes();
		//画边。
		createEdges();
	}
	
	public void exportMIG(String graphName, String storeDir) {
		graphName = graphName.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		String outPath = storeDir + "/" + graphName + ".gexf";
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		
		Writer out;
		try {
			File dir = new File(storeDir);
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			
			File f = createFileWithoutRepeat(outPath);
			out = new FileWriter(f, false);
			graphWriter.writeToStream(this.gexf, out, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 存在的问题，所在的文件夹最初的时候需要是空的。
	 */
	public File createFileWithoutRepeat(String path) throws IOException {
		File file = new File(path);
		if (!file.exists()) {
			try{
				file.createNewFile();
			}catch(Exception e){
				System.out.println("problem file name path is  :" + file.getName());
				e.printStackTrace();
			}
			fileCount.put(file.getName(), 1);
			return file;
		} else {
			if (!fileCount.containsKey(file.getName())) {
				fileCount.put(file.getName(), 1);
			}

			int count = fileCount.get(file.getName());
			File newFile = new File(FileUtils.getNewName(path, ".gexf", ++count));
			newFile.createNewFile();
			return newFile;
		}
	}
	
	public void setIdForUG(UnitGraph ug){
		setIdForUG(ug,this.unit2Id,this.edges);
	}
	
	/*
	 * 为语句以及语句之间的关系，分配id.
	 */
	public static void setIdForUG(UnitGraph ug,Map<Unit, Integer> unit2Id,List<MyEdge> edges) {
		Integer nodeIp = 1;
		Integer edgeIp = 1;

		// 为节点分配ip.
		Iterator<Unit> unitIt = ug.iterator();
		while (unitIt.hasNext()) {
			Unit tmp = unitIt.next();
			unit2Id.put(tmp, nodeIp++);
		}

		// 为边分配ip,以及对应关系。
		unitIt = ug.iterator();
		while (unitIt.hasNext()) {
			Unit current = unitIt.next();
			for (Unit son : ug.getSuccsOf(current)) {
				edges.add(new MyEdge(edgeIp++, unit2Id.get(current), unit2Id.get(son)));
			}
		}
	}

	public void setFileCount(Map<String, Integer> fileCount){
		this.fileCount = fileCount;
	}
	
	protected void createNodes(){
		Iterator<Unit> unitIt = unit2Id.keySet().iterator();
		while(unitIt.hasNext()){
			Unit unit = unitIt.next();
			createNode(unit);
		}
	}
	
	protected void createEdges(){
		for(MyEdge myEdge : edges){
			linkNodeByID(myEdge);
		}
	}
	
	
	protected void createNode(Unit unit){
		
		String id = unit2Id.get(unit).toString();
		String unitInfo = unit.toString();
		if (getNodeByID(id) != null) {
			return;
		}
		Node node = this.graph.createNode(id);
		node.setLabel(unitInfo).getAttributeValues().addValue(this.unitType, unit.getClass().toString());
		node.setSize(20);
	}
	
	protected void linkNodeByID(MyEdge myEdge) {
		String srcId = myEdge.getSrc().toString();
		String tgtId = myEdge.getTgt().toString();
		Node sourceNode = this.getNodeByID(srcId);
		Node targetNode = this.getNodeByID(tgtId);
		
		if (!sourceNode.hasEdgeTo(tgtId)) {
			sourceNode.connectTo(myEdge.getIp().toString(), "", EdgeType.DIRECTED, targetNode);
		}
	}
	
	protected Node getNodeByID(String Id) {
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
	
}
