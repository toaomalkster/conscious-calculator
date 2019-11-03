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
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;
import lett.malcolm.consciouscalculator.utils.Events;
import lett.malcolm.consciouscalculator.utils.QuantityUtils;

/**
 * TODO figure out how to make most parts of Event immutable.
 * 
 * @author Malcolm Lett
 */
abstract class BaseEvent implements Event {
	private double strength = 0;
	//private int size = 1; // dynamically calculated in size()
	private Set<EventTag> tags = new HashSet<>();
	private Set<String> references = new HashSet<>();
	private Object data;
	
	// only set once event accepted
	private String guid;
	private Clock clock;
	private Instant timestamp;
			
	public BaseEvent() {
	}

	@Override
	public Event clone() {
		try {
			BaseEvent clone = (BaseEvent) super.clone();
			clone.tags = new HashSet<>(this.tags);
			clone.data = DataRules.clone(this.data);
			return clone;
		} catch (CloneNotSupportedException e) {
			// not expected
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getClass().getSimpleName()).append("{");
		
		// guid
		if (guid() != null) {
			buf.append(Events.toShortGuid(guid())).append(",");
		}
		
		// age
		if (timestamp != null) {
			if (clock == null) {
				buf.append(timestamp).append(",");
			}
			else {
				long age = Duration.between(timestamp, clock.instant()).toMillis();
				buf.append(QuantityUtils.toShortMillisString(age)).append(",");
			}
		}
		
		// strength
		buf.append(String.format("%.03f", strength)).append(",");
		
		// tags
		for (EventTag tag: tags) {
			buf.append(tag).append(",");
		}
		
		// references
		for (String reference: references) {
			buf.append("ref=").append(Events.toShortGuid(reference)).append(",");
		}
		
		// content
		buf.append(DataRules.stringOf(data));
		
		buf.append("}");
		return buf.toString();
	}
	
	@Override
	public String guid() {
		return guid;
	}

	@Override
	public Instant timestamp() {
		return timestamp;
	}

	@Override
	public double strength() {
		return strength;
	}

	@Override
	public int size() {
		return DataRules.measureSize(data);
	}
	
	public Set<EventTag> tags() {
		return tags;
	}

	public Set<String> references() {
		return references;
	}

	@Override
	public Object data() {
		return data;
	}

	@Override
	public void setStrength(double strength) {
		this.strength = strength;
	}

	@Override
	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void setTimestamp(Clock clock) {
		setTimestamp(clock, clock.instant());
	}
	
	@Override
	public void setTimestamp(Clock clock, Instant timestamp) {
		this.timestamp = timestamp;
		this.clock = clock;
	}

	@Override
	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public void setTags(Set<EventTag> tags) {
		if (tags == null) {
			tags = new HashSet<>();
		}
		this.tags = tags;
	}

	@Override
	public void setReferences(Set<String> references) {
		if (references == null) {
			references = new HashSet<>();
		}
		this.references = references;
	}
	
	@Override
	public void setData(Object data) {
		DataRules.assertValid(data);
		this.data = data;
	}
}
