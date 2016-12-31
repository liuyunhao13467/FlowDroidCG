package flowdroid.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * apk的信息来自数据库。
 */
public class ApkFromSql {
	//应该能够看到其拥有的包，类，方法。边？
	private String ApkName;
	private String ApkVersion;
	private Set<MyEdge> edges = new HashSet<MyEdge>();
	private Set<MethodFromSql> methods = new HashSet<MethodFromSql>();
	private Map<Integer,MethodFromSql> indexOfMethods = new HashMap<Integer, MethodFromSql>();
	//TODO 包与类的对应关系，类与方法的对应关系。【需要用成员变量表示！！】
	
	
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
