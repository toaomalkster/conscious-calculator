package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.ConsciousFeedbacker.ConsciousState;
import lett.malcolm.consciouscalculator.emulator.events.DataRules;
import lett.malcolm.consciouscalculator.emulator.events.StuckThoughtEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Detects when there are active thoughts but that are going nowhere,
 * and inserts an event to flag this artifact, in the hope that
 * it will influence the behaviour of processors to try something different.
 * 
 * When thought is clearly going nowhere, a {@link StuckThoughtEvent} is emitted.
 * If it looks like a cycle may be happening, but it isn't certain of that,
 * a {@code DejaVuEvent} will be emitted, for a processor to pick up and examine more closely
 * along with Short Term Memory data to confirm.
 */
public class StuckThoughtInterceptor implements InputInterceptor {
	// including incoming state
	private static final int THRESHOLD_TICK_COUNT = 2;
	
	private Clock clock;
	
	// state
	private Deque<ConsciousState> lastFewTicks = new LinkedList<>();
	
	public StuckThoughtInterceptor(Clock clock) {
		this.clock = clock;
	}
	
	@Override
	public InputDesignator inputDesignator() {
		return InputDesignator.CONSCIOUS_FEEDBACK;
	}

	/**
	 * Assumes each invocation to this method represents a 'tick',
	 * and measures time by the number of such ticks.
	 * @param stream
	 * @return
	 */
	@Override
	public Event intercept(Queue<Object> stream) {
		try {
			for (ConsciousState state: castStream(stream)) {
				lastFewTicks.offer(state);
				
				if (state.getTop() != null && isLastFewUnchanged()) {
					Event event = new StuckThoughtEvent(clock, state.getTop().guid());
					event.setStrength(0.6);
					return event;
				}
			}
			return null;
		} finally {
			cleanup();
		}
	}
	
	private boolean isLastFewUnchanged() {
		int count = 0;
		
		Iterator<ConsciousState> itr = lastFewTicks.descendingIterator();
		ConsciousState prev = null;
		while (itr.hasNext()) {
			ConsciousState it = itr.next();
			if (count == 0 || isSameState(prev, it)) {
				count++;
			}
			else {
				break;
			}
			
			if (count >= THRESHOLD_TICK_COUNT) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isSameState(ConsciousState state1, ConsciousState state2) {
		// nullness
		if (state1 == state2) {
			return true;
		}
		else if (state1 == null || state2 == null) {
			return false;
		}
		
		// top event
		if (state1.getTop() == null ^ state1.getTop() == null) {
			return false;
		}
		else if (state1.getTop() != null && state2.getTop() != null) {
			if (!state1.getTop().getClass().equals(state2.getTop().getClass())) {
				return false;
			}
			if (!DataRules.isSame(state1.getTop().data(), state2.getTop().data())) {
				return false;
			}
		}

		// no changes found
		return true;
	}
	
	private void cleanup() {
		while (lastFewTicks.size() > THRESHOLD_TICK_COUNT) {
			lastFewTicks.poll();
		}
	}

	@SuppressWarnings("unchecked")
	private Queue<ConsciousState> castStream(Queue<?> stream) {
		return (Queue<ConsciousState>) (Object) stream;
	}
}
