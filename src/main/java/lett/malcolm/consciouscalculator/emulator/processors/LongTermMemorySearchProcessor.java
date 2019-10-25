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
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.LongTermMemory;
import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.MemorySearchRequestEvent;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.LTMAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Performs a search against Long Term Memory.
 * 
 * Known Event types acted on by the this processor:
 * - {@link MemorySearchRequestEvent}
 * 
 * @author Malcolm Lett
 */
// TODO do one for ShortTermMemory too
public class LongTermMemorySearchProcessor implements Processor, LTMAwareProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(LongTermMemorySearchProcessor.class);

	private Clock clock;
	private LongTermMemory longTermMemory;
	
	public LongTermMemorySearchProcessor(Clock clock) {
		this.clock = clock;
	}
	
	@Override
	public void setLTM(LongTermMemory longTermMemory) {
		this.longTermMemory = longTermMemory;
	}

	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				Object referenceData = ((MemorySearchRequestEvent) memoryItem).getReferenceData();
				
				List<Event> resultsReadyForLoading = search(referenceData);
				
				Event updatedMemoryItem = memoryItem.clone();
				updatedMemoryItem.tags().add(EventTag.HANDLED);
				resultsReadyForLoading.add(updatedMemoryItem);
				
				return resultsReadyForLoading;
				
			}
		}
		
		return null;
	}

	/**
	 * 
	 */
	// TODO probably needs a more advanced way of knowing whether it's done, because both
	// ShortTermMemorySearchProcessor and LongTermMemorySearchProcessor will be trying to do the same
	// thing, and may want both results to be ultimately loaded.
	private static boolean accepts(Event memoryItem) {
		return  memoryItem instanceof MemorySearchRequestEvent &&
				memoryItem.data() != null &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}
	
	private List<Event> search(Object referenceData) {
		List<Event> longTermMemories;
		if (referenceData instanceof PerceptEvent) {
			longTermMemories = longTermMemory.search(((PerceptEvent) referenceData).data());
		}
		else if (referenceData instanceof Percept) {
			longTermMemories = longTermMemory.search((Percept) referenceData);
		}
		else {
			LOG.warn("Unexpected data type presented in MemorySearchRequest: " + referenceData.getClass().getName());
			return Collections.emptyList();
		}
		
		// TODO wrap as what?
		// - individual MemoryEvent -- might already be that in some cases
		// - also accept individual PerceptEvent?
		// - one big MemoryEvent or MemorySearchResultEvent?
		// - what about just cloning the events but adding a FROM_MEMORY tag?
//		longTermMemories.stream()
//			.map(mapper)
		
		return longTermMemories;
	}

}
