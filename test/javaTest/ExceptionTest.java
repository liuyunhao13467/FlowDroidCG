
public class ExceptionTest {

	public static void main(String[] args) {
	}
	
	public static void C(int data){
		
		try {
			if (data > 0 ){
				System.out.println("right?");
			}else{
				throw new Exception();
			}
		}catch(Exception e){
			if(data == 1){
				System.out.println("data = 1,in the exception!!");

			}else{
				System.out.println("data != 1,in the exception!!");
			}
		}
	}
}
