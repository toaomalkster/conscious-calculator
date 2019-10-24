package lett.malcolm.consciouscalculator.emulator.events;


import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;

/**
 * Represents that a request has been made, via a textual representation.
 * 
 * @author Malcolm Lett
 */
public class TextRequestEvent extends BaseEvent implements Event {
	public TextRequestEvent(Clock clock, String text) {
		super(clock);
		this.setData(text);
		this.tags().add(EventTag.REQUEST);
	}
}
