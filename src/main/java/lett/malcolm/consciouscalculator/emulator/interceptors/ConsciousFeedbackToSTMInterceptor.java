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
 * 
 * @author Malcolm Lett
 */
// TODO consider changing to store clone of raw event type in STM, and only convert to MemoryEvent upon read into WM.
/*
 * Debate:
 * 	    Should STM be stored as original event types, or as MemoryEvents.
 * 
 * Conscious Feedback should be a 'representation' of conscious state...not the original values.
 * 
 * But at the moment there's a practical benefit in keeping that 'representation' as the same data structure,
 * and even as the same data.
 * 
 * Once loaded back into WM, it needs to feel like a 'historical' memory of an event, not
 * raw thought itself...or the processors will get confused.
 * 
 * If want to re-process something from the past, should make a conscious decision to extract the
 * original data and turn it into a current thought.
 * ie: extract "3+5" expression from a MemoryEvent.
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

	private boolean isSameState(ConsciousState state1, ConsciousState state2) {
		// top event
		if (state1.getTop() == null ^ state2.getTop() == null) {
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
	
	@SuppressWarnings("unchecked")
	private Queue<ConsciousState> castStream(Queue<?> stream) {
		return (Queue<ConsciousState>) (Object) stream;
	}

}
