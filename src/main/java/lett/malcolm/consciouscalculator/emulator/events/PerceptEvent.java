package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;

/**
 * Represents interpreted data with attached meaning.
 */
@JsonSerialize
public class PerceptEvent extends BaseEvent implements Event {
	public PerceptEvent(Clock clock, Percept percept) {
		super(clock);
		this.setData(percept);
	}
	
	@Override
	public Percept data() {
		return (Percept) super.data();
	}
}
