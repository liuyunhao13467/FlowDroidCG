package flowdroid.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * apk����Ϣ�������ݿ⡣
 */
public class ApkFromSql {
	//Ӧ���ܹ�������ӵ�еİ����࣬�������ߣ�
	private String ApkName;
	private String ApkVersion;
	private Set<MyEdge> edges = new HashSet<MyEdge>();
	private Set<MethodFromSql> methods = new HashSet<MethodFromSql>();
	private Map<Integer,MethodFromSql> indexOfMethods = new HashMap<Integer, MethodFromSql>();
	//TODO ������Ķ�Ӧ��ϵ�����뷽���Ķ�Ӧ��ϵ������Ҫ�ó�Ա������ʾ������
	
	
	public String getApkName() {
		return ApkName;
	}
	public void setApkName(String apkName) {
		ApkName = apkName;
	}
	public String getApkVersion() {
		return ApkVersion;
	}
	public void setApkVersion(String apkVersion) {
		ApkVersion = apkVersion;
	}
	public Set<MyEdge> getEdges() {
		return edges;
	}
	public void setEdges(Set<MyEdge> edges) {
		this.edges = edges;
	}
	public Set<MethodFromSql> getMethods() {
		return methods;
	}
	public void setMethods(Set<MethodFromSql> methods) {
		this.methods = methods;
	}
	public Map<Integer, MethodFromSql> getIndexOfMethods() {
		return indexOfMethods;
	}
	public void setIndexOfMethods(Map<Integer, MethodFromSql> indexOfMethods) {
		this.indexOfMethods = indexOfMethods;
	}

	
}
