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
