package lett.malcolm.consciouscalculator.emulator.events;

import static lett.malcolm.consciouscalculator.utils.MapBuilder.*;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Represents a remembering of a past or present thought event.
 * This is the representation output from the Conscious Feedback loop, and fed into
 * Short Term Memory.
 */
public class MemoryEvent extends BaseEvent implements Event {
	public MemoryEvent(Clock clock, String eventType, Object eventData) {
		super(clock);
		
		this.setData(aDataMap()
				.with("eventType", eventType)
				.with("eventData", eventData)
				.build());
	}
}
