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
package lett.malcolm.consciouscalculator.emulator.interfaces;


import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import lett.malcolm.consciouscalculator.emulator.events.DataRules;

/**
 * @author Malcolm Lett
 */
public interface Event extends Cloneable {
	
	/**
	 * Deep clone.
	 * @return
	 */
	public Event clone();
	
	public String guid();
	
	public Instant timestamp();
	
	public double strength();
	
	/**
	 * Comparative size of the event.
	 * Represents how much space taken up in Working Memory, Short Term Memory, or Long Term Memory. 
	 * @return 0 or positive number
	 */
	public int size();
	
	/**
	 * Meta-data, used to identify broad categories of events an different scales,
	 * and to flag state against the event.
	 * eg: to flag that the event was a request, or that it has been completed.
	 * @return non-null set - mutable
	 */
	public Set<EventTag> tags();
	
	/**
	 * GUID references to other events in the same memory region (WM, STM, LTM).
	 * @return non-null set - mutable
	 */
	public Set<String> references();
	
	/**
	 * Data always conforms to rules set by {@link DataRules}.
	 */
	public Object data();

	public void setStrength(double strength);

	public void setTimestamp(Instant timestamp);

	/**
	 * Also sets 'clock' reference, which is used as a convenience by {@link #toString()}.
	 * @param clock
	 */
	public void setTimestamp(Clock clock);
	
	/**
	 * Also sets 'clock' reference, which is used as a convenience by {@link #toString()}.
	 * @param clock
	 * @param timestamp
	 */
	public void setTimestamp(Clock clock, Instant timestamp);
	
	public void setGuid(String guid);

	public void setTags(Set<EventTag> tags);

	public void setReferences(Set<String> references);
	
	/**
	 * Must conform to rules set by {@link DataRules}.
	 */
	public void setData(Object data);
}
