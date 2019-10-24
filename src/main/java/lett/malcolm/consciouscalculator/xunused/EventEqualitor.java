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
package lett.malcolm.consciouscalculator.xunused;


import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * @author Malcolm Lett
 * @deprecated currently unused
 */
@Deprecated
public class EventEqualitor {
	/**
	 * Convenient factory method for an {@link EventEqualitor} suitable for detecting
	 * whether a stream of events from the same source have any 'changes'.
	 * @return
	 */
	public static EventEqualitor forChangeDetection() {
		return new EventEqualitor();
	}
	
	private EventEqualitor() {
	}
	
	public boolean isSame(Event one, Event two) {
		// short-circuit
		if (one == two) {
			return true;
		}
		
		// type
		if (!one.getClass().equals(two.getClass())) {
			return false;
		}
		
		// data
		return DataRules.isSame(one.data(), two.data());
	}
}
