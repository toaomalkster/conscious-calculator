package lett.malcolm.consciouscalculator.emulator.events;

import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;

abstract class BaseEvent implements Event {
	private String guid;
	private double strength = 0;
	private Instant timestamp;
	private int size = 1;
	private Set<EventTag> tags = new HashSet<>();
	private Object data;
			
	public BaseEvent(Clock clock) {
		this.guid = UUID.randomUUID().toString();
		this.timestamp = clock.instant();
	}

	@Override
	public Event clone() {
		// clone data
		
		// deep clone this object
		try {
			BaseEvent clone = (BaseEvent) super.clone();
			clone.tags = new HashSet<>(this.tags);
			clone.data = cloneData();
			return clone;
		} catch (CloneNotSupportedException e) {
			// not expected
			throw new RuntimeException(e);
		}
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
		return size;
	}
	
	public Set<EventTag> tags() {
		return tags;
	}

	@Override
	public Object data() {
		return data;
	}

	public void setStrength(double strength) {
		this.strength = strength;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public void setTags(Set<EventTag> tags) {
		this.tags = tags;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	private Object cloneData() {
		if (data == null) {
			return null;
		}
		
		if (data instanceof String) {
			// immutable, so doesn't need to be cloned
			return data;
		}

		// catch-all: reflection
		try {
			return data.getClass().getMethod("clone").invoke(this.data);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(data.getClass().getSimpleName() + " needs to implement clone()", e);
		}
	}
}
