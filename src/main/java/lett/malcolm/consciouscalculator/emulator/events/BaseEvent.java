package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

abstract class BaseEvent implements Event {
	private String guid;
	private double strength = 0;
	private Instant timestamp;
	private int size = 1;
	private Object data;
			
	public BaseEvent(Clock clock) {
		this.guid = UUID.randomUUID().toString();
		this.timestamp = clock.instant();
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

	public void setData(Object data) {
		this.data = data;
	}
}
