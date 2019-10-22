package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ActionEvent;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.facts.EquationFact;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Triggers a response from an evaluated expression, that was created as a request.
 * 
 * Known Event types acted on by the this processor:
 * - {@link PerceptEvent}
 */
public class ExpressionResponseProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionResponseProcessor.class);

	private Clock clock;
	
	public ExpressionResponseProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * TODO flag original REQUEST as COMPLETE
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem) && hasTiesBackToARequest(memoryItem, memory)) {
				Percept result = ((PerceptEvent) memoryItem).data();
				try {
					String exprText = evaluate(result);
					
					Event event = new ActionEvent(clock, exprText);
					event.tags().addAll(memoryItem.tags());
					event.setStrength(memoryItem.strength() + 0.01);
					
					Event updatedMemoryItem = memoryItem.clone();
					updatedMemoryItem.tags().add(EventTag.HANDLED);
					
					return Arrays.asList(event, updatedMemoryItem);
				} catch (IllegalArgumentException|ClassCastException ignored) {
					// can't evaluate this expression
					LOG.trace("Unable to respond from "+memoryItem+": " + ignored.getMessage());
				}
			}
		}
		
		return null;
	}

	/**
	 * Extracts a expression or answer string.
	 * @param item
	 * @return
	 */
	private String evaluate(Percept percept) {
		if (percept.references().contains(ExpressionFact.GUID) ||
				percept.references().contains(EquationFact.GUID)) {
			// render expression or equation
			StringBuilder buf = new StringBuilder();
			boolean first = true;
			for (Percept token: (Collection<Percept>) percept.data()) {
				if (!first) buf.append(" ");
				buf.append(evaluate(token));
				first = false;
			}
			return buf.toString();
		}
		else if (percept.data() instanceof Number || percept.data() instanceof Boolean || percept.data() instanceof String){
			return String.valueOf(percept.data());
		}
		else {
			throw new IllegalArgumentException("Wrong percept expression - don't know how to deal with percept: "+percept.toString());
		}
	}
	
	/**
	 * Looks for a CONCLUSION percept event, with ties back to a REQUEST event.
	 * @param memoryItem
	 * @return
	 */
	private static boolean accepts(Event memoryItem) {
		return  !memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED) &&
				memoryItem.tags().contains(EventTag.CONCLUSION) &&
				memoryItem instanceof PerceptEvent;
	}
	
	private static boolean hasTiesBackToARequest(Event memoryItem, WorkingMemory memory) {
		List<Event> chain = memory.getChainEndingWith(memoryItem.guid());
		for (Event event: chain) {
			if (event.tags().contains(EventTag.REQUEST)) {
				return true;
			}
		}
		return false;
	}
}
