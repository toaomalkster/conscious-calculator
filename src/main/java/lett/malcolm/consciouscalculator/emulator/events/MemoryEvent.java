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
