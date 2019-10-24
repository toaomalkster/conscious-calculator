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
package lett.malcolm.consciouscalculator.emulator.facts;


import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents a mathematical expression.
 * 
 * Rules:
 * - may be one or two arguments, plus an operator.
 * 
 * Can evaluate expressions, if all values are known are supplied.
 * 
 * @author Malcolm Lett
 */
public class ExpressionFact implements Fact {
	public static final String GUID = ExpressionFact.class.getSimpleName();

	@Override
	public String guid() {
		return GUID;
	}

	/**
	 * Represented as a list of PerceptEvents.
	 */
	@Override
	public List<Class<?>> dataTypes() {
		return Collections.singletonList(List.class);
	}
}
