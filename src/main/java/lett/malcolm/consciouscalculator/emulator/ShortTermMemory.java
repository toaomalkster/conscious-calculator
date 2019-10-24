package lett.malcolm.consciouscalculator.emulator;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Short-term memory, as the name suggests, only holds events for a short retention period.
 * The specifics are yet to be figured out, but the idea is to emulate the behaviour of a human short-term memory.
 * 
 * @author Malcolm Lett
 */
public class ShortTermMemory {
	private static final Logger log = LoggerFactory.getLogger(ShortTermMemory.class);

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
		log.debug("STM Add:    " + event);
		
		contents.add(event);
	}
}
