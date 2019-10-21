package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.facts.EquationFact;
import lett.malcolm.consciouscalculator.emulator.facts.EquationFact.EquationOperatorSymbol;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionTokenFact;
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact.OperatorSymbol;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

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
		Confidence confidence = Confidence.FAIL;
		if (tokens.stream().anyMatch(isEquationOperator())) {
			if (isTypePattern(tokens, NumberOrString.class, OperatorSymbol.class, NumberOrString.class, EquationOperatorSymbol.class, NumberOrString.class)) {
				OperatorSymbol operator = (OperatorSymbol) tokens.get(1);
				if (tokens.get(0) instanceof String || tokens.get(2) instanceof String || tokens.get(4) instanceof String) {
					confidence = Confidence.UNRECOGNISED_TOKENS;
				}
				else if (operator.numArgs() != 2) {
					confidence = Confidence.WRONG_NUM_ARGS;
				}
				else {
					confidence = Confidence.STRONG;
				}
			}
			else if (isTypePattern(tokens, OperatorSymbol.class, NumberOrString.class, EquationOperatorSymbol.class, NumberOrString.class)) {
				OperatorSymbol operator = (OperatorSymbol) tokens.get(0);
				if (tokens.get(1) instanceof String || tokens.get(3) instanceof String) {
					confidence = Confidence.UNRECOGNISED_TOKENS;
				}
				else if (operator.numArgs() != 1) {
					confidence = Confidence.WRONG_NUM_ARGS;
				}
				else {
					confidence = Confidence.STRONG;
				}
			}
		}
		else {
			if (isTypePattern(tokens, NumberOrString.class, OperatorSymbol.class, NumberOrString.class)) {
				OperatorSymbol operator = (OperatorSymbol) tokens.get(1);
				if (tokens.get(0) instanceof String || tokens.get(2) instanceof String) {
					confidence = Confidence.UNRECOGNISED_TOKENS;
				}
				else if (operator.numArgs() != 2) {
					confidence = Confidence.WRONG_NUM_ARGS;
				}
				else {
					confidence = Confidence.STRONG;
				}
			}
			else if (isTypePattern(tokens, OperatorSymbol.class, NumberOrString.class)) {
				OperatorSymbol operator = (OperatorSymbol) tokens.get(0);
				if (tokens.get(1) instanceof String) {
					confidence = Confidence.UNRECOGNISED_TOKENS;
				}
				else if (operator.numArgs() != 1) {
					confidence = Confidence.WRONG_NUM_ARGS;
				}
				else {
					confidence = Confidence.STRONG;
				}
			}
		}
		
		if (confidence != Confidence.FAIL) {
			List<Percept> percepts = new ArrayList<>();
			for (Object token: tokens) {
				if (token instanceof Number) {
					percepts.add(new Percept(NumberFact.GUID, token));
				}
				else if (token instanceof OperatorSymbol) {
					// not allowed to store raw enums, so marshal to code String
					percepts.add(new Percept(OperatorFact.GUID, ((OperatorSymbol) token).code()));
				}
				else if (token instanceof EquationOperatorSymbol) {
					// not allowed to store raw enums, so marshal to code String
					percepts.add(new Percept(EquationFact.GUID, ((EquationOperatorSymbol) token).code()));
				}
				else if (token instanceof String) {
					percepts.add(new Percept(ExpressionTokenFact.GUID, token));
				}
				else {
					throw new UnsupportedOperationException("Don't know what to do with a "+token.getClass().getSimpleName());
				}
			}
			
			return new Percept(ExpressionFact.GUID, percepts);
		}
		
		throw new IllegalArgumentException("Not a recognised pattern: "+typePatternAsStrings(tokens));
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
				// expression operator
				value = OperatorSymbol.valueOfCodeOrNull(tokenStr);
			}
			else if (EquationOperatorSymbol.valueOfCodeOrNull(tokenStr) != null) {
				// equation operator
				value = EquationOperatorSymbol.valueOfCodeOrNull(tokenStr);
			}
			else {
				// unknown string
				value = tokenStr;
			}
			tokens.add(value);
		}
		
		return tokens;
	}
	
	private static boolean isTypePattern(List<Object> tokens, Class<?>... tokenTypes) {
		if (tokens.size() != tokenTypes.length) {
			return false;
		}
		for (int i=0; i < tokenTypes.length; i++) {
			if (tokenTypes[i].equals(NumberOrString.class)) {
				if (!(tokens.get(i) instanceof Number || tokens.get(i) instanceof String)) {
					return false;
				}
			}
			else if (!tokenTypes[i].isAssignableFrom(tokens.get(i).getClass())) {
				return false;
			}
		}
		return true;
	}
	
	private static List<String> typePatternAsStrings(List<Object> tokens) {
		return tokens.stream().map(t -> className(t)).collect(Collectors.toList());
	}
	
	private static Predicate<Object> isEquationOperator() {
		return (t) -> t instanceof EquationOperatorSymbol;
	}
	
	private static String className(Object obj) {
		// cope with enum types using per-enum implementations (which create anonymous inner classes)
		if (obj.getClass().getSimpleName().equals("") && obj.getClass().getEnclosingClass() != null) {
			return obj.getClass().getEnclosingClass().getSimpleName();
		}
		
		return obj.getClass().getSimpleName();
	}
	
	private static enum Confidence {
		FAIL(0.0),
		WRONG_NUM_ARGS(0.1),
		UNRECOGNISED_TOKENS(0.2),
		STRONG(1.0);
		
		// relative strength
		private final double strength;
		
		private Confidence(double strength) {
			this.strength = strength;
		}
	}
	
	// placeholder class
	private static class NumberOrString {
		
	}
}
