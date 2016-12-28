package flowdroid.test.ioTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputFile {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String data = "hello ,this is wangkang .";
		String path = "test/gexf/";
		File file = new File(path + "me");
		if(!file.exists()){
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(data);
		bw.close();
	}

}
