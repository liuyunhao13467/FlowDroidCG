
public class SwitchTest {
	static enum DataType{Max,Min,Middle};

	public static void main(String[] args) {
//		C(1);
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
	
	public static void C(int type){
		switch(type){
		case 0:
			A1();
			break;
		case 1:
			A2();
			break;
		case 2:
			A3();
			break;
		default:
			System.out.println("wrong !!");
			A3();
			break;
//		System.out.println("path here !!!"); //soot编译无法通过。
		}
		System.out.println("end ...");
	}

}
