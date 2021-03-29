package it.unisa.ocelot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides static methods to do general things.
 * 
 * @author simone
 */

public class Utils {
	private static final String ENCODING = "UTF-8";
	public static final Debugger debugger = new Debugger();

	/**
	 * Returns the content of the file with the specified path.
	 * 
	 * @param pFilename
	 *            The path of the file
	 * @return The content of the file
	 */
	public static String readFile(String pFilename) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(pFilename));
		return Charset.forName(ENCODING).decode(ByteBuffer.wrap(encoded))
				.toString();
	}

	public static boolean arrayContains(Object[] pArray, Object pContent) {
		for (int i = 0; i < pArray.length; i++) {
			if (pArray[i].equals(pContent))
				return true;
		}
		return false;
	}
	
	public static void waitForEnter() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			in.readLine();
		} catch (Exception e) {
		}
	}

	/**
	 * Writes the specified content to the file with the specified path.
	 * 
	 * @param pFilename
	 *            The path of the file
	 * @param pContent
	 *            The content to write on the file
	 */
	public static void writeFile(String pFilename, String pContent)
			throws IOException {
		PrintWriter writer = new PrintWriter(pFilename, ENCODING);
		writer.print(pContent);
		writer.close();
	}

	public static List<String[]> makeSortedCombinations(String[] pList) {
		List<String[]> combinations = new ArrayList<String[]>();

		for (int i = 0; i < pList.length; i++)
			for (int j = i + 1; j < pList.length; j++) {
				combinations.add(new String[] { pList[j], pList[i] });
			}

		return combinations;
	}
	
	public static String printParameters(Object[][][] pParameters) {
		String result = "";
		if (pParameters[0][0].length != 0)
			result += "Values\t" + Arrays.toString(pParameters[0][0]) + "\n";
		if (pParameters[1].length != 0) {
			result += "Arrays\n";
			for (int i = 0; i < pParameters[1].length; i++)
				result += "\t" + Arrays.toString(pParameters[1][i]) + "\n";
		}
		if (pParameters[2][0].length != 0)
			result += "Pointers\t" + Arrays.toString(pParameters[2][0]) + "\n";
		
		return result;
	}

	/**
	 * Prints a separator to a more readable output
	 */
	public static void printSeparator() {
		System.out.println("------------------------------------------");
	}
}
