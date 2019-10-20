package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.ConsciousFeedbacker.ConsciousState;
import lett.malcolm.consciouscalculator.emulator.ShortTermMemory;
import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.emulator.events.MemoryEvent;
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
	
	// state
	private boolean first = true;
	private ConsciousState prevState = null;
	
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
		for (ConsciousState state: castStream(stream)) {
			if (first || !isSameState(prevState, state)) {
				Event event = convertToEvent(state);
				if (event != null) {
					shortTermMemory.store(event);
				}
			}
			
			first = false;
			prevState = state;
		}
		return null;
	}
	
	/**
	 * Extracts information out of {@code state} and converts it into
	 * a {@link MemoryEvent}.
	 * @param state
	 * @return new memory event
	 */
	private Event convertToEvent(ConsciousState state) {
		Event topEvent = state.getTop();
		
		Event result = new MemoryEvent(clock,
				topEvent.getClass().getSimpleName(),
				DataRules.clone(topEvent.data()));
		result.setStrength(topEvent.strength());
		
		return result;
	}

	private boolean isSameState(ConsciousState prevState, ConsciousState newState) {
		// top event
		if (prevState.getTop() == null ^ prevState.getTop() == null) {
			return false;
		}
		else if (prevState.getTop() != null && newState.getTop() != null) {
			if (!prevState.getTop().getClass().equals(newState.getTop().getClass())) {
				return false;
			}
			if (!DataRules.isSame(prevState.getTop().data(), newState.getTop().data())) {
				return false;
			}
		}

		// no changes found
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Queue<ConsciousState> castStream(Queue<?> stream) {
		return (Queue<ConsciousState>) (Object) stream;
	}

}
