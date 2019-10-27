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


import static lett.malcolm.consciouscalculator.utils.MapBuilder.*;

import java.time.Clock;
import java.util.Map;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Represents a remembering of a past or present thought.
 * This is the representation output from the Conscious Feedback loop, and fed into
 * Short Term Memory.
 * 
 * @author Malcolm Lett
 */
// TODO when recalling 'concepts' from LTM, the existing structure here may not work
// TODO change to be able to store raw Percept data, or even collections of Percepts
public class MemoryEvent extends BaseEvent implements Event {
	public MemoryEvent(Clock clock, String eventType, Object eventData) {
		super(clock);
		
		this.setData(aDataMap()
				.with("eventType", eventType)
				.with("eventData", eventData)
				.build());
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> data() {
		return (Map<String, Object>) super.data();
	}
	
	public String eventType() {
		return (String) data().get("eventType");
	}
	
	public Object eventData() {
		return data().get("eventData");
	}
}
