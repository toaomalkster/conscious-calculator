package lett.malcolm.consciouscalculator.emulator;

import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class ConsciousFeedbacker {
	private WorkingMemory workingMemory;

	/**
	 * @param workingMemory
	 */
	public ConsciousFeedbacker(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}
	
	/**
	 * Summarises the current state of the emulated consciousness,
	 * within the scope of awareness, and writes to the {@link #resultStream}.
	 * 
	 * Scope of awareness:
	 * - top-most event within Working Memory -- (ideally all of Working Memory would be considered in priority order, but not currently)
	 * 
	 * Not in scope of awareness:
	 * - execution of Actions - these happen "mysteriously"
	 * 
	 * Writes to the result stream every 'tick', regardless of the level of activity.
	 * 
	 * @param consciousFeedbackStream stream to write to
	 */
	public void writeTo(Queue<Object> consciousFeedbackStream) {
		Event top = workingMemory.top();
		
		// process, filter, and simplify the state of working-memory
		ConsciousState summary = new ConsciousState();
		
		// summarise state of working-memory by holding a copy of the top-most event only
		// (TODO consider whether to convert this into a MemoryEvent now or later)
		if (top != null) {
			// TODO if ever start adding linkages between events, this probably wants to collapse some of those linkages down
			summary.setTop(top.clone());
		}
		
		consciousFeedbackStream.offer(summary);
	}
	
	/**
	 * Represents current summarised and simplified state of data within field of conscious awareness.
	 */
	public static class ConsciousState {
		private Event top;
		
		ConsciousState() {
		}

		/**
		 * Null if nothing going on
		 * @return
		 */
		public Event getTop() {
			return top;
		}

		void setTop(Event top) {
			this.top = top;
		}
	}
}
