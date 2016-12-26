package flowdroid.utils;

import soot.SootClass;
import soot.SootMethod;

public class ParserUtils {
	
	
	
	public static boolean isArrayLetter(String letter) {
		if ("[".equals(letter))
			return true;
		return false;
	}
	
	public static boolean isObjectLetter(String letter) {
		if ("L".equals(letter))
			return true;
		return false;
	}
	
	public static boolean isBaseLetter(String letter) {
		if (("Z".equals(letter)) || ("B".equals(letter))
				|| ("S".equals(letter)) || ("C".equals(letter))
				|| ("I".equals(letter)) || ("J".equals(letter))
				|| ("F".equals(letter)) || ("D".equals(letter)))
			return true;
		return false;
	}
	
	public static boolean isArray(String returntype) {
		if (returntype.startsWith("["))
			return true;
		return false;
	}
	
	public static boolean isObject(String returntype) {
		if (returntype.startsWith("L"))
			return true;
		return false;
	}
	
	public static boolean isBaseType(StringBuffer returntype) {
		if (returntype.length() == 1)
			return true;
		return false;
	}
	
	//TODO 
	public static boolean isInRegister(String returntype){
		if (returntype.startsWith("V"))
			return true;
		return false;
	}
	
	public static StringBuffer stanClassSig(StringBuffer sclassname) {
		String str = sclassname.substring(1);
		sclassname = new StringBuffer(str.replace("/", "."));	
		sclassname.trimToSize();

		return sclassname;
	}

	public static boolean isAndroidMethod(SootMethod method ){
		SootClass sc = method.getDeclaringClass();
		return isAndroidClass(sc);
	}
	
	public static boolean isOwnMethod(SootMethod method , String ownPackageName){
		SootClass sc = method.getDeclaringClass();
		if(sc.getName().startsWith(ownPackageName)){
			return true;
		}
		return false;
	}
	
	public static boolean isAndroidClass(SootClass sc){
		String name =  sc.getName();
		if(name.startsWith("android.") || name.startsWith("com.android.test.runner")
				|| name.startsWith("dalvik.annotation") ||  name.startsWith("dalvik.bytecode")
				|| name.startsWith("dalvik.system") ||  name.startsWith("junit.framework")
				|| name.startsWith("junit.runner") ||  name.startsWith("org.apache.http.conn")
				|| name.startsWith("org.apache.http.conn.scheme") ||  name.startsWith("org.apache.http.conn.ssl")
				|| name.startsWith("org.apache.http.params") ||  name.startsWith("org.json")
				|| name.startsWith("org.w3c.dom") ||  name.startsWith("org.w3c.dom.ls")
				|| name.startsWith("org.xml.sax") ||  name.startsWith("org.xml.sax.ext")
				|| name.startsWith("org.xml.sax.helpers") ||  name.startsWith("org.xmlpull.v1")
				|| name.startsWith("org.xmlpull.v1.sax2")){
			return true;
		}
		return false;
	}
}