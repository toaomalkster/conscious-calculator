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
package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Internal request to STM and LTM for hits on memory, by associativity.
 * 
 * @author Malcolm Lett
 */
public class MemorySearchRequestEvent extends BaseEvent implements Event {
	public MemorySearchRequestEvent(Clock clock, Object referenceData) {
		super(clock);
		this.setData(referenceData);
		
		// not setting REQUEST tag, because didn't come from end user.
	}
	
	/**
	 * Gets the data to be used to search against.
	 * @return
	 */
	public Object getReferenceData() {
		return data();
	}
}
