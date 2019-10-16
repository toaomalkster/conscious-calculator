package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.AttentionAttenuator;
import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ActionEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.ActionAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Executes any actions that are present within working memory.
 * Gets some slightly special treatment from {@link AttentionAttenuator}.
 * 
 * Known Event types acted on by the this processor:
 * - {@link ActionEvent}
 */
public class SpeakActionProcessor implements Processor, ActionAwareProcessor {
	private Clock clock;
	private Queue<String> outputStream;
	
	public SpeakActionProcessor(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void setOutputStream(Queue<String> stream) {
		this.outputStream = stream;
	}
	
	/**
	 * TODO
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		List<Event> handledEvents = new ArrayList<>();
		
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				// execute action
				String text = (String) memoryItem.data();
				outputStream.offer(text);
				
				// TODO nasty hack for now
				String t;
				while ((t = outputStream.poll()) != null) {
					System.out.println(t);
				}
				
				// record as handled
				Event updatedMemoryItem = memoryItem.clone();
				updatedMemoryItem.tags().add(EventTag.HANDLED);
				
				handledEvents.add(updatedMemoryItem);
			}
		}
		
		return handledEvents;
	}

	private static boolean accepts(Event memoryItem) {
		return memoryItem instanceof ActionEvent &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}

}
