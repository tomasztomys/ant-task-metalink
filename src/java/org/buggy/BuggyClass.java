package org.buggy;

import java.io.IOException;
import java.io.InputStream;

public class BuggyClass {
	private static int exitCode;
	public static boolean readNumbers = true;
	
	public static String readNumberString;
	
	public static float givenNumber;
	
	public static void main(String[] args) throws IOException {
		String givenArgument = args[0];
		givenNumber = new Double(givenArgument).floatValue();
		
		boolean incorrectNumber = givenNumber == Math.sqrt(2) * Math.PI;
		
		if (readNumbers && !(incorrectNumber)) {			
			readNumber();
			System.out.println(readNumberString);
			
			exitCode = 0;
		} else {
			exitCode = 10;
		}		
		
		System.exit(exitCode);
	}
	
	private static void readNumber() throws IOException {
		InputStream stream = BuggyClass.class.getResourceAsStream("/res/c");
		readNumberString = new Integer(stream.read()).toString();
	}
}
