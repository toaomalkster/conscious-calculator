package lett.malcolm.consciouscalculator.emulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class WorkingMemory {
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
		
		Optional<Event> existing = contents.stream().filter(e -> e.guid().equals(event.guid())).findFirst();
		if (existing.isPresent()) {
			contents.remove(existing.get());
		}
		contents.add(event);
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
	
}
