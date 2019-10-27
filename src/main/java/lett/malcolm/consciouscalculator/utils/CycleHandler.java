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
package lett.malcolm.consciouscalculator.utils;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Uses object reference for identity.
 * 
 * Works in two modes:
 * - object only
 * - map from 'object' to 'mirror'
 * 
 * Where docs refer to 'object', they always mean on the 'key' side of the map.
 */
public class CycleHandler {
	public static final int DEFAULT_EXPECTED_MAX_NUMBER_OF_OBJECTS = 100;
	private Map<Object, Object> observed = new IdentityHashMap<>(DEFAULT_EXPECTED_MAX_NUMBER_OF_OBJECTS);
	
	public CycleHandler() {
	}
	
	/**
	 * Replaces internal map with a new one configured with a different
	 * expected max number of objects.
	 * This can be important for performance tuning of {@link IdentityHashMap}.
	 * @param max
	 * @return this instance, for method chaining
	 */
	public CycleHandler withExpectedMaxNumberOfObjects(int max) {
		this.observed = new IdentityHashMap<>(max);
		return this;
	}
	
	/**
	 * Records that an obj has been observed, and indicates whether it was previously observed.
	 * Always ignores nulls.
	 * @param obj
	 * @return
	 */
	public boolean observeAndIsDuplicate(Object obj) {
		if (obj != null) {
			return observed.put(obj, obj) != null;
		}
		return false;
	}

	/**
	 * Always ignores nulls.
	 * @param obj
	 * @param mirror
	 */
	public void observeMirror(Object obj, Object mirror) {
		if (obj != null) {
			observed.put(obj, mirror);
		}
	}
	
	public Object getObservedMirror(Object obj) {
		return observed.get(obj);
	}
}