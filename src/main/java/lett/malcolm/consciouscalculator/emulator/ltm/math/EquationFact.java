package lett.malcolm.consciouscalculator.emulator.ltm.math;

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

}
