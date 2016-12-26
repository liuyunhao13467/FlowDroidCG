
public class TestMain2 {

	public static void main(String[] args) {

		C(1);
	}

	public static void A1() {
		System.out.println("inside A");
	}

	public static void A2() {
		System.out.println("inside A2");
	}

	public static void B2() {
		System.out.println("inside B2");
	}
	
	public static void A3() {
		System.out.println("inside A3");
	}

	public static void B3() {
		System.out.println("inside B3");
	}
	
	public static int getCount(int i){
		return i*5;
	}

	public static void C(int i) {
		
		i = getCount(i);
		A1();
		B2();
		if ((2*i+0.5) > 1 &&  i < 5) {
			A2();
			for(int j = 0;j < i; i++){
				System.out.println("in the cycle");
			}
		} else {
			B2();
			if ((2*i+0.5) < 0) {
				A3();
				A2();
			} else {
				B3();
			}
		}
		int data = 0;
		System.out.println("hello world!!");
	}
}
