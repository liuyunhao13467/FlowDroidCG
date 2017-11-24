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
	/* 阈值 */
	// public static double MAX = 0.00000000001;
	public static double MAX = 0.00000000001;
	/* 阻尼系数 */
	public static double alpha = 0.85;
	public static int NODE_SIZE = 5;

	private static Map<Integer, List<Integer>> outEdge = new HashMap<Integer, List<Integer>>();
	private static Map<Integer, Double> pageRank = new HashMap<Integer, Double>();
	private static Map<Integer, Double> prePageRank = new HashMap<Integer, Double>();

	private static double[] init = new double[NODE_SIZE];

	public static void main(String[] args) {
		// 加载要进行计算的页面，将pageMap，pageList，init等进行初始化。
		// PageEntity的pr属性无值外，其它属性已经初始化完毕。
		loadData();
		// 使用PageRank算法对pr进行初始化
		pageRank = doPageRank();

		// 进行Pagerank计算，只要值超出界限，就停止计算，保留结果。
		while (!(checkMax(pageRank, prePageRank))) {
			//深层复制Map。
			copyPR(pageRank);
			pageRank = doPageRank();
		}
		showPR();
	}

	private static void copyPR(Map<Integer,Double> pr){
		//Integer,Double是传值。
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
				//放入每个pr中。
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
		//后续处理，为每个值，设置阻尼。
				Set prKeys = pr.keySet();
				Iterator<Integer> prKeysIt = prKeys.iterator();
				while(prKeysIt.hasNext()){
					Integer prTmp = prKeysIt.next();
					//计算公式。
					pr.put(prTmp, (pr.get(prTmp))*(1 - alpha) +alpha);
				}
	}
	
	private static void loadData() {
		//加载数据 到 apk,outEdge,pageRank初始化。
		MySQLCor mysql;
		Apk apk = null;
		try {
			mysql = new MySQLCor(TestDBConnect.DB_URL_LOCAL, TestDBConnect.USER_NAME,TestDBConnect.USER_PWD);
			 apk = TestDBConnect.data2ApkEntity(mysql);
			outEdge = TestDBConnect.getOutEdges(apk);
			
			//pageRank初始化。
			Iterator<Method> methodIt = apk.getMethods().iterator();
			while(methodIt.hasNext()){
				prePageRank.put(methodIt.next().getMethodID().getIDInApk(), 1.0);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean checkMax(Map<Integer, Double> prePR, Map<Integer, Double> PR) {
		// 是基于距离的。
		double distance = calDistance(prePR, PR);

		if (distance < MAX) {
			return true;
		}
		return false;
	}

	// TODO 计算向量距离。
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
		// TODO 添加数据。（from database）
	}

	private static void showPR() {
		// 需要在此基础上进一步分析。
		Set methodID = pageRank.keySet();
		Iterator<Integer> it = methodID.iterator();
		while (it.hasNext()) {
			Integer tmp = it.next();
			System.out.println("结点 " + tmp + " 的pagerank值 为：" + pageRank.get(tmp));
		}
	}
}