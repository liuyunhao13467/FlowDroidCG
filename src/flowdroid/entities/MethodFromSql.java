package flowdroid.entities;

import java.util.List;


/*
 * 用来存储从数据库中查询出的方法信息。
 */
public class MethodFromSql {
public static final int ANDROID_METHOD = 0;
public static final int THIRD_METHOD = 1;
public static final int JAVA_METHOD = 2;
public static final int SELF_METHOD = 3;
	
private ApkFromSql apkIn;
private MethodID methodID;
private String packageName;
private String className;
private String methodName;
private int methodType;
private String parameters;
//private List<String> parameters;
private String returnType;


public MethodID getMethodID() {
	return methodID;
}
public void setMethodID(MethodID methodID) {
	this.methodID = methodID;
}
public String getMethodName() {
	return methodName;
}
public void setMethodName(String methodName) {
	this.methodName = methodName;
}


public String getPackageName() {
	return packageName;
}
public void setPackageName(String packageName) {
	this.packageName = packageName;
}
public String getClassName() {
	return className;
}
public void setClassName(String className) {
	this.className = className;
}
public String getParameters() {
	return parameters;
}
public void setParameters(String parameters) {
	this.parameters = parameters;
}
public ApkFromSql getApkIn() {
	return apkIn;
}
public void setApkIn(ApkFromSql apkIn) {
	this.apkIn = apkIn;
}
public String getReturnType() {
	return returnType;
}
public void setReturnType(String returnType) {
	this.returnType = returnType;
}
public int getMethodType() {
	return methodType;
}
public void setMethodType(int methodType) {
	this.methodType = methodType;
}


}
