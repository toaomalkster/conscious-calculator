package lett.malcolm.consciouscalculator.emulator.events;

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class ExpressionEvaluationRequestEvent extends BaseEvent implements Event {
	// TODO define expression data type
	public ExpressionEvaluationRequestEvent(Clock clock, Object expr) {
		super(clock);
		this.setData(expr);
	}
}
