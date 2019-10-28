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
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.utils.Events;
import lett.malcolm.consciouscalculator.utils.MyMath;

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
			Event target;
			if (acceptsTriggerEvent(memoryItem) && (target = findTargetMemoryItem(memoryItem, memory)) != null) {
				// find helpful concepts
				List<Percept> concepts = findConceptInWorkingMemory(target, memory);
				
				// apply knowledge of concepts to find the problem
				Problem problemItem = findProblem(((PerceptEvent) target).data(), concepts);
				
				// apply knowledge of concepts to find the solution
				// TODO should be pumping each partial result back into WM
				Percept solutionItem = null;
				Percept solution = null;
				if (problemItem != null) {
					solutionItem = fillInMissingConcept(problemItem, concepts);
				}
				if (solutionItem != null) {
					solutionItem = interpretValue(solutionItem, concepts);
				}
				if (solutionItem != null) {
					solution = rebuildOriginalRequest(((PerceptEvent) target).data(), problemItem, solutionItem);
				}
				
				// emit
				if (solution != null) {
					Event result = new PerceptEvent(clock, solution);
					result.references().add(target.guid());
					return Arrays.asList(result);
				}
			}
		}
		
		return null;
	}
	
	// TODO block once handled
	private boolean acceptsTargetEvent(Event memoryItem) {
		return memoryItem instanceof PerceptEvent &&
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
	 * @param target
	 * @param concepts
	 * @return percept within target that needs work, or null if none found
	 */
	// TODO this is only a quick solution, will need quite a lot more magic for a general purpose solution
	// TODO in reality it needs to be a mixture of processors that apply different matching strategies
	// TODO a mathematical-type algorithmic solution would be suitable here
	// (It's also the sort of thing that could be learned by a NN)
	private Problem findProblem(Percept target, List<Percept> concepts) {
		// pick most closely matching concept
		// eg: Equations = Number Operator Number EquationOperator Number
		Percept matchingConcept = null;
		for (Percept concept: concepts) {
			if (target.references().contains(concept.guid())) {
				matchingConcept = concept;
			}
		}
		if (matchingConcept == null) {
			return null;
		}
		
		// find the discrepancy
		// FIXME could be any combination of data types
		if (target.data() instanceof List && matchingConcept instanceof List) {
			// FIXME no reason to assume the lists contain Percept objects, or all Percept objects
			// FIXME if the lists are different lengths then could be an indication of 'missing' or 'extra' information
			List<Percept> targetItems = (List<Percept>) target.data();
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
		}
		
		return null;
	}
	
	/**
	 * Produces a new percept to replace the problem percept, with information partially solved.
	 * Here, it attempts to populate the unknown detailed concept.
	 * 
	 * eg:
	 * {@code ExpressionToken(?)} vs {@code Percept#Nummber} => conclusion: {@code Number(?)} - '?' is a Number, of unknown value.
	 * @param problem
	 * @param concepts
	 * @return
	 */
	// TODO this is a pathetically quick and dumb solution for now
	private Percept fillInMissingConcept(Problem problem, List<Percept> concepts) {
		// match-up concepts
		Percept matchedConcept = null;
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
	 * eg - step 1:
	 * {@code ExpressionToken(?)} vs {@code Percept#Nummber} => conclusion: {@code Number(?)} - '?' is a Number, of unknown value.
	 * 
	 * eg - step 2:
	 * {@code Number(?)} => pick a number
	 * {@code ExpressionToken(?)}
	 * @param problem
	 * @param concepts
	 * @return
	 */
	// TODO this is a pathetically quick and dumb solution for now
	// TODO in a more complete form, this would contain a long list of strategies for experimentation on different percept and data types
	// TODO this should totally be a mixture of processors
	// TODO these are strategies that could be "learned"
	private Percept interpretValue(Percept problem, List<Percept> concepts) {
		if (problem.references().contains(NumberFact.GUID) && !(problem.data() instanceof Number)) {
			// it's a number, but the value is unknown, so guess one
			int value = MyMath.randMinMax(NumberFact.ASSUMED_MIN_VALUE, NumberFact.ASSUMED_MAX_VALUE);
			return new Percept(
					problem.references().iterator().next(), // TODO need to handle more than just one?
					value);
		}
		
		return null;
	}
	
	// TODO again, pathetically hard-coded high-assuming solution
	private Percept rebuildOriginalRequest(Percept original, Problem problemItem, Percept solvedProblemItem) {
		Percept clone = original.cloneAsNew();
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
