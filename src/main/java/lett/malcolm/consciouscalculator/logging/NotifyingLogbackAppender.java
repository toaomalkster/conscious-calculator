/**
 * Conscious Calculator - Emulation of a conscious calculator.
 * Copyright © 2019 Malcolm Lett (malcolm.lett at gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lett.malcolm.consciouscalculator.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Listeners can register themselves, even if only for a period, and then this
 * appender will notify all listeners accordingly.
 * Listeners can choose to register only for events on a specified thread.
 *
 * Thread-safe.
 * 
 * @author Malcolm Lett
 */
public class NotifyingLogbackAppender extends AppenderBase<ILoggingEvent> {
	private Map<Consumer<String>, BiConsumer<String, String>> listeners = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	protected void append(ILoggingEvent event) {
		// thread-safely create conservative copy
		// (using synchronized maps internal mutex across iteration)
		List<BiConsumer<String, String>> metaListeners = new ArrayList<>();
		listeners.values().forEach(ml -> metaListeners.add(ml));

		// call listeners
		String threadName = event.getThreadName();
		String message = encode(event);
		metaListeners.forEach(ml -> ml.accept(threadName, message));
	}
	
	/**
	 * @param listener
	 * @param threadName limit to this thread, or null to not limit
	 */
	public void addListener(Consumer<String> listener, Thread thread) {
		addListener(listener, thread.getName());
	}

	/**
	 * @param listener
	 * @param threadName limit to this thread name, or null to not limit
	 */
	public void addListener(final Consumer<String> listener, final String threadName) {
		if (threadName == null) {
			listeners.put(listener, (tn,msg) -> listener.accept(msg));
		}
		else {
			listeners.put(listener, (tn,msg) -> {
				if (threadName.equals(tn)) {
					listener.accept(msg);
				}
			});
		}
	}
	
	/**
	 * Must use the same object reference as when added.
	 * @param listener
	 */
	public void removeListener(Consumer<String> listener) {
		listeners.remove(listener);
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
