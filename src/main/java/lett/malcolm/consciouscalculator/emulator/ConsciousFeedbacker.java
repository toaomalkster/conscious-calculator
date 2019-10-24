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


import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * @author Malcolm Lett
 */
public class ConsciousFeedbacker {
	private WorkingMemory workingMemory;

	/**
	 * @param workingMemory
	 */
	public ConsciousFeedbacker(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}
	
	/**
	 * Summarises the current state of the emulated consciousness,
	 * within the scope of awareness, and writes to the {@link #resultStream}.
	 * 
	 * Scope of awareness:
	 * - top-most event within Working Memory -- (ideally all of Working Memory would be considered in priority order, but not currently)
	 * 
	 * Not in scope of awareness:
	 * - execution of Actions - these happen "mysteriously"
	 * 
	 * Writes to the result stream every 'tick', regardless of the level of activity.
	 * 
	 * @param consciousFeedbackStream stream to write to
	 */
	public void writeTo(Queue<Object> consciousFeedbackStream) {
		Event top = workingMemory.top();
		
		// process, filter, and simplify the state of working-memory
		ConsciousState summary = new ConsciousState();
		
		// summarise state of working-memory by holding a copy of the top-most event only
		// (TODO consider whether to convert this into a MemoryEvent now or later)
		if (top != null) {
			// TODO if ever start adding linkages between events, this probably wants to collapse some of those linkages down
			summary.setTop(top.clone());
		}
		
		consciousFeedbackStream.offer(summary);
	}
	
	/**
	 * Represents current summarised and simplified state of data within field of conscious awareness.
	 */
	public static class ConsciousState {
		private Event top;
		
		ConsciousState() {
		}

		/**
		 * Null if nothing going on
		 * @return
		 */
		public Event getTop() {
			return top;
		}

		void setTop(Event top) {
			this.top = top;
		}
	}
}
