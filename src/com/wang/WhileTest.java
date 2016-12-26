
public class WhileTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static void A1() {
		System.out.println("inside A");
	}

	public static void A2() {
		System.out.println("inside A2");
	}

	public static void A3() {
		System.out.println("inside A3");
	}
	public static void C(int data){
		A1();
		for(int i = 0;i <data ;i++){
			A2();
		}
		A3();
	}

}
