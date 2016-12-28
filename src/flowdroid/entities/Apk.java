package flowdroid.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Apk {
	//应该能够看到其拥有的包，类，方法。边？
	private String ApkName;
	private String ApkVersion;
	private Set<MyEdge> edges = new HashSet<MyEdge>();
	private Set<Method> methods = new HashSet<Method>();
	private Map<Integer,Method> indexOfMethods = new HashMap<Integer, Method>();
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
