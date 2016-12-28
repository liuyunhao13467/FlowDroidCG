package flowdroid.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Apk {
	//Ӧ���ܹ�������ӵ�еİ����࣬�������ߣ�
	private String ApkName;
	private String ApkVersion;
	private Set<MyEdge> edges = new HashSet<MyEdge>();
	private Set<Method> methods = new HashSet<Method>();
	private Map<Integer,Method> indexOfMethods = new HashMap<Integer, Method>();
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
	public Set<Method> getMethods() {
		return methods;
	}
	public void setMethods(Set<Method> methods) {
		this.methods = methods;
	}
	public Map<Integer, Method> getIndexOfMethods() {
		return indexOfMethods;
	}
	public void setIndexOfMethods(Map<Integer, Method> indexOfMethods) {
		this.indexOfMethods = indexOfMethods;
	}

	
}
