package lett.malcolm.consciouscalculator.emulator.facts;


import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents a mathematical equation, with a LHS and RHS.
 * 
 * Rules:
 * - LHS may be any expression representable via {@link ExpressionFact}.
 * - RHS may be only a single value (known or unknown)
 * 
 * Can evaluate and test the equation with supplied values for unknowns.
 * 
 * @author Malcolm Lett
 */
public class EquationFact implements Fact {
	public static final String GUID = EquationFact.class.getSimpleName();

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
