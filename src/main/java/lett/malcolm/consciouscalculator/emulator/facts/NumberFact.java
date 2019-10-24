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
package lett.malcolm.consciouscalculator.emulator.facts;


import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * @author Malcolm Lett
 */
// TODO reference ExpressionTokenFact
public class NumberFact implements Fact {
	public static final String GUID = NumberFact.class.getSimpleName();

	@Override
	public String guid() {
		return GUID;
	}

	@Override
	public List<Class<?>> dataTypes() {
		return Collections.singletonList(Number.class);
	}
}
