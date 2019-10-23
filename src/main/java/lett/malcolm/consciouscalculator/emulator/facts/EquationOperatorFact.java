package lett.malcolm.consciouscalculator.emulator.facts;

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

import java.util.Arrays;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.facts.OperatorFact.OperatorSymbol;
import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents the equality/inequality operator within a mathematical equation.
 * 
 * @author Malcolm Lett
 */
// TODO reference ExpressionTokenFact?
public class EquationOperatorFact implements Fact {
	public static final String GUID = EquationOperatorFact.class.getSimpleName();

	@Override
	public String guid() {
		return GUID;
	}

	@Override
	public List<Class<?>> dataTypes() {
		return Arrays.asList(
				EquationOperatorSymbol.class,
				String.class);
	}
	
	public static enum EquationOperatorSymbol {
		EQUALS("=") {
			public boolean apply(Number lhs, Number rhs) {
				return lhs.equals(rhs);
			}
		},
		NOT_EQUALS("!=") {
			public boolean apply(Number lhs, Number rhs) {
				return !lhs.equals(rhs);
			}
		},
		;
		
		private String code;
		
		private EquationOperatorSymbol(String code) {
			this.code = code;
		}
		
		public String code() {
			return code;
		}
		
		public abstract boolean apply(Number lhs, Number rhs);
		
		/**
		 * 
		 * @param code
		 * @return
		 * @throws IllegalArgumentException if not known
		 */
		public static EquationOperatorSymbol valueOfCode(String code) {
			EquationOperatorSymbol value = valueOfCodeOrNull(code);
			if (value == null) {
				throw new IllegalArgumentException("Unrecognised operator '"+code+"'");
			}
			return value;
		}
	
		/**
		 * 
		 * @param code
		 * @return symbol or null if not known
		 */
		public static EquationOperatorSymbol valueOfCodeOrNull(String code) {
			for (EquationOperatorSymbol symbol: values()) {
				if (symbol.code().equals(code)) {
					return symbol;
				}
			}
			return null;
		}
	}

}
