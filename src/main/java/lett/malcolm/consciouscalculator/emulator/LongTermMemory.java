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
package lett.malcolm.consciouscalculator.emulator;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Long-term memory (LTM), holds two kinds of data:
 * - facts and concepts in their raw form (pre-programmend and learned)
 * - history of significant events
 * 
 * At present LTM is only used for pre-programmed facts and concepts.
 * 
 * For learned skills, there will be a special interplay between LTM and (learning) Processors.
 * The Processors represent the learned and optimised skill, without need to refer to LTM.
 * Whereas LTM holds the original concepts, which require using more manual analytic and sequential processing
 * to use.
 * 
 * @author Malcolm Lett
 */
public class LongTermMemory {
	private static final Logger log = LoggerFactory.getLogger(LongTermMemory.class);

	private final int maxSize;
	private final List<Event> contents = new ArrayList<>();
	
	public LongTermMemory(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Stores the event, in order.
	 * 
	 * Always ADDs, never REPLACEs.
	 * @param event
	 */
	public void store(Event event) {
		log.debug("LTM Add:    " + event);
		
		contents.add(event);
	}
}
