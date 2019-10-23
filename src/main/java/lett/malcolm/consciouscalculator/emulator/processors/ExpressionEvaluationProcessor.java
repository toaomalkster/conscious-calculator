package lett.malcolm.consciouscalculator.emulator.processors;

/*-
 * #%L
 * Conscious Calculator
 * %%
 * Copyright (C) 2019 Malcolm Lett
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact.OperatorSymbol;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Evaluates un-evaluated expressions.
 * eg: "3 + 5" => "8"
 * 
 * Known Event types acted on by the this processor:
 * - {@link PerceptEvent}
 */
// FIXME should this just update the existing event?
//    - But need to record that there was a question.
//    - And don't want the update affecting the storage in STM.
//        -- Maybe STM should be a separate clone to avoid that happening?
// Or REPLACE the existing event?
//
// TODO this class can be merged with EquationEvaluationProcessor 
public class ExpressionEvaluationProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionEvaluationProcessor.class);

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
				try {
					Percept result = evaluate(((PerceptEvent) memoryItem).data());
					if (result != null) {
						PerceptEvent resultEvent = new PerceptEvent(clock, result);
						//resultEvent.tags().addAll(memoryItem.tags());
						resultEvent.tags().add(EventTag.CONCLUSION);
						resultEvent.setStrength(memoryItem.strength() + 0.01);
						resultEvent.references().add(memoryItem.guid());
						
						Event updatedMemoryItem = memoryItem.clone();
						updatedMemoryItem.tags().add(EventTag.HANDLED);
						
						return Arrays.asList(resultEvent, updatedMemoryItem);
					}
					
				} catch (ClassCastException|IllegalArgumentException ignored) {
					// can't evaluate this expression
					LOG.trace("Unable to evaluate "+memoryItem+": " + ignored.getMessage());
				}
			}
		}
		
		return null;
	}
	
	private static boolean accepts(Event memoryItem) {
		if (!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED) &&
				memoryItem instanceof PerceptEvent) {
			if (memoryItem.data() instanceof Percept) {
				Percept percept = (Percept) memoryItem.data();
				return percept.references().contains(ExpressionFact.GUID);
			}
		}
		
		// TODO check if expression is not already evaluated, and that it has unknowns in the right place
		
		return false;
	}
	
	/**
	 * Deals with the possibility that there are parts of the expression it doesn't understand.
	 * @param item
	 * @return
	 * @throws IllegalArgumentException if cannot evaluate
	 * @throws ClassCastException if cannot evaluate
	 */
	private Percept evaluate(Percept expr) {
		List<Percept> tokens = (List<Percept>) expr.data();
		OperatorSymbol op = getOperator(tokens);
		
		Number result;
		if (op.numArgs() == 1) {
			assertTokenTypes(tokens, new OperatorFact(), new NumberFact());
			Number arg1 = (Number) tokens.get(1).data();
			result = op.apply(arg1);
		}
		else if (op.numArgs() == 2) {
			assertTokenTypes(tokens, new NumberFact(), new OperatorFact(), new NumberFact());
			Number arg1 = (Number) tokens.get(0).data();
			Number arg2 = (Number) tokens.get(2).data();
			result = op.apply(arg1, arg2);
		}
		else {
			throw new IllegalArgumentException("Wrong token pattern - don't know how to deal with operators with "+op.numArgs()+" arguments");
		}
		
		return new Percept(NumberFact.GUID, result);
	}
	
	private OperatorSymbol getOperator(List<Percept> tokens) {
		for (Percept token: tokens) {
			if (token.references().contains(OperatorFact.GUID)) {
				return OperatorSymbol.valueOfCode((String) token.data());
			}
		}
		throw new IllegalArgumentException("Wrong token pattern - no operator");
	}
	
	private void assertTokenTypes(List<Percept> tokens, Fact... facts) {
		if (tokens.size() != facts.length) {
			throw new IllegalArgumentException("Wrong token pattern - expected "+facts.length+", found "+tokens.size()+"");
		}
		for (int i=0; i < facts.length; i++) {
			if (!isAssignableToAnyOf(tokens.get(i).data(), facts[i].dataTypes())) {
				throw new IllegalArgumentException("Wrong token pattern - expected["+i+"] is any of ("+facts[i].dataTypes()+"), found "+tokens.get(i).data().getClass().getSimpleName()+"");
			}
		}
	}
	
	private boolean isAssignableToAnyOf(Object value, Collection<Class<?>> dataTypes) {
		for (Class<?> type: dataTypes) {
			if (type.isAssignableFrom(value.getClass())) {
				return true;
			}
		}
		return false;
	}
}
