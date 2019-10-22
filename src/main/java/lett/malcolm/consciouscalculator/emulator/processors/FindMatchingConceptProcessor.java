package lett.malcolm.consciouscalculator.emulator.processors;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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
 */
public class FindMatchingConceptProcessor implements Processor {
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionEvaluationProcessor.class);

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

	private boolean acceptsTargetEvent(Event memoryItem) {
		return !memoryItem.tags().contains(EventTag.COMPLETED) &&
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
	 * @param triggerEvent
	 * @param memory
	 * @return null if none found
	 */
	private Event findTargetMemoryItem(StuckThoughtEvent triggerEvent, WorkingMemory memory) {
		List<Event> found = new ArrayList<>();
		for (String ref: triggerEvent.references()) {
			Event event = memory.get(ref);
			if (event != null && acceptsTargetEvent(event)) {
				found.add(event);
			}
		}
		
		// return event with highest strength
		return found.stream().reduce(Events.strongest()).orElse(null);
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
			.filter(hasPercept())
			.reduce(Events.strongest())
			.map(getPercept())
			.orElse(null);
	}
	
	private Predicate<Event> hasPercept() {
		return e -> {
			MemoryEvent mem = (MemoryEvent) e;
			if (e.data() instanceof Percept || mem.eventData() instanceof Percept) {
				return true;
			};
			return false;
		};
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
