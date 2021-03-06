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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Short-term memory, as the name suggests, only holds events for a short retention period.
 * The specifics are yet to be figured out, but the idea is to emulate the behaviour of a human short-term memory.
 * 
 * @author Malcolm Lett
 */
public class ShortTermMemory {
	private static final Logger log = LoggerFactory.getLogger(ShortTermMemory.class);

	private final int maxSize;
	private final List<Event> contents = new ArrayList<>();
	
	public ShortTermMemory(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Stores the event, in order.
	 * May cause compaction or even loss of lower-strength events.
	 * 
	 * Always ADDs, never REPLACEs.
	 * @param event
	 */
	public void store(Event event) {
		// TODO apply compaction, and obsolescence rules
		log.debug("STM Add:    " + event);
		
		contents.add(event);
	}
}
