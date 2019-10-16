package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ExpressionEvent;
import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Detects textual mathematical expressions within received command request events,
 * parsed the expressions, and outputs them as Expression events.
 * 
 * Known Event types acted on by the this processor:
 * - {@link TextRequestEvent}
 */
public class ExpressionParseProcessor implements Processor {
	private Clock clock;
	
	public ExpressionParseProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * Only looks at working memory, and issues the first parsed expression it finds.
	 * 
	 * Attempts to parse any events that:
	 * - aren't COMPLETED
	 * - have textual data
	 * - (don't have to be a request)
	 * 
	 * Processes in working memory order, and emits the first success.
	 * Emitted events have the same strength and tags copied from the evaluated
	 * memory item.
	 */
	@Override
	public Event process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				String text = (String) memoryItem.data();
				try {
					Object expr = parseExpression(text);
					if (expr != null) {
						Event event = new ExpressionEvent(clock, expr);
						event.tags().addAll(memoryItem.tags());
						event.setStrength(memoryItem.strength());
						return event;
					}
				} catch (RuntimeException unused) {
					// not an event this processor is interested in
				}
			}
		}
		
		return null;
	}
	
	private static boolean accepts(Event memoryItem) {
		return !memoryItem.tags().contains(EventTag.COMPLETED) &&
				memoryItem.data() != null &&
				memoryItem.data() instanceof String;
	}

	// TODO parse to an Expression object
	private static Object parseExpression(String text) {
		// TODO
		
		// dummy implementation for now
		Object expr = text;
		
		return expr;
	}
}
