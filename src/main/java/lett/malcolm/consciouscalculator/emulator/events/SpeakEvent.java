package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class SpeakEvent extends BaseEvent implements Event {
	public SpeakEvent(Clock clock, String text) {
		super(clock);
		this.setData(text);
	}
}
