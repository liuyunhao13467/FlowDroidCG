package flowdroid.algorithm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import flowdroid.db.MySQLCor;
import flowdroid.entities.Apk;
import flowdroid.entities.Method;
import flowdroid.test.dbTest.TestDBConnect;

public class PageRankDemo {
	/* ��ֵ */
	// public static double MAX = 0.00000000001;
	public static double MAX = 0.00000000001;
	/* ����ϵ�� */
	public static double alpha = 0.85;
	public static int NODE_SIZE = 5;

	private static Map<Integer, List<Integer>> outEdge = new HashMap<Integer, List<Integer>>();
	private static Map<Integer, Double> pageRank = new HashMap<Integer, Double>();
	private static Map<Integer, Double> prePageRank = new HashMap<Integer, Double>();

	private static double[] init = new double[NODE_SIZE];

	public static void main(String[] args) {
		// ����Ҫ���м����ҳ�棬��pageMap��pageList��init�Ƚ��г�ʼ����
		// PageEntity��pr������ֵ�⣬���������Ѿ���ʼ����ϡ�
		loadData();
		// ʹ��PageRank�㷨��pr���г�ʼ��
		pageRank = doPageRank();

		// ����Pagerank���㣬ֻҪֵ�������ޣ���ֹͣ���㣬���������
		while (!(checkMax(pageRank, prePageRank))) {
			//��㸴��Map��
			copyPR(pageRank);
			pageRank = doPageRank();
		}
		showPR();
	}

	private static void copyPR(Map<Integer,Double> pr){
		//Integer,Double�Ǵ�ֵ��
		prePageRank.clear();
		Iterator<Integer> prIt = pr.keySet().iterator();
		while(prIt.hasNext()){
			Integer tmpKey = prIt.next();
			prePageRank.put(tmpKey,pr.get(tmpKey));
		}
	} 

	private static Map<Integer,Double> doPageRank() {
		Map<Integer, Double> pr = new HashMap<Integer, Double>();
		
		Iterator<Integer> preIt = prePageRank.keySet().iterator();
		while(preIt.hasNext()){
			Integer tmpKey = preIt.next();
			if(!outEdge.containsKey(tmpKey)){
				pr.put(tmpKey, 0.0);
				continue;
			}
			
			Iterator<Integer> edgeIt = outEdge.get(tmpKey).iterator();
			
			while(edgeIt.hasNext()){
				Integer tmpEdgeKey = edgeIt.next();
				prePageRank.get(tmpEdgeKey);
				//����ÿ��pr�С�
				Double partOfPR =  prePageRank.get(tmpKey)/outEdge.get(tmpKey).size();
				
				if(pr.containsKey(tmpEdgeKey)){
					pr.put(tmpEdgeKey, pr.get(tmpEdgeKey) + partOfPR);
				}else{
					pr.put(tmpEdgeKey, partOfPR);
				}
				
			}
		}
		
		setDamping(pr);
		return pr ;
	}
	private static void setDamping(Map<Integer, Double> pr){
		//��������Ϊÿ��ֵ���������ᡣ
				Set prKeys = pr.keySet();
				Iterator<Integer> prKeysIt = prKeys.iterator();
				while(prKeysIt.hasNext()){
					Integer prTmp = prKeysIt.next();
					//���㹫ʽ��
					pr.put(prTmp, (pr.get(prTmp))*(1 - alpha) +alpha);
				}
	}
	
	private static void loadData() {
		//�������� �� apk,outEdge,pageRank��ʼ����
		MySQLCor mysql;
		Apk apk = null;
		try {
			mysql = new MySQLCor(TestDBConnect.DB_URL_LOCAL, TestDBConnect.USER_NAME,TestDBConnect.USER_PWD);
			 apk = TestDBConnect.data2ApkEntity(mysql);
			outEdge = TestDBConnect.getOutEdges(apk);
			
			//pageRank��ʼ����
			Iterator<Method> methodIt = apk.getMethods().iterator();
			while(methodIt.hasNext()){
				prePageRank.put(methodIt.next().getMethodID().getIDInApk(), 1.0);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkMax(Map<Integer, Double> prePR, Map<Integer, Double> PR) {
		// �ǻ��ھ���ġ�
		double distance = calDistance(prePR, PR);

		if (distance < MAX) {
			return true;
		}
		return false;
	}

	// TODO �����������롣
	public static double calDistance( Map<Integer, Double> prePR,Map<Integer, Double> PR){
		double sum =0;
		
		Set preKey = prePR.keySet();
		Set key = PR.keySet();
		if(preKey.size() != key.size()){
			return -1;
		}
		
		Iterator<Integer> it = preKey.iterator();
		while(it.hasNext()){
			Integer tmpKey = it.next();
			sum += Math.pow(prePR.get(tmpKey), PR.get(tmpKey));
		}
		return Math.sqrt(sum);
	}

	private static void inputRelation() {
		// TODO ������ݡ���from database��
	}

	private static void showPR() {
		// ��Ҫ�ڴ˻����Ͻ�һ��������
		Set methodID = pageRank.keySet();
		Iterator<Integer> it = methodID.iterator();
		while (it.hasNext()) {
			Integer tmp = it.next();
			System.out.println("��� " + tmp + " ��pagerankֵ Ϊ��" + pageRank.get(tmp));
		}
	}
}