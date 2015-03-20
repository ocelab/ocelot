package it.unisa.ocelot.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides static methods to do general things.
 * @author simone
 */

public class Utils {
	private static final String ENCODING = "UTF-8";
	public static final Debugger debugger = new Debugger();

	
	/**
	 * Does nothing, returns pAvg!
	 * @param pAvg
	 * @param pMin
	 * @param pMax
	 * @return
	 */
	public static double normalize(double pAvg, double pMin, double pMax) {
		return pAvg;
//		if (pMax == pMin)
//			return 0;
//		else
//			return (pAvg-pMin)/(pMax-pMin);
	}
	
	public static double mirrorNormalize(double pAvg, double pMin, double pMax) {
		return pAvg;
//		if (pMax == pMin)
//			return 0;
//		else
//			return 1 - (pAvg-pMin)/(pMax-pMin);
	}
	
	/**
	 * Returns the content of the file with the specified path.
	 * @param pFilename The path of the file
	 * @return The content of the file
	 */
	public static String readFile(String pFilename) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(pFilename));
		return Charset.forName(ENCODING).decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	public static boolean arrayContains(Object[] pArray, Object pContent) {
		for (int i = 0; i < pArray.length; i++) {
			if (pArray[i].equals(pContent))
				return true;
		}
		return false;
	}
	
	/**
	 * Writes the specified content to the file with the specified path.
	 * @param pFilename The path of the file
	 * @param pContent The content to write on the file
	 */
	public static void writeFile(String pFilename, String pContent) throws IOException {
		PrintWriter writer = new PrintWriter(pFilename, ENCODING);
		writer.print(pContent);
		writer.close();
	}
	
	public static List<String[]> makeSortedCombinations(String[] pList) {
		List<String[]> combinations = new ArrayList<String[]>();
		
		for (int i = 0; i < pList.length; i++)
			for (int j = i + 1; j < pList.length; j++) {
				combinations.add(new String[] {pList[j], pList[i]});
			}
		
		return combinations;
	}
}
