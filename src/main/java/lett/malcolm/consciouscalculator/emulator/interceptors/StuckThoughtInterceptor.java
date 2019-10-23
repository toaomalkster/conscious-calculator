package lett.malcolm.consciouscalculator.emulator.interceptors;

/*-
 * #%L
 * Conscious Calculator
 * %%
 * Copyright (C) 2019 Malcolm Lett
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
 * 
 * @author Malcolm Lett
 */
public class StuckThoughtInterceptor implements InputInterceptor {
	// including incoming state
	private static final int THRESHOLD_TICK_COUNT = 3;
	
	private Clock clock;
	
	// state
	private Deque<ConsciousState> lastFewTicks = new LinkedList<>();
	private Event lastStuckEvent = null;
	
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
					// don't trigger when top event is a StuckThoughtEvent itself,
					// or when it's the same stuck event as already flagged
					if (!(state.getTop() instanceof StuckThoughtEvent)) {
						Event event = new StuckThoughtEvent(clock, state.getTop().guid());
						event.setStrength(0.6);
						
						if (canEmit(event)) {
							lastStuckEvent = event;
							return event;
						}
					}
				}
			}
			return null;
		} finally {
			cleanup();
		}
	}
	
	/**
	 * Emits the supplied event to the target collection, but only if it hasn't already been
	 * previously emitted to working memory.
	 * 
	 * We do NOT want to allow re-emits once a previous StuckThoughtEvent is handled,
	 * so we don't care whether the actual event in WM has been handled or not.
	 * (Which we probably couldn't detect anyway, due to cloning, and we shouldn't even if we could)
	 * @param target
	 * @param event
	 * @return true if emitted
	 */
	private boolean canEmit(Event event) {
		boolean alreadyEmitted = lastStuckEvent != null && 
				lastStuckEvent.references().equals(event.references());
		
		return !alreadyEmitted;
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
			prev = it;
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
