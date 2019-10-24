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


import java.util.Arrays;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents a mathematical operator, as used within an expression.
 * 
 * @author Malcolm Lett
 */
// FIXME blindly assumes integers
//TODO reference ExpressionTokenFact
public class OperatorFact implements Fact {
	public static final String GUID = OperatorFact.class.getSimpleName();
	
	@Override
	public String guid() {
		return GUID;
	}

	@Override
	public List<Class<?>> dataTypes() {
		return Arrays.asList(
				OperatorSymbol.class,
				String.class);
	}
	
	public static enum OperatorSymbol {
		PLUS("+", 2) {
			protected Number applyInternal(Number... args) {
				return args[0].intValue() + args[1].intValue();
			}
		},
		MINUS("-", 2) {
			protected Number applyInternal(Number... args) {
				return args[0].intValue() - args[1].intValue();
			}
		},
		MULTIPLY("*", 2) {
			protected Number applyInternal(Number... args) {
				return args[0].intValue() * args[1].intValue();
			}
		},
		DIVIDE("/", 2) {
			protected Number applyInternal(Number... args) {
				return args[0].intValue() / args[1].intValue();
			}
		},
//		NEGATE("-", 1) {
//			protected Number applyInternal(Number... args) {
//				return -args[0].intValue();
//			}
//		},
		EXP("exp", 1) {
			protected Number applyInternal(Number... args) {
				return Math.exp(args[0].intValue());
			}
		},
		;
		
		private String code;
		private int numArgs;
		
		private OperatorSymbol(String code, int numArgs) {
			this.code = code;
			this.numArgs = numArgs;
		}
		
		public String code() {
			return code;
		}
		
		public int numArgs() {
			return numArgs;
		}

		public Number apply(Number... args) {
			assertNumArgs(args);
			return applyInternal(args);
		}
		
		protected abstract Number applyInternal(Number... args);
		
		private void assertNumArgs(Number... args) {
			if (args.length != numArgs) {
				throw new IllegalArgumentException("'"+code()+"' operator requires "+numArgs+" args (supplied "+args.length+")");
			}
		}

		/**
		 * 
		 * @param code
		 * @return
		 * @throws IllegalArgumentException if not known
		 */
		public static OperatorSymbol valueOfCode(String code) {
			OperatorSymbol value = valueOfCodeOrNull(code);
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
		public static OperatorSymbol valueOfCodeOrNull(String code) {
			for (OperatorSymbol symbol: values()) {
				if (symbol.code().equals(code)) {
					return symbol;
				}
			}
			return null;
		}
	}
}
