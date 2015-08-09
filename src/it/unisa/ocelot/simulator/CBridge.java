package it.unisa.ocelot.simulator;

public class CBridge {
	private int coreId;
	private static int n;
	
	public CBridge() {
		this.coreId = 0;
		n++;
		//System.out.println(n);
	}
	
	/**
	 * Creates a new CBridge on the specified core process ID.
	 * @param pCoreID
	 */
	public CBridge(int pCoreID) {
		this.coreId = pCoreID;
	}
	
	static {
		initialize(3, 0, 1);
	}
	
	/**
	 * Initializes all the C native part. To be called before everything else
	 */
	public native static void initialize(int values, int arrays, int pointers);
	
	/**
	 * Executes the test function in order to populate the given the event handler
	 * @param pHandler Event handler, it will contain the result of the test (each non-trivial choice)
	 * @param arguments Parameters of the function
	 */
	public native void getEvents(EventsHandler pHandler, Object[] values, Object[][] arrays, Object[] pointers);
	
	public void onCrash() {
		System.out.println("A crash occurred...");
	}
}
