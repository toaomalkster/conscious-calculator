package lett.malcolm.consciouscalculator.emulator.interfaces;

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

import java.util.List;

import lett.malcolm.consciouscalculator.emulator.WorkingMemory;

/**
 * Processors primarily act against the contents of the {@link WorkingMemory}.
 * Processors will be called repeatedly, and should only become 'excited' and attempt to
 * perform actions when they detect a state of something they're interested in.
 * 
 * Processors may become excited on:
 * - specific input events
 * - specific events within Working Memory.
 */
public interface Processor {
	/**
	 * The first returned event is considered the main one. When the attenuator decides which
	 * processor's output to process, it only examines the first event.
	 * 
	 * @param events events that have been extracted from incoming inputs, if any
	 * @param memory current working memory
	 * @return a generated event that is offered up for potential attention,
	 *   and potentially other events that need to be updated (eg: with status flag changes)
	 */
	public List<Event> process(List<Event> events, WorkingMemory memory);
}
