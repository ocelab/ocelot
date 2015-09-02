package it.unisa.ocelot.simulator;

public class CBridge {
	private int coreId;
	private static int n;
	private static boolean initialized = false;
	
	public CBridge() {
		this.coreId = 0;
	}
	
	/**
	 * Creates a new CBridge on the specified core process ID.
	 * @param pCoreID
	 */
	public CBridge(int pCoreID) {
		this();
		this.coreId = pCoreID;
	}
	
	public static void initialize(Object[][][] pParameters) {
		if (!initialized) {
			while (true) {
				try {
					initialize(pParameters[0][0].length, pParameters[1].length, pParameters[2][0].length);;
					break;
				} catch (RuntimeException e) {
					System.err.println("Temporary fail: " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Initializes all the C native part. To be called before everything else
	 */
	public synchronized native static void initialize(int values, int arrays, int pointers);
	
	/**
	 * Executes the test function in order to populate the given the event handler
	 * @param pHandler Event handler, it will contain the result of the test (each non-trivial choice)
	 * @param arguments Parameters of the function
	 */
	public void getEvents(EventsHandler pHandler, Object[] pValues, Object[][] pArrays, Object[] pPointers) {
		double[] values = new double[pValues.length];
		for (int i = 0; i < pValues.length; i++)
			values[i] = ((Number)(pValues[i])).doubleValue();
		
		double[][] arrays = new double[pArrays.length][];
		for (int i = 0; i < pArrays.length; i++) {
			arrays[i] = new double[pArrays[i].length];
			for (int j = 0; j < pArrays[i].length; j++)
				arrays[i][j] = ((Number)pArrays[i][j]).doubleValue();
		}
		
		double[] pointers = new double[pPointers.length];
		for (int i = 0; i < pPointers.length; i++)
			pointers[i] = ((Number)pPointers[i]).doubleValue();
		
//		n++;
//		if (n % 100 == 0)
//			System.out.println(n);
		
		this.getEvents(pHandler, values, arrays, pointers);
	}
	
	public native void memoryDump();
	
	public native void getEvents(EventsHandler pHandler, double[] values, double[][] arrays, double[] pointers);
	
	public void onCrash() {
		System.out.println("A crash occurred...");
	}
}
