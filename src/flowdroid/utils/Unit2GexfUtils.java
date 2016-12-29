package flowdroid.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import flowdroid.entities.MyEdge;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;

/*
 * ������һ��UnitGraph������Ӧ��gexf�ļ���
 */
public class Unit2GexfUtils {

	public static void exportUnitGraphGexf(UnitGraph ug,String path){
		Map<Unit,Integer> unitIps = new HashMap<>();
		List<MyEdge> edges = new ArrayList<MyEdge>();
		
		Iterator<Unit> unitIt = ug.iterator();
		//Ϊ�ڵ����ip.
		Integer nodeIp = 1;
		while(unitIt.hasNext()){
			Unit tmp = unitIt.next();
			unitIps.put(tmp, nodeIp++);
		}
		
		//Ϊ�߷���ip,�Լ���Ӧ��ϵ��
		unitIt = ug.iterator();
		Integer edgeIp =1;
		while(unitIt.hasNext()){
			Unit tmp = unitIt.next();
			for(Unit son :ug.getSuccsOf(tmp)){
				edges.add(new MyEdge(edgeIp, unitIps.get(tmp),unitIps.get(son)));
				edgeIp++;
			}
		}
		outputInfo(changeToGexf(unitIps, edges).toString(),path);
	}
	
	public static void outputInfo(String data,String path){
		File file = new File(path);
		if(!file.exists()){
			try {
				file.createNewFile();
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(data);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		
	}
	
	public static StringBuilder changeToGexf(Map<Unit,Integer> unitIps,List<MyEdge> edges){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buff.append("<gexf xmlns=\"http://www.gephi.org/gexf\" xmlns:viz=\"http://www.gephi.org/gexf/viz\">\n");
		buff.append("<graph type=\"static\">\n");
		buff.append("<attributes class=\"node\" type=\"static\">\n" );
		buff.append("<attribute default=\"normal\" id=\"0\" title=\"type\" type=\"string\"/>\n");
		buff.append("<attribute id=\"1\" title=\"class_name\" type=\"string\"/>\n");
		buff.append("<attribute id=\"2\" title=\"method_name\" type=\"string\"/>\n");
		buff.append("<attribute id=\"3\" title=\"descriptor\" type=\"string\"/>\n");
		buff.append("<attribute default=\"0\" id=\"4\" title=\"permissions\" type=\"integer\"/>\n");
		buff.append("<attribute default=\"normal\" id=\"5\" title=\"permissions_level\" type=\"string\"/>\n");
		buff.append("<attribute default=\"false\" id=\"6\" title=\"dynamic_code\" type=\"boolean\"/>\n");
		buff.append("</attributes>\n" );
		buff.append("<nodes>\n");
		for(Unit unit : unitIps.keySet()){
			buff.append(String.format("<node id=\"%d\" label=\"%s\">\n", unitIps.get(unit),suitXmlFormat(unit.toString())));
			//TODO ����һЩ�������ԡ�������androguard�з���һ����
			buff.append("</node>\n");
		}		
		buff.append("</nodes>\n");
		
		buff.append("<edges>\n");
		for(MyEdge myEdge : edges){
			buff.append(String.format("<edge id=\"%d\" source=\"%d\" target=\"%d\"/>\n", myEdge.getIp(),myEdge.getSrc(),myEdge.getTgt()));
		}
		buff.append("</edges>\n");
		buff.append("</graph>\n");
		buff.append("</gexf>");
		return buff;
	}
	
	/*
	 * ��һЩ�ַ����᲻����xml�ĸ�ʽ���ַ���Ҫ����Ҫ��һЩ����ʶ��Ĵʻ㣨�����֣���
	 * ʹ��ʵ���������滻��
	 */
	public static String suitXmlFormat(String data){
		data = data.replaceAll("<", "&lt;");
		data = data.replaceAll(">", "&gt;");
		data = data.replaceAll("&", "&amp;");
		data = data.replaceAll("\"", "'");
		return data;
	}
}
