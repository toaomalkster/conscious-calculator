package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Internal request to STM and LTM for hits on memory, by associativity.
 */
public class MemorySearchRequestEvent extends BaseEvent implements Event {
	public MemorySearchRequestEvent(Clock clock, Object referenceData) {
		super(clock);
		this.setData(referenceData);
		
		// not setting REQUEST tag, because didn't come from end user.
	}
}
