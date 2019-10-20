package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Identifies a moment when thought seems to have either completely stalled,
 * or is going around in loops.
 */
public class StuckThoughtEvent extends BaseEvent implements Event {
	public StuckThoughtEvent(Clock clock, String latestEventGuid) {
		super(clock);
		this.references().add(latestEventGuid);
		
		// must not to store a null value
		this.setData(true);
	}
}
