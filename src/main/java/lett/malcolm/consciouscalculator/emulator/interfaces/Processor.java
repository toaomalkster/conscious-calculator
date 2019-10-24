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
 * 
 * @author Malcolm Lett
 */
public interface Processor {
	/**
	 * The first returned event is considered the main one. When the attenuator decides which
	 * processor's output to process, it only examines the first event.
	 * 
	 * @param events events that have been extracted from incoming inputs, if any
	 * @param memory current working memory
	 * @return a generated event that is offered up for potential attention,
	 *   and potentially other events that need to be updated (eg: with status flag changes)
	 */
	public List<Event> process(List<Event> events, WorkingMemory memory);
}
