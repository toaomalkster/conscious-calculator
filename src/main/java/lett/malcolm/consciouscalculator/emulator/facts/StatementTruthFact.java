package lett.malcolm.consciouscalculator.emulator.facts;

import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.interfaces.Fact;

/**
 * Represents whether a statement is true or not.
 * The statement may be a mathematical equation that has been tested, but doesn't have to be.
 */
public class StatementTruthFact implements Fact {
	public static final String GUID = StatementTruthFact.class.getSimpleName();
	
	@Override
	public String guid() {
		return GUID;
	}

	@Override
	public List<Class<?>> dataTypes() {
		return Collections.singletonList(Boolean.class);
	}
}
