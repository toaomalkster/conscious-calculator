package lett.malcolm.consciouscalculator.xunused;


import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * @author Malcolm Lett
 * @deprecated currently unused
 */
@Deprecated
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
		return DataRules.isSame(one.data(), two.data());
	}
}
