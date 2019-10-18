package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.ShortTermMemory;
import lett.malcolm.consciouscalculator.emulator.events.EventEqualitor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Special purpose interceptor that detects each new event coming from conscious feedback,
 * and records them into Short Term Memory.
 */
public class ConsciousFeedbackToSTMInterceptor implements InputInterceptor {
	private Clock clock;
	private ShortTermMemory shortTermMemory;
	private EventEqualitor equalitor = EventEqualitor.forChangeDetection();
	
	// state
	private boolean first = true;
	private Object prevState = null;
	
	public ConsciousFeedbackToSTMInterceptor(Clock clock, ShortTermMemory shortTermMemory) {
		this.clock = clock;
		this.shortTermMemory = shortTermMemory;
	}
	
	@Override
	public InputDesignator inputDesignator() {
		return InputDesignator.CONSCIOUS_FEEDBACK;
	}

	/**
	 * Doesn't currently return the intercepted event.
	 * TODO re-assess this
	 */
	@Override
	public Event intercept(Queue<Object> stream) {
		for (Object obj: stream) {
			if (first || !isSameState(prevState, obj)) {
				Event event = convertToEvent(obj);
				if (event != null) {
					shortTermMemory.store(event);
				}
			}
			
			first = false;
			prevState = obj;
		}
		return null;
	}
	
	private boolean isSameState(Object prevState, Object newState) {
		if (prevState == newState) {
			return true;
		}
		if (prevState != null && newState != null) {
			if (prevState instanceof Event && newState instanceof Event) {
				return equalitor.isSame((Event) prevState, (Event) newState);
			}
			else {
				return prevState.equals(newState);
			}
		}
		return true;
	}
	
	private Event convertToEvent(Object obj) {
		Event event = null;
		if (obj instanceof Event) {
			event = ((Event) obj).clone();
		}
		
		// TODO marshal into something a little more meaningful
		
		// TODO handle other data types?
		
		return event;
	}

}
