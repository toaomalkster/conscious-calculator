package lett.malcolm.consciouscalculator.emulator.interceptors;

import java.time.Clock;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.events.TextRequestEvent;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;

/**
 * Recognises that a request has been issued via the command input stream.
 */
public class RequestCommandInterceptor implements InputInterceptor {
	private Clock clock;
	
	public RequestCommandInterceptor(Clock clock) {
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
			
			return new TextRequestEvent(clock, data);
		}
		return null;
	}

}
