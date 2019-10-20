package lett.malcolm.consciouscalculator.emulator.interfaces;

public enum EventTag {
	/**
	 * The event was triggered by a request, probably from the COMMAND input stream.
	 * Upon completion, the user hopes to obtain a response via an Action to the output stream.
	 */
	REQUEST,
	
	/**
	 * The event has been handled by a processor.
	 */
	HANDLED,
	
	/**
	 * A requested action has been completed.
	 * Mainly intended for mirroring the {@link #REQUEST} tag.
	 */
	COMPLETED,
	
	/**
	 * This event represents the accepted resolution/solution/answer
	 * to a prior event.
	 */
	CONCLUSION
}
