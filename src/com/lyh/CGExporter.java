package com.lyh;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CGExporter {
	private Gexf gexf;
    private Graph graph;
    private Attribute codeArray;
    private AttributeList attrList;

    public CGExporter() {
        this.gexf = new GexfImpl();
        this.graph = this.gexf.getGraph();
        this.gexf.getMetadata().setCreator("liu3237").setDescription("App method invoke graph");
        this.gexf.setVisualization(true);
        this.graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
        this.attrList = new AttributeListImpl(AttributeClass.NODE);
        this.graph.getAttributeLists().add(attrList);
        //可以给每个节点设置一些属性，这里设置的属性名是 codeArray，实际上后面没用到
        this.codeArray = this.attrList.createAttribute("0", AttributeType.STRING,"codeArray");
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
