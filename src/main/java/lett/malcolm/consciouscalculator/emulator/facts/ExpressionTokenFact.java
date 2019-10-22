package lett.malcolm.consciouscalculator.emulator.facts;

import java.util.Arrays;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents a generic token within an expression.
 * 
 * Usually expression tokens are parsed to more concrete forms, but sometimes they need to represent more
 * of an unknown.
 */
public class ExpressionTokenFact implements Fact {
	public static final String GUID = ExpressionTokenFact.class.getSimpleName();

	@Override
	public String guid() {
		return GUID;
	}

	/**
	 * Represented as a list of PerceptEvents.
	 */
	@Override
	public List<Class<?>> dataTypes() {
		return Arrays.asList(String.class, Number.class, Enum.class);
	}
}
