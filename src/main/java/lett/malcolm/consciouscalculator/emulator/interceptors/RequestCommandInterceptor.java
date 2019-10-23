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

import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Recognises that a request has been issued via the command input stream.
 */
public class RequestCommandInterceptor implements InputInterceptor {
	private Clock clock;
	
	public RequestCommandInterceptor(Clock clock) {
		this.clock = clock;
	}
	
	@Override
	public InputDesignator inputDesignator() {
		return InputDesignator.COMMAND;
	}

	@Override
	public Event intercept(Queue<Object> stream) {
		for (Object obj: stream) {
			String data = (String) obj;
			
			Event event = new TextRequestEvent(clock, data);
			event.setStrength(0.5);
			return event;
		}
		return null;
	}

}
