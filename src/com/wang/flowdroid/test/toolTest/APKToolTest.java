package flowdroid.test.toolTest;

import java.io.File;
import java.io.IOException;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;

public class APKToolTest {

	private static final String APK_IN_DIR_PATH = "test/test_apk/aa.ex.B_K_K_AD-67.apk";
	private static final String SMOLI_ONE_DIR = "test/smoli/";

	//TODO　对于修改后的结果删除smoli文件之外的文件,如 ：assets ,res文件。
	public static void main(String[] args) {
     ApkDecoder decoder =  new ApkDecoder();
     String resultName ;
     
     File apkFile = new File(APK_IN_DIR_PATH);
     resultName = apkFile.getName();
     resultName = SMOLI_ONE_DIR + resultName;
     
     try {
		decoder.setOutDir(new File(resultName));
	     decoder.setApkFile(apkFile);
	     decoder.setDecodeResources((short) 0x0100);
	     decoder.decode();
	     System.out.println("decoded ````");
	} catch (AndrolibException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}  
	}
}
