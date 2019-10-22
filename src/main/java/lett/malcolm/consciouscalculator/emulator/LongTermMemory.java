package lett.malcolm.consciouscalculator.emulator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Long-term memory (LTM), holds two kinds of data:
 * - facts and concepts in their raw form (pre-programmend and learned)
 * - history of significant events
 * 
 * At present LTM is only used for pre-programmed facts and concepts.
 * 
 * For learned skills, there will be a special interplay between LTM and (learning) Processors.
 * The Processors represent the learned and optimised skill, without need to refer to LTM.
 * Whereas LTM holds the original concepts, which require using more manual analytic and sequential processing
 * to use.
 */
public class LongTermMemory {
	private static final Logger log = LoggerFactory.getLogger(LongTermMemory.class);

	private final int maxSize;
	private final List<Event> contents = new ArrayList<>();
	
	public LongTermMemory(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Stores the event, in order.
	 * 
	 * Always ADDs, never REPLACEs.
	 * @param event
	 */
	public void store(Event event) {
		log.debug("LTM Add:    " + event);
		
		contents.add(event);
	}
}
