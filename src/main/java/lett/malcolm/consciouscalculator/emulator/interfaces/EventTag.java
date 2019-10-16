package lett.malcolm.consciouscalculator.emulator.interfaces;

public enum EventTag {
	/**
	 * The event was triggered by a request, probably from the COMMAND input stream.
	 * Upon completion, the user hopes to obtain a response via an Action to the output stream.
	 */
	REQUEST,
	
	/**
	 * The event required some sort of processing, which has been completed.
	 */
	COMPLETED
}
