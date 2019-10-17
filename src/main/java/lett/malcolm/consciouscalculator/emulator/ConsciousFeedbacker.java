package lett.malcolm.consciouscalculator.emulator;

import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class ConsciousFeedbacker {
	private Queue<Object> resultStream;
	private WorkingMemory workingMemory;

	/**
	 * @param resultStream stream to write to
	 * @param workingMemory
	 */
	public ConsciousFeedbacker(Queue<Object> resultStream,
			WorkingMemory workingMemory) {
		this.resultStream = resultStream;
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
	 */
	public void process() {
		Event top = workingMemory.top();
		
		// TODO process, filter, and simplify the state of working-memory
		Event summary = top.clone(); // TODO if ever start adding linkages between events, this probably wants to collapse those linkages down
		resultStream.offer(summary);
	}
}
