/**
 * Conscious Calculator - Emulation of a conscious calculator.
 * Copyright © 2019 Malcolm Lett (malcolm.lett at gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lett.malcolm.consciouscalculator.emulator.processors;

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
 * This is a quick'n'nasty solution for the rest of the tasks needed to solve partially parsed expressions.
 * Prerequisites: requires that WM is populated by some remembered concepts that can help us infer the meaning of
 * the unknown expression component. 
 * 
 * Infers unknown expression and equation tokens, based on concepts present within WM.
 * eg: "3 + ? = 8"
 * 
 * At the time this first kicks in, WM will be in a state as follows (in an example WM strength order):
 * <ul>
 * <li> StuckThoughtEvent (ref=TextRequestEvent)
 * <li> TextRequestEvent "3 + ? = 8" (REQUEST,HANDLED)
 * <li> MemoryEvent with eventData containing a list of Percepts: NumberFact, EquationFact, OperatorFact, ExpressionTokenFact, EquationOperatorFact  (ref=MemorySearchRequestEvent)
 * <li> MemorySearchRequestEvent with contents of PerceptEvent (HANDLED,ref=PerceptEvent)
 * <li> PerceptEvent holding partially-parsed Equation: Number(3) Operator(+) ExpressionToken(?) EquationOperator(=) Number(8)  (ref=TextRequestEvent)
 * </ul>
 * 
 * Known Event types acted on by the this processor:
 * - {@link PerceptEvent}
 * 
 * @author Malcolm Lett
 */
public class QuickPartiallyUnparsableExpressionSolverProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(QuickPartiallyUnparsableExpressionSolverProcessor.class);

	private Clock clock;
	
	public QuickPartiallyUnparsableExpressionSolverProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * Looks for the combination of:
	 * <ul>
	 * <li> A PerceptEvent with a StuckThoughtEvent associated
	 * <li> A MemoryEvent with a concept Percept that could be useful (TODO needs to be more flexible and to support PerceptEvent too)
	 * <li> ...something to detect that it's been done already...
	 *      Must be that we need that TriedAttemptsEvent.
	 * </ul>
	 * 
	 * Don't know how to stop unnecessary infinite loops when other processors have completed the thought.
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
			}
		}
		
		return null;
	}
	
	private static boolean accepts(Event memoryItem) {
		// TODO
		
		return false;
	}
	
}
