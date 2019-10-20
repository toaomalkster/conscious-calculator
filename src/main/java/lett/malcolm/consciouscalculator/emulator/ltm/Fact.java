package lett.malcolm.consciouscalculator.emulator.ltm;

import java.util.List;

public interface Fact {

	/**
	 * Pre-programmed facts have the simple classname as the guid.
	 * Otherwise it's a unique guid.
	 * @return
	 */
	public String guid();
	
	/**
	 * Recognised data types for this fact.
	 * @return null, empty, or some
	 */
	public List<Class<?>> dataTypes();
}
