package flowdroid.entities;

/*
 * �������ݿ��й���method��ID��Ϣ��
 */
public class MethodID {
private ApkFromSql apk;
private Integer IDInApk;//ֻ����һ��apk�ڲ���Ч��ID.

public String getUniqueID(){
	//TODO �õ�ȫ�ֵ�Ψһ��ID.��ʽ�� ApkName_ApkVersion_MethodID.
	return null;
}

public ApkFromSql getApk() {
	return apk;
}
public void setApk(ApkFromSql apk) {
	this.apk = apk;
}
public Integer getIDInApk() {
	return IDInApk;
}
public void setIDInApk(Integer iDInApk) {
	IDInApk = iDInApk;
}
}
