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
package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Recognises that a request has been issued via the command input stream.
 * 
 * @author Malcolm Lett
 */
public class RequestCommandInterceptor implements InputInterceptor {
	private Clock clock;
	
	public RequestCommandInterceptor() {
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
			
			Event event = new TextRequestEvent(data);
			event.setStrength(0.5);
			return event;
		}
		return null;
	}

}
