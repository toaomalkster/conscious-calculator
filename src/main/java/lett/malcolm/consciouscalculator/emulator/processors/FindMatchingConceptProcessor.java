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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.emulator.events.MemoryEvent;
import lett.malcolm.consciouscalculator.emulator.events.MemorySearchRequestEvent;
import lett.malcolm.consciouscalculator.emulator.events.StuckThoughtEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.utils.Events;

/**
 * Given an event in Working Memory that has become stuck, but needs to be handled, this processor attempts to help
 * solve the problem by finding the underlying concept that best gives more context to the event.
 * 
 * This is to enable subsequent detailed processors to use that underlying concept to critically solve the problem.
 * 
 * Known Event types acted on by the this processor:
 * - {@link StuckThoughtEvent}
 * - any unhandled event
 * 
 * @author Malcolm Lett
 */
public class FindMatchingConceptProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(FindMatchingConceptProcessor.class);

	private Clock clock;
	
	public FindMatchingConceptProcessor(Clock clock) {
		this.clock = clock;
	}

	/**
	 * Operates against two events:
	 * - the 'trigger' -- a StuckThoughtEvent
	 * - the 'target'  -- an event referenced by StuckThoughtEvent
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		List<Event> result = new ArrayList<>();
		
		for (Event memoryItem: memory.all()) {
			if (acceptsTriggerEvent(memoryItem)) {
				// find target
				Event target = findTargetMemoryItem((StuckThoughtEvent) memoryItem, memory);
				
				// see if concept already loaded from LTM
				Percept concept = null;
				if (target != null) {
					concept = findConceptInWorkingMemory(target, memory);
				}
				
				// OR attempt to load concept from LTM
				// (TODO if already present and not responded to yet and there is no corresponding StuckThoughtEvent, then do nothing)
				// (fluent API: emit(new Event(), ifNoUnprocessedEquivalentsPresent(), ...??)
				if (target != null && concept == null) {
					Event event = new MemorySearchRequestEvent(clock, target.data());
					event.setStrength(target.strength() + 0.01);
					event.references().add(target.guid());
					
					if (!emitTo(result, event, memory)) {
						// hold off doing anything
						LOG.trace("Blocked event emit because already present and unhandled in WM: "+event);
						return null;
					}
					return result;
				}
			}
		}
		
		return null;
	}

	private boolean acceptsTriggerEvent(Event memoryItem) {
		return memoryItem instanceof StuckThoughtEvent &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}

	/**
	 * Accepts only events that we have a chance to help in.
	 * Currently can only help on events that have a Percept.
	 * @param memoryItem
	 * @return
	 */
	private boolean acceptsTargetEvent(Event memoryItem) {
		return !(memoryItem instanceof MemorySearchRequestEvent) &&
				hasPercept(memoryItem) &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}
	
	/**
	 * Emits the supplied event to the target collection, but only if it hasn't already been
	 * previously emitted to working memory and is still under way.
	 * @param target
	 * @param event
	 * @return true if emitted
	 */
	private boolean emitTo(Collection<Event> target, Event event, WorkingMemory memory) {
		boolean alreadyPresent = memory.all().stream()
				.filter(e -> e.getClass().equals(event.getClass()))
				.filter(e -> e.references().equals(event.references()))
				.filter(e -> DataRules.isSame(e.data(), event.data()))
				.filter(e -> !e.tags().contains(EventTag.HANDLED))
				.findAny()
				.isPresent();
		
		if (!alreadyPresent) {
			target.add(event);
			return true;
		}
		return false;
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
	 * @param triggerEvent
	 * @param memory
	 * @return null if none found
	 */
	private Event findTargetMemoryItem(StuckThoughtEvent triggerEvent, WorkingMemory memory) {
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
	 * Looks for a MemoryEvent containing a Percept,
	 * and where the event links to {@code targetEvent}.
	 * @param targetEvent
	 * @param memory
	 * @return
	 */
	private Percept findConceptInWorkingMemory(Event targetEvent, WorkingMemory memory) {
		return memory.all().stream()
			.filter(e -> e instanceof MemoryEvent)
			.filter(e -> e.references().contains(targetEvent.guid()))
			.filter(this::hasPercept)
			.reduce(Events.strongest())
			.map(getPercept())
			.orElse(null);
	}
	
	private boolean hasPercept(Event e) {
		if (e.data() instanceof Percept) {
			return true;
		}
		else if (e instanceof MemoryEvent && ((MemoryEvent) e).eventData() instanceof Percept) {
			return true;
		}
		return false;
	}
	
	private Function<Event, Percept> getPercept() {
		return e -> {
			MemoryEvent mem = (MemoryEvent) e;
			if (e.data() instanceof Percept) {
				return (Percept) e.data();
			}
			else if (mem.eventData() instanceof Percept) {
				return (Percept) mem.eventData();
			}
			return null;
		};
	}
}
