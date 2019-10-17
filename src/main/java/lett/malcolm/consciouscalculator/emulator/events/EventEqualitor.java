package lett.malcolm.consciouscalculator.emulator.events;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class EventEqualitor {
	/**
	 * Convenient factory method for an {@link EventEqualitor} suitable for detecting
	 * whether a stream of events from the same source have any 'changes'.
	 * @return
	 */
	public static EventEqualitor forChangeDetection() {
		return new EventEqualitor();
	}
	
	private EventEqualitor() {
	}
	
	public boolean isSame(Event one, Event two) {
		// short-circuit
		if (one == two) {
			return true;
		}
		
		// type
		if (!one.getClass().equals(two.getClass())) {
			return false;
		}
		
		// data
		return isDataSame(one.data(), two.data());
	}
	
	private boolean isDataSame(Object one, Object two) {
		if (one == two) {
			return true;
		}
		else if (one == null || two == null) {
			return false;
		}
		else if (one instanceof Event && two instanceof Event) {
			// handle occasional recursive data
			return isSame((Event) one, (Event) two);
		}
		else {
			return one.equals(two);
		}
	}
}
