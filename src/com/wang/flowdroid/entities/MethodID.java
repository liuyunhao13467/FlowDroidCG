package flowdroid.entities;

public class MethodID {
private Apk apk;
private Integer IDInApk;//ֻ����һ��apk�ڲ���Ч��ID.

public String getUniqueID(){
	//TODO �õ�ȫ�ֵ�Ψһ��ID.��ʽ�� ApkName_ApkVersion_MethodID.
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
