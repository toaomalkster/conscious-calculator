package lett.malcolm.consciouscalculator.emulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.emulator.lowlevel.Trigger;

/**
 * Bootstrapper for the emulation process.
 * Holds instantiated objects, and manages the control loop via a trigger queue.
 * 
 * The emulator itself, and everything it uses, is single-threaded. To keep things simple.
 * Where the hosting application uses threads, it needs to serialise all access to the emulated components.
 * 
 * The emulator has only one external input: text based messages can be supplied to it.
 * It also has only one external output: it generates text based messages that can be printed to the console, for example.
 * 
 * TODO make the Emulator run in its own thread, and carefully synchronize access on the public methods.
 */
public class Emulator {
	public static final int DEFAULT_WORKING_MEMORY_MAX_SIZE = 100;

	private AttentionAttenuator attentionAttenuator;
	private WorkingMemory workingMemory;
	private List<InputInterceptor> inputInterceptors;
	private List<Processor> processors;
	private Queue<Object> commandStream = new LinkedList<>();
	private Queue<Object> consciousFeedbackStream = new LinkedList<>();
	
	// low-level
	private Queue<Trigger> triggerQueue = new LinkedList<>();
	
	public Emulator() {
		this.workingMemory = new WorkingMemory(DEFAULT_WORKING_MEMORY_MAX_SIZE);
		this.attentionAttenuator = new AttentionAttenuator(commandStream,
				consciousFeedbackStream, workingMemory);
		
		// TODO discover interceptors and processors through class-path scanning
		this.inputInterceptors = new ArrayList<>();
		this.processors = new ArrayList<>();
	}

	/**
	 * Send a signal on the 'command' input.
	 * @param text
	 */
	public void sendCommand(String text) {
		commandStream.offer(text);
		trigger(true);
	}
	
	/**
	 * 
	 * @return
	 */
	// TODO decide on a good output queue that can support multiple consumers
	public Queue<String> getOutput() {
		return null;
	}
	
	private void controlLoop() {
		while (!triggerQueue.isEmpty()) {
			List<Event> interceptedEvents = new ArrayList<>();
			List<Event> processedEvents = new ArrayList<>();
			
			// sense intercepting - 'input'
			for (InputInterceptor interceptor: inputInterceptors) {
				if (InputDesignator.COMMAND.equals(interceptor.senseDesignator())) {
					// clone incoming stream
					Queue<Object> stream = new LinkedList<Object>(commandStream);
					Event event = interceptor.intercept(stream);
					if (event != null) {
						interceptedEvents.add(event);
					}
				}
			}
			
			// processing
			for (Processor processor: processors) {
				interceptedEvents = Collections.unmodifiableList(interceptedEvents);
				Event event = processor.process(interceptedEvents, workingMemory);
				processedEvents.add(event);
			}
			
			// attention
			boolean updated = attentionAttenuator.act(interceptedEvents, processedEvents);
			
			// run conscious feedback loop
			// TODO
			// - generate event from current state of play
			// - feed back into consciousFeedbackStream
			// - add another trigger to queue
			
			// tick cleanup: consume input queues
			commandStream.clear();
			consciousFeedbackStream.clear();
			
			if (updated) {
				trigger();
			}
		}
	}
	
	private void trigger() {
		triggerQueue.offer(new Trigger());
		controlLoop();
	}

	private void trigger(boolean runIfNotRunning) {
		trigger();
		if (runIfNotRunning) {
			controlLoop();
		}
	}
}
