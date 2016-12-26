package flowdroid.test.strTest;

public class StringBuilderTest {

	public static void main(String[] args) {
		StringBuilder sb1 = new StringBuilder();
		if(sb1.toString().equals("")){
			// sb1.equals("") 则为 false.【差别在于一个toString】
			System.out.println("初始值确定");
		}else{
			System.out.println("初始值为："+sb1.toString());
		}
	}


	public static void testConstruct(){
		// 判断用一个StringBuilder构造另一个，然后是否指向同一块内存。（no）
		// 新建了内存区域。
				StringBuilder sb1 = new StringBuilder("hello");
				StringBuilder sb2 = new StringBuilder(sb1.toString());
				sb2.append("2222");
				System.out.println("the sb1 is : " +sb1 );
				System.out.println("the sb2 is : " +sb2 );
	}
}
