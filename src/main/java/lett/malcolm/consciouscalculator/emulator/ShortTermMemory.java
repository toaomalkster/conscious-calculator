package lett.malcolm.consciouscalculator.emulator;

import java.util.ArrayList;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class ShortTermMemory {
	private final int maxSize;

	private final List<Event> contents = new ArrayList<>();
	
	public ShortTermMemory(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Stores the event, in order.
	 * May cause compaction or even loss of lower-strength events.
	 * 
	 * Always ADDs, never REPLACEs.
	 * @param event
	 */
	public void store(Event event) {
		// TODO apply compaction, and obsolescence rules
		
		contents.add(event);
	}
}
