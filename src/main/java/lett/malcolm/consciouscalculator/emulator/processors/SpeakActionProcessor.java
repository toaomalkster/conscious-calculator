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
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.AttentionAttenuator;
import lett.malcolm.consciouscalculator.emulator.WorkingMemory;
import lett.malcolm.consciouscalculator.emulator.events.ActionEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.ActionAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;

/**
 * Executes any actions that are present within working memory.
 * Gets some slightly special treatment from {@link AttentionAttenuator}.
 * 
 * Known Event types acted on by the this processor:
 * - {@link ActionEvent}
 * 
 * TODO move this into some other kind of special-purpose 'action' mechanism.
 * Remember: for humans, actions happen 'mysteriously' once we've decided we want them to happen.
 */
public class SpeakActionProcessor implements Processor, ActionAwareProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(SpeakActionProcessor.class);
	
	private Clock clock;
	private Queue<String> outputStream;
	
	public SpeakActionProcessor(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void setOutputStream(Queue<String> stream) {
		this.outputStream = stream;
	}
	
	/**
	 * TODO
	 */
	@Override
	public List<Event> process(List<Event> events, WorkingMemory memory) {
		List<Event> handledEvents = new ArrayList<>();
		
		for (Event memoryItem: memory.all()) {
			if (accepts(memoryItem)) {
				// execute action
				String text = (String) memoryItem.data();
				outputStream.offer(text);
				
				// TODO nasty hack for now
				String t;
				while ((t = outputStream.poll()) != null) {
					LOG.info("Speach: {}", t);
				}
				
				// record as handled
				Event updatedMemoryItem = memoryItem.clone();
				updatedMemoryItem.tags().add(EventTag.HANDLED);
				
				handledEvents.add(updatedMemoryItem);
			}
		}
		
		return handledEvents;
	}

	private static boolean accepts(Event memoryItem) {
		return memoryItem instanceof ActionEvent &&
				!memoryItem.tags().contains(EventTag.COMPLETED) &&
				!memoryItem.tags().contains(EventTag.HANDLED);
	}

}
