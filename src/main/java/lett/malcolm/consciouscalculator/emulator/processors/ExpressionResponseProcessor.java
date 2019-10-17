package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ActionEvent;
import lett.malcolm.consciouscalculator.emulator.events.ExpressionEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Triggers a response from an evaluated expression, that was created as a request.
 * 
 * Known Event types acted on by the this processor:
 * - {@link ExpressionEvent}
 */
public class ExpressionResponseProcessor implements Processor {
	private Clock clock;
	
	public ExpressionResponseProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * TODO
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				Event event = new ActionEvent(clock, (String) memoryItem.data());
				event.tags().addAll(memoryItem.tags());
				event.setStrength(memoryItem.strength() + 0.01);
				
				Event updatedMemoryItem = memoryItem.clone();
				updatedMemoryItem.tags().add(EventTag.HANDLED);
				
				return Arrays.asList(event, updatedMemoryItem);
			}
		}
		
		return null;
	}

	private static boolean accepts(Event memoryItem) {
		return memoryItem instanceof ExpressionEvent &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED) &&
				memoryItem.tags().contains(EventTag.REQUEST) &&
				
				// TODO use proper Expression logic to detect whether fully evaluated
				((String) memoryItem.data()).contains("=");
	}
}
