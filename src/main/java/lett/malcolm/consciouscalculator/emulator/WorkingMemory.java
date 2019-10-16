package lett.malcolm.consciouscalculator.emulator;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class WorkingMemory {
	private final int maxSize;
	
	private final SortedSet<Event> contents = new TreeSet<>(
			Comparator.comparing(Event::strength).reversed());
	
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
	 * @return
	 */
	public SortedSet<Event> all() {
		return Collections.unmodifiableSortedSet(contents);
	}
	
}
