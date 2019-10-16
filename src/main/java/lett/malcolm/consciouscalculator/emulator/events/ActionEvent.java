package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Special kind of event that triggers an output.
 */
public class ActionEvent extends BaseEvent implements Event {
	public ActionEvent(Clock clock, String text) {
		super(clock);
		this.setData(text);
	}
}
