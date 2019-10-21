package lett.malcolm.consciouscalculator.emulator.facts;

/**
 * Represents a mathematical equation, with a LHS and RHS.
 * 
 * Rules:
 * - LHS may be any expression representable via {@link ExpressionFact}.
 * - RHS may be only a single value (known or unknown)
 * 
 * Can evaluate and test the equation with supplied values for unknowns.
 */
public class EquationFact {
	public static final String GUID = EquationFact.class.getSimpleName();

	// TODO may need to move this to an EquationOperatorFact
	// TODO reference ExpressionTokenFact?
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
