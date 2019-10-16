package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.events.ExpressionEvaluationRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Recognises requests to evaluate an expression within COMMAND.
 */
public class ExpressionInputInterceptor implements InputInterceptor {
	private Clock clock;
	
	public ExpressionInputInterceptor(Clock clock) {
		this.clock = clock;
	}
	
	@Override
	public InputDesignator senseDesignator() {
		return InputDesignator.COMMAND;
	}

	@Override
	public Event intercept(Queue<Object> stream) {
		for (Object obj: stream) {
			String data = (String) obj;

			Object expr = parseExpression(data);
			
			if (expr != null) {
				return new ExpressionEvaluationRequestEvent(clock, expr);
			}
		}
		return null;
	}

	// TODO parse to an Expression object
	private static Object parseExpression(String text) {
		// TODO
		return text;
	}
}
