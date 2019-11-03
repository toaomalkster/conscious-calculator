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
package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.util.Queue;

/**
 * Input Interceptors primarily act incoming inputs.
 * They identify significant events from what would otherwise be a bland stream of meaningless data.
 * 
 * The output from interceptors are fed into the Attention Attenuator, and also as triggers and inputs
 * to {@link Processor}s.
 * 
 * Interceptors are allowed to hold state. This is especially important as intereptors need to detect
 * events (such as "changes"), from input streams. Changes cannot be detected without an idea of what came
 * before.
 * 
 * @author Malcolm Lett
 */
public interface InputInterceptor extends EventEmitter {
	/**
	 * Identifies which input this interceptor should be bound to.
	 * Interceptors only receive data from one input stream.
	 * @return
	 */
	public InputDesignator inputDesignator();
	
	/**
	 * Extract meaningful events, if it understands the incoming data.
	 * 
	 * Events produced by interceptors are expected to have strength, references, tags, and data assigned.
	 * The emitted strength should be a confidence level relative to the interceptor itself, in the range 0.0 to 1.0.
	 * Interceptors should <em>not</em> not consider relative strengths to other interceptors or processors.
	 * 
	 * Globally relative event strength, GUID, and timestamp are automatically applied once the events are
	 * accepted into the emulation.
	 * @param senseDesignator which sense this data came from
	 * @param stream stream of data
	 * @return new event, or nothing
	 */
	public Event intercept(Queue<Object> stream);
}
