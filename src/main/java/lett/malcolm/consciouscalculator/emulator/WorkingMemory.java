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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * @author Malcolm Lett
 */
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
	
	private void addInternal(Event event) {
		// FIXME not ideal to sort every time, but here as a quick work around for now
		contents.add(event);
		contents.sort(Comparator.comparing(Event::strength).reversed());
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
		addInternal(event);
		
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
	 * Gets all events directly referencing the specified guid, if any.
	 * @param guid
	 * @return found events in decreasing strength order, or empty list if none
	 */
	// FIXME not ideal to sort every time, but here as a quick work around for now
	public List<Event> getReferencesTo(String guid) {
		List<Event> list = contents.stream()
				.filter(e -> e.references().contains(guid))
				.sorted(Comparator.comparing(Event::strength).reversed())
				.collect(Collectors.toList());
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Gets the chain of events, starting with the given guid (inclusive).
	 * 
	 * Given the following relationships, this method returns the sequence (E1, E2, E3, E4):
	 * <pre>
	 *    E1#guid <- E2 <- E3 <- E4
	 * </pre>
	 * 
	 * In the common scenario where events have multiple referencing events, creating a tree, the tree is flattened
	 * and returned as a list.
	 * @param event
	 * @return chain or flattened tree, if found; empty list otherwise
	 */
	public List<Event> getChainStartingWith(Event event) {
		return getChainStartingWith(event.guid());
	}
	
	/**
	 * Gets the chain of events, ending with the given guid (inclusive).
	 * 
	 * Given the following relationships, this method returns the sequence (E1, E2, E3, E4):
	 * <pre>
	 *    E1 <- E2 <- E3 <- E4#guid
	 * </pre>
	 * 
	 * In the unlikely scenario that events reference back to multiple events, creating a tree, the tree is flattened
	 * and returned as a list.
	 * @param event
	 * @return chain, if found; empty list otherwise
	 */
	public List<Event> getChainEndingWith(Event event) {
		return getChainEndingWith(event.guid());
	}
	
	/**
	 * Gets the chain of events, starting with the given guid (inclusive).
	 * 
	 * Given the following relationships, this method returns the sequence (E1, E2, E3, E4):
	 * <pre>
	 *    E1#guid <- E2 <- E3 <- E4
	 * </pre>
	 * 
	 * In the common scenario where events have multiple referencing events, creating a tree, the tree is flattened
	 * and returned as a list.
	 * @param guid
	 * @return chain or flattened tree, if found; empty list otherwise
	 */
	public List<Event> getChainStartingWith(String guid) {
		List<Event> chain = new ArrayList<>();
		
		Set<String> observed = new HashSet<>();
		Queue<String> guids = new LinkedList<>();
		guids.offer(guid);
		
		// start from 'guid' and work forwards
		while (!guids.isEmpty()) {
			String it = guids.poll();
			
			if (!observed.contains(it)) {
				observed.add(it);
				
				Event event = get(it);
				if (event != null) {
					chain.add(event);
					getReferencesTo(it).forEach(e -> guids.offer(e.guid()));
				}
			}
		}
		
		return chain;
	}
	
	/**
	 * Gets the chain of events, ending with the given guid (inclusive).
	 * 
	 * Given the following relationships, this method returns the sequence (E1, E2, E3, E4):
	 * <pre>
	 *    E1 <- E2 <- E3 <- E4#guid
	 * </pre>
	 * 
	 * In the unlikely scenario that events reference back to multiple events, creating a tree, the tree is flattened
	 * and returned as a list.
	 * @param guid
	 * @return chain, if found; empty list otherwise
	 */
	public List<Event> getChainEndingWith(String guid) {
		List<Event> chain = new ArrayList<>();
		
		Set<String> observed = new HashSet<>();
		Queue<String> guids = new LinkedList<>();
		guids.offer(guid);
		
		// start from 'guid' and work backwards, building chain in reverse order
		while (!guids.isEmpty()) {
			String it = guids.poll();
			
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
	 * Tests whether there exists a chain of events starting from {@code startEvent} and ending with {@code endEvent},
	 * based on back-references.
	 * If {@code endEvent} directly references {@code startEvent}, then this returns {@code true}, regardless of whether
	 * either event are held within working memory. For any transitive references, requires the chain of events to be present
	 * within working memory.
	 * @param startEvent
	 * @param endEvent
	 * @return {@code true} if a chain can be found
	 */
	public boolean containsChainFromTo(Event startEvent, Event endEvent) {
		String targetGuid = startEvent.guid();
		if (endEvent.references().contains(targetGuid)) {
			return true;
		}
		
		return this.getChainEndingWith(endEvent).stream()
				.anyMatch(e -> e.references().contains(targetGuid));
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
