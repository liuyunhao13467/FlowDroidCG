package flowdroid.test.strTest;

public class EqualsTest {

	public static void main(String[] args) {
		String str1 = "data";
		String str2 = "data";
		if(str1 == "data"){
			System.out.println("== ����");
		}
		if(str1.equals(str2)){
			System.out.println("equals ����");
		}
		
		

	}
	
	public static void basicType(){
		int a = 1;
		int b =1;
		if(a == b){
			System.out.println("int the same");
		}
	}

}
