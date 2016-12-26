package flowdroid.entities;

public class MethodID {
private Apk apk;
private Integer IDInApk;//只是在一个apk内部有效的ID.

public String getUniqueID(){
	//TODO 得到全局的唯一的ID.格式： ApkName_ApkVersion_MethodID.
	return null;
}

public Apk getApk() {
	return apk;
}
public void setApk(Apk apk) {
	this.apk = apk;
}
public Integer getIDInApk() {
	return IDInApk;
}
public void setIDInApk(Integer iDInApk) {
	IDInApk = iDInApk;
}
}
