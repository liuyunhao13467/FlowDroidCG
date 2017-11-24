package flowdroid.test.ioTest;

import flowdroid.utils.FileUtils;

public class outputExceptionTest {

	public static void main(String[] args) {
		try{
			throw new Exception("your mistakes !!");
		}catch(Exception e){
			
			FileUtils.outputException("test/error_log/error.txt", e);
			e.printStackTrace();
		}
	}
}
