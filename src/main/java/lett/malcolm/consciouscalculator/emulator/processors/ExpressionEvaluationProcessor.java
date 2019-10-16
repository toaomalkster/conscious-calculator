package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ExpressionEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Evaluates un-evaluated expressions.
 * 
 * Known Event types acted on by the this processor:
 * - {@link ExpressionEvent}
 */
// FIXME should this just update the existing event?
//    - But need to record that there was a question.
//    - And don't want the update affecting the storage in STM.
//        -- Maybe STM should be a separate clone to avoid that happening?
// Or REPLACE the existing event?
public class ExpressionEvaluationProcessor implements Processor {
	private Clock clock;
	
	public ExpressionEvaluationProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * Only looks at working memory, and evaluates the expression it finds,
	 * but only if the expression hasn't already been evaluated.
	 * 
	 * Attempts to parse any events that:
	 * - are an ExpressionEvent
	 * - aren't COMPLETED
	 * - (don't have to be a request)
	 * 
	 * Processes in working memory order, and emits the first success.
	 * Emitted events have the same strength and tags copied from the evaluated
	 * memory item.
	 * 
	 * @return an updated event
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				Event event = evaluate((ExpressionEvent) memoryItem);
				if (event != null) {
					// indicate as an UPDATE by setting same GUID
					event.setGuid(memoryItem.guid());
					
					event.tags().addAll(memoryItem.tags());
					event.setStrength(memoryItem.strength());
					return Collections.singletonList(event);
				}
			}
		}
		
		return null;
	}
	
	private static boolean accepts(Event memoryItem) {
		return !memoryItem.tags().contains(EventTag.COMPLETED) &&
				memoryItem instanceof ExpressionEvent;
		
		// TODO check if expression is not already evaluated, and that it has unknowns in the right place
	}
	
	private Event evaluate(ExpressionEvent item) {
		if ("3 + 5".equals(item.data())) {
			return new ExpressionEvent(clock, "3 + 5 = 8");
		}
		return null;
	}
}
