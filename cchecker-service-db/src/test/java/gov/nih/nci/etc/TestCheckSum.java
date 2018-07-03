package gov.nih.nci.etc;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class TestCheckSum {

	public static void main(String args[]) throws Exception {
		String digest = "SHA1";
		//"apache-tomcat-9.0.8.zip" apache-tomcat-8.5.31.zip apache-tomcat-8.0.52.zip
		String filePath = "/Users/asafievan/Downloads/apache-tomcat-8.0.52.zip";//full path in here
		MessageDigest messageDigest = MessageDigest.getInstance(digest);
		FileInputStream fis = new FileInputStream(filePath);
		byte[] dataBytes = new byte[4096];

		int nread = 0;

		while ((nread = fis.read(dataBytes)) != -1) {
			messageDigest.update(dataBytes, 0, nread);
		};
		
		fis.close();
		
		byte[] mdbytes = messageDigest.digest();

		//to hex
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		System.out.println("Algorithm " + digest +". Digest in hex format : " + sb.toString());
		//eea1cbcbff54bc8d5950660b420a06ecc52d03e6
		//eabd8844ba110b6e9ea45f309239ebf1006697bc
		//d6f08c2bc25236f6196c7ac51922427022f7658a
	}

}
