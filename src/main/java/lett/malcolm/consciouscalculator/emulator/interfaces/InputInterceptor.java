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
public interface InputInterceptor {
	/**
	 * Identifies which input this interceptor should be bound to.
	 * Interceptors only receive data from one input stream.
	 * @return
	 */
	public InputDesignator inputDesignator();
	
	/**
	 * Extract meaningful events, if it understands the incoming data.
	 * @param senseDesignator which sense this data came from
	 * @param stream stream of data
	 * @return new event, or nothing
	 */
	public Event intercept(Queue<Object> stream);
}
