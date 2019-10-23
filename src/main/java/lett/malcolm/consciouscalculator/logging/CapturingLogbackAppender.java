package lett.malcolm.consciouscalculator.logging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class CapturingLogbackAppender extends AppenderBase<ILoggingEvent> {
	//private static final ConcurrentLinkedQueue<String> events = new ConcurrentLinkedQueue<>();
	private static final Queue<String> events = new LinkedList<>();
	
	public static List<String> getLatestEvents() {
		List<String> list = new ArrayList<>();
		
		String event;
		while ((event = events.poll()) != null) {
			list.add(event);
		}
		
		return list;
	}
	
	/**
	 * Clears the current set of captured events.
	 */
	public static void clear() {
		events.clear();
	}

	@Override
	protected void append(ILoggingEvent event) {
		events.offer(encode(event));
	}

	private String encode(ILoggingEvent event) {
		String loggerName = event.getLoggerName();
		int separatorIdx = loggerName.lastIndexOf(".");
		if (separatorIdx > 0) {
			loggerName = loggerName.substring(separatorIdx+1);
		}
		
		return loggerName + " - " + event.getFormattedMessage();
	}
}
