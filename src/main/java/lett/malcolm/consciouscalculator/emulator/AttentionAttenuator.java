package lett.malcolm.consciouscalculator.emulator;

import java.util.List;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

public class AttentionAttenuator {
	private Queue<? extends Object> commandStream;
	private Queue<? extends Object> consciousFeedbackStream;
	private WorkingMemory workingMemory;
	
	public AttentionAttenuator(
			Queue<? extends Object> commandStream,
			Queue<? extends Object> consciousFeedbackStream,
			WorkingMemory workingMemory) {
		this.commandStream = commandStream;
		this.consciousFeedbackStream = consciousFeedbackStream;
		this.workingMemory = workingMemory;
	}

	/**
	 * Decides what to do given:
	 * - raw sense inputs
	 * - events from interceptors
	 * - events from processors
	 * - current state of working memory
	 * 
	 * Outcomes:
	 * - updates state of working memory
	 * @param interceptedEvents
	 * @param processedEvents
	 */
	public void act(List<Event> interceptedEvents, List<Event> processedEvents) {
		
	}
	
}
