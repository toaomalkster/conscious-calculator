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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.MemoryEvent;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.events.StuckThoughtEvent;
import lett.malcolm.consciouscalculator.emulator.facts.EquationFact;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.utils.Events;
import lett.malcolm.consciouscalculator.utils.MyMath;

/**
 * This is a quick'n'nasty solution for the rest of the tasks needed to solve partially parsed expressions/equations.
 * Prerequisites: requires that WM is populated by some remembered concepts that can help us infer the meaning of
 * the unknown expression or equation component.
 * 
 * The implementation here is built on top of domain knowledge, under the principle that the 'Conscious Calculator' is
 * an emulated consciousness with the intelligence of a calculator. Thus it acceptable to have pre-programmed knowledge and handling
 * of equation and expressions. Thus we don't need to implement a full general intelligence, in order to be authentic.
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
// TODO In a general purpose solution: -- this class includes some notes throughout of what a more complete, generic solution might look
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
	// TODO In a general purpose solution: each of steps here would be an independent processor, which would
	//      emit results back into WM for others to pick up.
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			Event targetEvent;
			if (acceptsTriggerEvent(memoryItem) && (targetEvent = findTargetMemoryItem(memoryItem, memory)) != null) {
				Percept targetExpr = ((PerceptEvent) targetEvent).data(); // expr or equation
				
				// find helpful concepts
				List<Percept> concepts = findConceptInWorkingMemory(targetEvent, memory);
				
				// apply knowledge of concepts to find the problem
				Problem problemItem = findProblem(targetExpr, concepts);
				
				// apply knowledge of concepts to find the solution
				Percept solutionItem = null;
				Percept solvedExpr = null;
				if (problemItem != null) {
					solutionItem = fillInMissingConcept(problemItem, concepts);
				}
				if (solutionItem != null) {
					solutionItem = interpretValue(solutionItem, concepts);
				}
				if (solutionItem != null) {
					solvedExpr = rebuildOriginalRequest(targetExpr, problemItem, solutionItem);
				}
				
				// emit 'attempt' at a solved expr
				if (solvedExpr != null) {
					Event result = new PerceptEvent(clock, solvedExpr);
					result.references().add(targetEvent.guid());
					return Arrays.asList(result);
				}
			}
		}
		
		return null;
	}
	
	// TODO block once handled
	private boolean acceptsTargetEvent(Event memoryItem) {
		return memoryItem instanceof PerceptEvent &&
				(((PerceptEvent) memoryItem).data().references().contains(EquationFact.GUID) ||
						((PerceptEvent) memoryItem).data().references().contains(ExpressionFact.GUID)) &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}
	
	private boolean acceptsTriggerEvent(Event memoryItem) {
		return memoryItem instanceof StuckThoughtEvent &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}

	/**
	 * Selects one target event from those referenced by triggerEvent, and
	 * only from within WorkingMemory.
	 * Also checks against {@link #acceptsTargetEvent(Event)}.
	 * 
	 * Copes with the following linkages:
	 * <pre>
	 *    Original                    (some sort of original event that caused the processing)
	 *       ^
	 *       |
	 *    Target <----- StuckThought  (identified stuck thought at Target, but some derived event may be working for it)
	 *       ^
	 *       |
	 *    Derived1
	 *       ^
	 *       |
	 *    Derived2                    (it may be this one that we need to provide help on)
	 * </pre>
	 * @param triggerEvent assumed to be a StuckThoughtEvent
	 * @param memory
	 * @return null if none found
	 */
	private Event findTargetMemoryItem(Event triggerEvent, WorkingMemory memory) {
		// find targeted event
		// (don't do too much filtering at this point, as the direct target may be flagged as HANDLED already)
		List<Event> directTargets = triggerEvent.references().stream()
				.map(r -> memory.get(r))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		// now find all related events
		List<Event> allTargets = new ArrayList<>();
		for (Event directTarget: directTargets) {
			allTargets.addAll(
					memory.getChainStartingWith(directTarget).stream()
					.collect(Collectors.toList()));
		}
		
		// finally, filter on just events we have a chance to help in,
		// and pick single strongest event
		// (currently can only help on events that have a Percept)
		return allTargets.stream()
				.filter(this::acceptsTargetEvent)
				.reduce(Events.strongest())
				.orElse(null);
	}
	
	/**
	 * Looks for a MemoryEvent containing one or more Percepts,
	 * and where the event links to {@code targetEvent}.
	 * @param targetEvent
	 * @param memory
	 * @return list of percepts from single strongest event, empty if none found
	 */
	private List<Percept> findConceptInWorkingMemory(Event targetEvent, WorkingMemory memory) {
		List<Percept> res = memory.all().stream()
			.filter(e -> e instanceof MemoryEvent)
			.filter(e -> memory.containsChainFromTo(targetEvent, e))
			.filter(this::hasAnyPercepts)
			.reduce(Events.strongest())
			.map(this::getPercepts)
			.orElse(Collections.emptyList());
		return res;
	}
	
	private boolean hasAnyPercepts(Event e) {
		return !getPercepts(e).isEmpty();
	}
	
	// note: LongTermMemorySearchProcessor currently emits a MemoryEvent<eventData:List<Percept and/or Event>>
	private List<Percept> getPercepts(Event e) {
		Collection<?> collection = null;
		if (e.data() instanceof Percept) {
			return Collections.singletonList((Percept) e.data());
		}
		else if (e.data() instanceof Collection) {
			collection = (Collection<?>) e.data();
		}
		else if (e instanceof MemoryEvent) {
			MemoryEvent mem = (MemoryEvent) e;
			if (mem.eventData() instanceof Percept) {
				return Collections.singletonList((Percept) mem.eventData());
			}
			else if (mem.eventData() instanceof Collection) {
				collection = (Collection<?>) mem.eventData();
			}
		}
		
		if (collection != null) {
			return collection.stream()
				.filter(o -> o instanceof Percept)
				.map(o -> (Percept) o)
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	/**
	 * Identifies a single point in the percept where the biggest identified problem lies.
	 * May not be the only problem, but applying a greedy solution for now.
	 * 
	 * Example: partially parsed Equation:
	 * <code>Equation: Number(3) Operator(+) ExpressionToken(?) EquationOperator(=) Number(8)  (ref=TextRequestEvent)</code>
	 * @param targetExpr expression or equation
	 * @param concepts
	 * @return percept within target that needs work, or null if none found
	 */
	// TODO In a general purpose solution: this would need a mathematical-type algorithmic solution.
	//      And it would need to be a mixture of processors that apply different matching strategies.
	//      It's also the sort of thing that could be learned by a NN.
	//      Additionally, the logic here makes all sorts of assumptions about the data types.
	private Problem findProblem(Percept targetExpr, List<Percept> concepts) {
		// pick most closely matching concept
		// eg: Equations = Number Operator Number EquationOperator Number
		Percept matchingConcept = null;
		for (Percept concept: concepts) {
			if (targetExpr.references().contains(concept.guid())) {
				matchingConcept = concept;
			}
		}
		if (matchingConcept == null) {
			return null;
		}
		
		// FIXME stops working at this point, because LTM entry for Expression/EquationFacts don't
		// have any data(). And it's a bit tricky to put data in there.
		
		// check assumptions
		if (!(targetExpr.data() instanceof List) || !(matchingConcept instanceof List)) {
			return null;
		}
		
		// find the discrepancy
		// FIXME if the lists are different lengths then could be an indication of 'missing' or 'extra' information
		List<Percept> targetItems = (List<Percept>) targetExpr.data();
		List<Percept> conceptItems = (List<Percept>) matchingConcept.data();
		for (int i=0; i < targetItems.size(); i++) {
			Percept targetItem = (i < targetItems.size()) ? targetItems.get(i) : null;
			Percept conceptItem = (i < conceptItems.size()) ? conceptItems.get(i) : null;
			if (targetItem != null && conceptItem != null) {
				if (!targetItem.references().contains(conceptItem.guid())) {
					return new Problem(targetItem, conceptItem);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Produces a new percept to replace the problem percept, with information partially solved.
	 * Here, it attempts to infer the underlying concrete concept (aka type) of the unknown item.
	 * 
	 * eg:
	 * {@code ExpressionToken(?)} vs {@code Percept#Nummber} => conclusion: {@code Number(?)} - '?' is a Number, of unknown value.
	 * @param problem
	 * @param concepts
	 * @return
	 */
	private Percept fillInMissingConcept(Problem problem, List<Percept> concepts) {
		if (problem.targetItem != null && problem.conceptItem != null) {
			// just dumbly assume target should have been instance of the concept
			return new Percept(
					problem.conceptItem.guid(),
					problem.targetItem.data());
		}
		
		return null;
	}
	
	/**
	 * Produces a new percept to replace the problem percept, with information partially solved.
	 * Here's it attempts to provide concrete values.
	 * 
	 * eg:
	 * {@code Number(?)} => pick a number
	 * @param problem
	 * @param concepts
	 * @return
	 */
	// TODO only knows how to fill in numbers for now
	// TODO In a general purpose solution: this would be a long list of strategies for experimentation on different percept and data types.
	//      And it would be a mixture of processors. These are strategies that could be "learned".
	private Percept interpretValue(Percept problem, List<Percept> concepts) {
		if (problem.references().contains(NumberFact.GUID) && !(problem.data() instanceof Number)) {
			// it's a number, but the value is unknown, so guess one
			int value = MyMath.randMinMax(NumberFact.ASSUMED_MIN_VALUE, NumberFact.ASSUMED_MAX_VALUE);
			return new Percept(
					problem.references().iterator().next(), // TODO need to handle more than just one reference?
					value);
		}
		
		return null;
	}

	/**
	 * Expands the original expression or equation with the (attempted) solved value.
	 * @param originalExpr original problem expression or equation
	 * @param problemItem item within originalExpr
	 * @param solvedProblemItem replacement value
	 * @return
	 */
	private Percept rebuildOriginalRequest(Percept originalExpr, Problem problemItem, Percept solvedProblemItem) {
		Percept clone = originalExpr.cloneAsNew();
		List<Percept> list = (List<Percept>) clone.data();
		
		ListIterator<Percept> listItr = list.listIterator();
		while (listItr.hasNext()) {
			Percept item = listItr.next();
			if (item.guid().equals(problemItem.targetItem.guid())) {
				listItr.set(solvedProblemItem);
			}
		}
		
		return clone;
	}
	
	private static class Problem {
		private final Percept targetItem;
		private final Percept conceptItem;
		
		public Problem(Percept targetItem, Percept conceptItem) {
			this.targetItem = targetItem;
			this.conceptItem = conceptItem;
		}
	}
}
