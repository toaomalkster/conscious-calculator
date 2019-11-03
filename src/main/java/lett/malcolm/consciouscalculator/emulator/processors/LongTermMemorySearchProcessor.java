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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.LongTermMemory;
import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.MemoryEvent;
import lett.malcolm.consciouscalculator.emulator.events.MemorySearchRequestEvent;
import lett.malcolm.consciouscalculator.emulator.events.PerceptEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptorResult;
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

	private LongTermMemory longTermMemory;
	
	@Override
	public void setLTM(LongTermMemory longTermMemory) {
		this.longTermMemory = longTermMemory;
	}

	@Override
	public List<Event> process(List<InputInterceptorResult> events, WorkingMemory memory) {
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				Object referenceData = ((MemorySearchRequestEvent) memoryItem).getReferenceData();
				
				// do search
				List<Object> found = search(referenceData);
				
				// emit result
				if (!found.isEmpty()) {
					Event resultEvent = wrap(found);
					resultEvent.setStrength(memoryItem.strength() + 0.01);
					resultEvent.references().add(memoryItem.guid());
					
					Event updatedMemoryItem = memoryItem.clone();
					updatedMemoryItem.tags().add(EventTag.HANDLED);
					
					return Arrays.asList(resultEvent, updatedMemoryItem);
				}
			}
		}
		
		return null;
	}

	/**
	 * 
	 */
	// TODO probably needs a more advanced way of knowing whether it's done, because both
	// ShortTermMemorySearchProcessor and LongTermMemorySearchProcessor will be trying to do the same
	// thing, and the consumer may want both results to be ultimately loaded.
	private static boolean accepts(Event memoryItem) {
		return  memoryItem instanceof MemorySearchRequestEvent &&
				memoryItem.data() != null &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}
	
	/**
	 * @param referenceData
	 * @return raw events and/or percepts as persisted within memory
	 */
	private List<Object> search(Object referenceData) {
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
		
		return longTermMemory.unwrapPercepts(longTermMemories);
	}

	/**
	 * @param foundMemories raw events and/or percepts as found from within memory
	 * @return
	 */
	private Event wrap(List<Object> foundMemories) {
		// mis-using the originally intended usage of MemoryEvent, in deference to new ideas
		return new MemoryEvent("", foundMemories);
	}

}
