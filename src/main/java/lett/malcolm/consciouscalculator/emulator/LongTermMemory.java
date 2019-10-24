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
package lett.malcolm.consciouscalculator.emulator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.facts.EquationFact;
import lett.malcolm.consciouscalculator.emulator.facts.EquationOperatorFact;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionFact;
import lett.malcolm.consciouscalculator.emulator.facts.ExpressionTokenFact;
import lett.malcolm.consciouscalculator.emulator.facts.NameFact;
import lett.malcolm.consciouscalculator.emulator.facts.NumberFact;
import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact;
import lett.malcolm.consciouscalculator.emulator.facts.StatementTruthFact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.utils.CycleHandler;

/**
 * Long-term memory (LTM), holds two kinds of data:
 * - facts and concepts in their raw form (pre-programmend and learned)
 * - history of significant events
 * 
 * At present LTM is only used for pre-programmed facts and concepts.
 * 
 * For learned skills, there will be a special interplay between LTM and (learning) Processors.
 * The Processors represent the learned and optimised skill, without need to refer to LTM.
 * Whereas LTM holds the original concepts, which require using more manual analytic and sequential processing
 * to use.
 * 
 * <h3>Data Relationships and Searching</h3>
 * Long-term memory is an associative memory system. For example with concepts, each concept relates
 * back to one or more other "building block" concepts, or just other concepts that have occurred
 * together a lot.
 * 
 * Searching is generally done via that associativity network.
 * Searches should start with an existing perception, and spread out to find related
 * concepts and memories.
 * 
 * @author Malcolm Lett
 */
// TODO should it store only Percepts? Or maybe it needs to store BOTH? Or only store Events, with percepts within them for concepts?
public class LongTermMemory {
	private static final Logger log = LoggerFactory.getLogger(LongTermMemory.class);
	
	private static final int DEFAULT_MAX_SEARCH_RESULT_COUNT = 10;

	private final int maxSize;
	private final Map<String, Event> contents = new HashMap<>();
	
	public LongTermMemory(Clock clock, int maxSize) {
		this.maxSize = maxSize;
		addPreprogrammedConcepts(clock, contents);
	}

	/**
	 * Adds all the pre-programmed facts and concepts.
	 * @param contents
	 */
	protected static void addPreprogrammedConcepts(Clock clock, Map<String, Event> contents) {
		// roots
		putAll(contents, wrapAsFactEvents(clock, new NameFact()));

		putAll(contents, wrapAsFactEvents(clock, new NumberFact()));
		putAll(contents, wrapAsFactEvents(clock, new StatementTruthFact()));
		
		putAll(contents, wrapAsFactEvents(clock, new ExpressionFact(), new NumberFact()));
		putAll(contents, wrapAsFactEvents(clock, new OperatorFact(), new ExpressionFact()));
		putAll(contents, wrapAsFactEvents(clock, new ExpressionTokenFact(), new ExpressionFact()));
		
		putAll(contents, wrapAsFactEvents(clock, new EquationFact(), new ExpressionFact(), new NumberFact(), new StatementTruthFact()));
		putAll(contents, wrapAsFactEvents(clock, new EquationOperatorFact(), new EquationFact()));
	}
	
	/**
	 * Stores the event, in order.
	 * 
	 * Always ADDs, never REPLACEs.
	 * @param event
	 */
	public void store(Event event) {
		log.debug("LTM Add:    " + event);
		
		put(contents, event);
	}
	
	/**
	 * Searches for related entries.
	 * 
	 * Related events will be returned in order of "most closely related" within memory,
	 * but without any indication of that relatedness in the result.
	 * Processors are expected to decide for themselves which are "better" for their needs.
	 * 
	 * Only a limited number of related entries will be returned, up to a maximum
	 * (currently {@link #DEFAULT_MAX_SEARCH_RESULT_COUNT}).
	 * 
	 * eg: searching from:
	 * <pre>
	 * 	Percept#10001([
	 *     Percept#10002(3) -> "#NumberFact",
	 *     Percept#10003(+) -> "#OperatorFact",
	 *     Percept#10004(?) -> "#ExpressionTokenFact"
	 *  ]) -> "#ExpressionFact"
	 * </pre>
	 * 
	 * Rules:
	 * - Return only "#Name" concept instance for immediate most related event
	 *  
	 * @param reference 'class' or specific 'instance' of a concept, fact, or memory.
	 * @return
	 */
	public List<Event> search(Event reference) {
		List<Event> found = new ArrayList<>();
		
		List<Percept> referenceFlatPercepts = (reference.data() instanceof Percept) ?
				flattenPerceptsByData((Percept) reference.data()) : Collections.emptyList();

		// do immediate search
		if (referenceFlatPercepts.isEmpty()) {
			addIfNonNull(found, contents.get(reference.guid()));
		}
		else {
			for (Percept referencePercept: referenceFlatPercepts) {
				addIfNonNull(found, contents.get(referencePercept.guid()));
				for (String ref: referencePercept.references()) {
					addIfNonNull(found, contents.get(ref));
				}
			}
		}
		
		// TODO pull up related concepts? (maybe just to one level?)
		
		// order and filter
		// TODO filter on "#Name#" facts
		return found.stream()
			.sorted(Comparator.comparingDouble((Event e) -> scoreRelatedness(reference, e)).reversed())
			.limit(DEFAULT_MAX_SEARCH_RESULT_COUNT)
			.collect(Collectors.toList());
	}
	
