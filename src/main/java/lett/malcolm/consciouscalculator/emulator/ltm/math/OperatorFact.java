package lett.malcolm.consciouscalculator.emulator.ltm.math;

import java.util.Arrays;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.ltm.Fact;

/**
 * Represents a mathematical operator, as used within an expression.
 */
// FIXME blindly assumes integers
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
		 * @return
		 * @throws IllegalArgumentException if not known
		 */
		public static OperatorSymbol valueOfCodeOrNull(String code) {
			for (OperatorSymbol symbol: values()) {
				if (symbol.code().equals(code)) {
					return symbol;
				}
			}
			throw new IllegalArgumentException("Unrecognised operator '"+code+"'");
		}
	}
}
