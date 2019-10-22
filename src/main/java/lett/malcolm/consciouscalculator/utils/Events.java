package lett.malcolm.consciouscalculator.utils;

import java.util.function.BinaryOperator;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public abstract class Events {
	
	private Events() { }

	/**
	 * Usage:
	 * <code>
	 * Event selected = events.stream().reduce(Events.strongest()).orElse(null);
	 * </code>
	 * @return
	 */
	public static BinaryOperator<Event> strongest() {
		return (a,b) -> a.strength() >= b.strength() ? a : b;
				
	}
}
