package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Represents interpreted data with attached meaning.
 */
public class PerceptEvent extends BaseEvent implements Event {
	public PerceptEvent(Clock clock, String factGuid, Object value) {
		super(clock);
		this.references().add(factGuid);
		this.setData(value);
	}
}
