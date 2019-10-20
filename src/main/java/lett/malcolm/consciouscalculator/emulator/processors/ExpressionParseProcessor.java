package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.emulator.ltm.math.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.ltm.math.NumberFact;
import lett.malcolm.consciouscalculator.emulator.ltm.math.OperatorFact;
import lett.malcolm.consciouscalculator.emulator.ltm.math.OperatorFact.OperatorSymbol;

/**
 * Detects textual mathematical expressions within received command request events,
 * parsed the expressions, and outputs them as ExpressionFact events.
 * 
 * Known Event types acted on by the this processor:
 * - {@link TextRequestEvent}
 */
public class ExpressionParseProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionParseProcessor.class);
	
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
	 * 
	 * @param a parsed {@link PerceptEvent}, plus a status update to the actioned event
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				String text = (String) memoryItem.data();
				Percept expr;
				try {
					expr = parseExpression(text);
				} catch (RuntimeException ignored) {
					// not an event this processor is interested in
					LOG.trace("Unable to parse '"+text+"': " + ignored.getMessage());
					continue;
				}
				
				if (expr != null) {
					PerceptEvent exprEvent = new PerceptEvent(clock, expr);
					//exprEvent.tags().addAll(memoryItem.tags());
					exprEvent.setStrength(memoryItem.strength() + 0.01);
					exprEvent.references().add(memoryItem.guid());
					
					Event updatedMemoryItem = memoryItem.clone();
					updatedMemoryItem.tags().add(EventTag.HANDLED);
					
					return Arrays.asList(exprEvent, updatedMemoryItem);
				}
			}
		}
		
		return null;
	}
	
	// TODO instead avoid parsing if WM already contains a PerceptEvent
	// referencing this one
	private static boolean accepts(Event memoryItem) {
		// TODO for now, only processes TextRequestEvents, because there's an infinite loop created
		// by the fact that the ExpressionEvent stores its data as a String too.
		if (!(memoryItem instanceof TextRequestEvent)) {
			return false;
		}
		
		return  !memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED) &&
				memoryItem.data() != null &&
				memoryItem.data() instanceof String;
	}

	// TODO use Antlr
	private Percept parseExpression(String text) {
		// short-cut: check it contains a known operator
		boolean found = false;
		for (OperatorSymbol symbol: OperatorFact.OperatorSymbol.values()) {
			if (text.contains(symbol.code()) ) {
				found = true;
				break;
			}
		}
		if (!found) {
			return null;
		}
		
		// simplistic implementation for now
		List<Object> tokens = tokenize(text);
		boolean accepted = false;
		if (isTypePattern(tokens, Number.class, OperatorSymbol.class, Number.class)) {
			OperatorSymbol operator = (OperatorSymbol) tokens.get(1);
			if (operator.numArgs() == 2) {
				accepted = true;
			}
		}
		else if (isTypePattern(tokens, OperatorSymbol.class, Number.class)) {
			OperatorSymbol operator = (OperatorSymbol) tokens.get(0);
			if (operator.numArgs() == 1) {
				accepted = true;
			}
		}
		
		if (accepted) {
			List<Percept> percepts = new ArrayList<>();
			for (Object token: tokens) {
				if (token instanceof Number) {
					percepts.add(new Percept(NumberFact.GUID, token));
				}
				else if (token instanceof OperatorSymbol) {
					// not allowed to store raw enums, so marshal to code String
					percepts.add(new Percept(OperatorFact.GUID, ((OperatorSymbol) token).code()));
				}
				else {
					throw new UnsupportedOperationException("Don't know what to do with a "+token.getClass().getSimpleName());
				}
			}
			
			return new Percept(ExpressionFact.GUID, percepts);
		}
		
		return null;
	}
	
	private static boolean isTypePattern(List<Object> tokens, Class<?>... tokenTypes) {
		if (tokens.size() != tokenTypes.length) {
			return false;
		}
		for (int i=0; i < tokenTypes.length; i++) {
			if (!tokenTypes[i].isAssignableFrom(tokens.get(i).getClass())) {
				return false;
			}
		}
		return true;
	}
	
	private static List<Object> tokenize(String text) {
		List<Object> tokens = new ArrayList<>();
		
		String[] tokenStrs = text.split("[ ()]+");
		for (String tokenStr: tokenStrs) {
			Object value = null;
			if (tokenStr.matches("[0-9]+")) {
				// number
				value = Integer.parseInt(tokenStr);
			}
			else if (OperatorSymbol.valueOfCodeOrNull(tokenStr) != null) {
				// operator
				value = OperatorSymbol.valueOfCodeOrNull(tokenStr);
			}
			else {
				// unknown string
				value = tokenStr;
			}
			tokens.add(value);
		}
		
		return tokens;
	}
}
