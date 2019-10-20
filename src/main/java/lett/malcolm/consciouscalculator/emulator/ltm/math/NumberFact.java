package lett.malcolm.consciouscalculator.emulator.ltm.math;

import java.util.Collections;
import java.util.List;

import lett.malcolm.consciouscalculator.emulator.ltm.Fact;

public class NumberFact implements Fact {
	public static final String GUID = NumberFact.class.getSimpleName();

	@Override
	public String guid() {
		return GUID;
	}

	@Override
	public List<Class<?>> dataTypes() {
		return Collections.singletonList(Number.class);
	}
}
