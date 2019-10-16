package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.util.List;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;

/**
 * Processors primarily act against the contents of the {@link WorkingMemory}.
 * Processors will be called repeatedly, and should only become 'excited' and attempt to
 * perform actions when they detect a state of something they're interested in.
 * 
 * Processors may become excited on:
 * - specific input events
 * - specific events within Working Memory.
 */
public interface Processor {
	/**
	 * 
	 * @param events events that have been extracted from incoming inputs, if any
	 * @param memory current working memory
	 * @return a generated event that is offered up for potential attention
	 */
	public Event process(List<Event> events, WorkingMemory memory);
}
