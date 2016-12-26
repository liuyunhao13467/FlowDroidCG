package flowdroid.test.classTest;

public class ClassTest {

	public static void main(String[] args) {

		Inter in = new B();
		if(in instanceof A){
			System.out.println("A ````");
		}
		if(in instanceof B){
			System.out.println("B`````");
		}
	}
	
	
	interface Inter{
		public void doing();
	}
	static class B implements Inter{

		@Override
		public void doing() {
			System.out.println("this is B ~");
		}
		
	}
	static class A implements Inter{

		@Override
		public void doing() {
			System.out.println("this is A ~");
		}
		
	}
}
