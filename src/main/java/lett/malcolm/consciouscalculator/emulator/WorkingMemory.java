package lett.malcolm.consciouscalculator.emulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class WorkingMemory {
	private static final Logger log = LoggerFactory.getLogger(WorkingMemory.class);

	private final int maxSize;
	
	// FIXME need a better solution
	// the comparator here is used for comparing of the 'keys' in the underlying map, not just the navigation order.
	// so it teats all events with the same strength as duplicates, and keeps only the first.
//	private final SortedSet<Event> contents = new TreeSet<>(
//			Comparator.comparing(Event::strength).reversed());
	
	private final List<Event> contents = new ArrayList<>();
	
	public WorkingMemory(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Stores the event, in order.
	 * May cause compaction or even loss of lower-strength events.
	 * 
	 * If the event GUID is the same as an existing entry, then REPLACES.
	 * Otherwise ADDs.
	 * @param event
	 */
	public void store(Event event) {
		// TODO apply strength, compaction, and obsolescence rules
		
		boolean replaced = false;
		
		Optional<Event> existing = contents.stream().filter(e -> e.guid().equals(event.guid())).findFirst();
		if (existing.isPresent()) {
			contents.remove(existing.get());
			replaced = true;
		}
		contents.add(event);
		
		if (replaced) {
			log.debug("WM Replace: " + event);
		}
		else {
			log.debug("WM Add:     " + event);
		}
	}
	
	/**
	 * Callers MUST NOT modify the returned set.
	 * @return all memory items, in priority order of navigation
	 */
	public Collection<Event> all() {
		// FIXME not ideal to sort every time, but here as a quick work around for now
		contents.sort(Comparator.comparing(Event::strength).reversed());
		return Collections.unmodifiableList(contents);
	}
	
	/**
	 * Gets the single top-most item within working memory, if any.
	 * @return the found item, or null if working memory is currently empty
	 */
	public Event top() {
		if (!contents.isEmpty()) {
			return all().iterator().next();
		}
		return null;
	}
	
	/**
	 * Gets an event by guid, if present.
	 * @param guid
	 * @return the found event, or null if not found
	 */
	public Event get(String guid) {
		for (Event event: contents) {
			if (event.guid().equals(guid)) {
				return event;
			}
		}
		return null;
	}
	
	/**
	 * Gets the chain of events, ending with the given guid (inclusive).
	 * @param guid
	 * @return chain, if found
	 */
	public List<Event> getChainEndingWith(String guid) {
		List<Event> chain = new ArrayList<>();
		
		Set<String> observed = new HashSet<>();
		Queue<String> guids = new LinkedList<>();
		guids.offer(guid);
		
		while (!guids.isEmpty()) {
			String it = guids.remove();
			
			if (!observed.contains(it)) {
				observed.add(it);
				
				Event event = get(it);
				if (event != null) {
					chain.add(event);
					event.references().forEach(guids::offer);
				}
			}
		}
		
		// constructed in reverse order, so flip order and return
		Collections.reverse(chain);
		return chain;
	}
	
	/**
	 * Degrades all strengths by 0.01.
	 */
	public void degradeStrengths() {
		for (Event event: contents) {
			event.setStrength(event.strength() - 0.01);
		}
	}
	
}
