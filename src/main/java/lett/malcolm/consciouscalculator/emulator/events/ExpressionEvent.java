package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Represents an expression, either in un-evaluated, or evaluated form.
 * May have been created from a request, or may just be part of an ongoing thought process.
 */
public class ExpressionEvent extends BaseEvent implements Event {
	// TODO define expression data type
	public ExpressionEvent(Clock clock, Object expr) {
		super(clock);
		this.setData(expr);
	}
}