	/**
	 * Reference event is of one of two types:
	 * <ul>
	 * <li> {@code Percept#10002(3) -> "#NumberFact"}
	 * <li> {@code Percept#NumberFact(null)}
	 * </ul>
	 * 
	 * And in the first form it may have nested percepts in its data.
	 * (Currently LTM concepts don't have nested percepts in their data, but maybe that'll happen too later)
	 * 
	 * Exact score values don't matter, because they are only used in relative terms.
	 * @param reference
	 * @param event
	 * @return
	 */
	private double scoreRelatedness(Event reference, Event event) {
		
		List<Percept> referenceFlatPercepts = (reference.data() instanceof Percept) ?
				flattenPerceptsByData((Percept) reference.data()) : Collections.emptyList();
		List<Percept> eventFlatPercepts = (event.data() instanceof Percept) ?
				flattenPerceptsByData((Percept) event.data()) : Collections.emptyList();
				
		// Score based on percentage of percepts in one list that match percepts in other list, weighted be levels of match.
		// Don't worry about maintaining 1:1 matches.
		// TODO Do in both directions, then average the result ?
				
		double sum = 0.0;
		int count = 0;
		for (Percept it: referenceFlatPercepts) {
			double best = 0.0;
			for (Percept other: eventFlatPercepts) {
				double score = 0.0;
				
				// concept to concept match
				if (it.guid().equals(other.guid())) {
					score = Objects.areEqual(it.data(), other.data()) ? 1.0 : 0.8;
				}
				
				// instance to concept match
				else if (it.references().contains(other.guid())) {
					score = Objects.areEqual(it.data(), other.data()) ? 0.8 : 0.6;
				}
				
				// TODO concept to instance ?
				// TODO instance to instance ?

				best = Math.max(best, score);
			}
			
			sum += best;
			count++;
		}
		
		if (count == 0) {
			return 0.0;
		}
		else {
			return sum / count;
		}
	}
	
	/**
	 * Starts with a given percept, which <em>may</em> have a nested tree or graph
	 * of other percepts within its data, and returns all percept instances as a single list,
	 * starting with the given percept itself.
	 * @param percept
	 * @return list containing {@code root} itself, and possibly others
	 */
	private List<Percept> flattenPerceptsByData(Percept root) {
		List<Percept> result = new ArrayList<>();
		
		CycleHandler cycles = new CycleHandler();
		Queue<Object> queue = new LinkedList<>();
		queue.offer(root);
		
		while (!queue.isEmpty()) {
			Object it = queue.remove();
			
			if (!cycles.observeAndIsDuplicate(it)) {
				if (it instanceof Percept) {
					result.add((Percept) it);
					
					if (((Percept) it).data() != null) {
						queue.add(((Percept) it).data());
					}
				}
				else if (it instanceof Collection) {
					for (Object item: (Collection<?>) it) {
						if (item != null) {
							queue.offer(item);
						}
					}
				}
				else if (it instanceof Map) {
					for (Object item: ((Map<String, Object>) it).values()) {
						if (item != null) {
							queue.offer(item);
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Used during pre-programmed population of LTM.
	 * 
	 * Produces, eg:
	 * <pre>
	 *   Percept#NumberFact(null) -> references:
	 *   	Percept#NumberFact.Name("number") -> "#Name"
	 *   
	 *   Percept#ExpressionFact(null) -> references:
	 *   	Percept#ExpressionFact.Name("expression") -> "#Name"
	 * </pre>
	 */
	private static List<Event> wrapAsFactEvents(Clock clock, Fact fact, Fact... relatedFacts) {
		List<Event> events = new ArrayList<>();
		
		// create percept 'name' (modeled as a 'Name' concept instance wrapped by an event)
		Percept name = new Percept(fact.guid()+".Name", Collections.singleton(NameFact.GUID), nameOf(fact));
		events.add(wrapAsPerceptEvent(clock, name));

		// create main percept (wrapped as event)
		Set<String> references = new HashSet<>();
		references.add(name.guid());
		for (Fact relatedFact: relatedFacts) {
			references.add(relatedFact.guid());
		}
		
		Percept percept = new Percept(fact.guid(), references, null);
		events.add(wrapAsPerceptEvent(clock, percept));
		
		return events;
	}
	
	private static Event wrapAsPerceptEvent(Clock clock, Percept percept) {
		Event event = new PerceptEvent(clock, percept);
		event.setGuid(percept.guid());
		return event;
	}

	/**
	 * Gets the English display-name of the fact, in lower-case.
	 * @param fact
	 * @return non-empty name
	 */
	private static String nameOf(Fact fact) {
		String name = fact.getClass().getSimpleName();
		if (name.endsWith("Fact")) {
			name = name.substring(0, name.length() - "Fact".length());
		}
		return name.toLowerCase();
	}
	
	private static void put(Map<String, Event> map, Event event) {
		map.put(event.guid(), event);
	}

	private static void putAll(Map<String, Event> map, Collection<Event> events) {
		for (Event event: events) {
			map.put(event.guid(), event);
		}
	}
	
	private static <T> void addIfNonNull(Collection<? super T> collection, T event) {
		if (event != null) {
			collection.add(event);
		}
	}
}
