package flowdroid.test.strTest;

public class StringBuilderTest {

	public static void main(String[] args) {
		StringBuilder sb1 = new StringBuilder();
		if(sb1.toString().equals("")){
			// sb1.equals("") ��Ϊ false.���������һ��toString��
			System.out.println("��ʼֵȷ��");
		}else{
			System.out.println("��ʼֵΪ��"+sb1.toString());
		}
	}


	public static void testConstruct(){
		// �ж���һ��StringBuilder������һ����Ȼ���Ƿ�ָ��ͬһ���ڴ档��no��
		// �½����ڴ�����
				StringBuilder sb1 = new StringBuilder("hello");
				StringBuilder sb2 = new StringBuilder(sb1.toString());
				sb2.append("2222");
				System.out.println("the sb1 is : " +sb1 );
				System.out.println("the sb2 is : " +sb2 );
	}
}
