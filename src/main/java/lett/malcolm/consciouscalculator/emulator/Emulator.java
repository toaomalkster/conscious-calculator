package lett.malcolm.consciouscalculator.emulator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lett.malcolm.consciouscalculator.emulator.interceptors.ConsciousFeedbackToSTMInterceptor;
import lett.malcolm.consciouscalculator.emulator.interceptors.RequestCommandInterceptor;
import lett.malcolm.consciouscalculator.emulator.interceptors.StuckThoughtInterceptor;
import lett.malcolm.consciouscalculator.emulator.interfaces.ActionAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Event;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputDesignator;
import lett.malcolm.consciouscalculator.emulator.interfaces.InputInterceptor;
import lett.malcolm.consciouscalculator.emulator.interfaces.LTMAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.interfaces.Processor;
import lett.malcolm.consciouscalculator.emulator.interfaces.STMAwareProcessor;
import lett.malcolm.consciouscalculator.emulator.lowlevel.Trigger;
import lett.malcolm.consciouscalculator.emulator.processors.EquationEvaluationProcessor;
import lett.malcolm.consciouscalculator.emulator.processors.ExpressionAndEquationParseProcessor;
import lett.malcolm.consciouscalculator.emulator.processors.ExpressionEvaluationProcessor;
import lett.malcolm.consciouscalculator.emulator.processors.ExpressionResponseProcessor;
import lett.malcolm.consciouscalculator.emulator.processors.FindMatchingConceptProcessor;
import lett.malcolm.consciouscalculator.emulator.processors.SpeakActionProcessor;

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
	public static final int DEFAULT_SHORT_TERM_MEMORY_MAX_SIZE = 1000;
	public static final int DEFAULT_LONG_TERM_MEMORY_MAX_SIZE = 1_000_000;
	
	// number of ticks with no event updates before stopping
	public static final int STAGNANT_TRIGGER_TOLERANCE = 5;
	
	private static final Logger logger = LoggerFactory.getLogger(Emulator.class);

	private Clock clock;
	private AttentionAttenuator attentionAttenuator;
	private WorkingMemory workingMemory;
	private ShortTermMemory shortTermMemory;
	private LongTermMemory longTermMemory;
	private ConsciousFeedbacker consciousFeedbacker;
	private ConsciousFeedbackToSTMInterceptor consciousFeedbackToSTMInterceptor;
	
	// interceptors and processors
	private List<InputInterceptor> inputInterceptors;
	private List<Processor> processors;
	
	// streams
	private Queue<Object> commandStream = new LinkedList<>();
	private Queue<Object> consciousFeedbackStream = new LinkedList<>();
	private Queue<String> outputStream = new LinkedList<>();
	
	// low-level
	private Queue<Trigger> triggerQueue = new LinkedList<>();
	
	public Emulator() {
		this.clock = Clock.systemDefaultZone();
		this.workingMemory = new WorkingMemory(DEFAULT_WORKING_MEMORY_MAX_SIZE);
		this.shortTermMemory = new ShortTermMemory(DEFAULT_SHORT_TERM_MEMORY_MAX_SIZE);
		this.longTermMemory = new LongTermMemory(DEFAULT_LONG_TERM_MEMORY_MAX_SIZE);
		this.attentionAttenuator = new AttentionAttenuator(commandStream,
				consciousFeedbackStream, workingMemory);
		this.consciousFeedbacker = new ConsciousFeedbacker(workingMemory);
		this.consciousFeedbackToSTMInterceptor = new ConsciousFeedbackToSTMInterceptor(clock, shortTermMemory);
		
		// TODO discover interceptors and processors through class-path scanning
		this.inputInterceptors = new ArrayList<>();
		this.processors = new ArrayList<>();
		
		inputInterceptors.add(consciousFeedbackToSTMInterceptor);
		inputInterceptors.add(new RequestCommandInterceptor(clock));
		inputInterceptors.add(new StuckThoughtInterceptor(clock));
		processors.add(new ExpressionEvaluationProcessor(clock));
		processors.add(new EquationEvaluationProcessor(clock));
		processors.add(new ExpressionAndEquationParseProcessor(clock));
		processors.add(new ExpressionResponseProcessor(clock));
		processors.add(new SpeakActionProcessor(clock));
		processors.add(new FindMatchingConceptProcessor(clock));
		
		for (Processor processor: processors) {
			if (processor instanceof ActionAwareProcessor) {
				((ActionAwareProcessor) processor).setOutputStream(outputStream);
			}
			if (processor instanceof STMAwareProcessor) {
				((STMAwareProcessor) processor).setSTM(shortTermMemory);
			}
			if (processor instanceof LTMAwareProcessor) {
				((LTMAwareProcessor) processor).setLTM(longTermMemory);
			}
		}
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
		int ticksWithoutUpdates = 0;
		while (triggerQueue.poll() != null) {
			List<Event> interceptedEvents = new ArrayList<>();
			List<List<Event>> processedEventSets = new ArrayList<>();
			boolean updated = false;
			
			// input intercepting
			for (InputInterceptor interceptor: inputInterceptors) {
				// clone incoming stream
				Queue<Object> stream = new LinkedList<Object>(getInputStream(interceptor.inputDesignator()));
				Event event = interceptor.intercept(stream);
				if (event != null) {
					interceptedEvents.add(event);
				}
			}
			updated |= !interceptedEvents.isEmpty();
			
			// processing
			for (Processor processor: processors) {
				interceptedEvents = Collections.unmodifiableList(interceptedEvents);
				List<Event> eventSet = processor.process(interceptedEvents, workingMemory);
				if (eventSet != null && !eventSet.isEmpty()) {
					processedEventSets.add(eventSet);
				}
			}
			updated |= !processedEventSets.isEmpty();
			logger.trace("Events: " + processedEventSets);
			
			// attention
			updated |= attentionAttenuator.act(interceptedEvents, processedEventSets);

			// tick cleanup: consume input queues
			// (has to go here, because we'll next push data onto the consciousFeedbackStream and want that to be feed back into the next loop)
			commandStream.clear();
			consciousFeedbackStream.clear();
			
			// run conscious feedback loop
			consciousFeedbacker.writeTo(consciousFeedbackStream);
			
			// degrade strengths
			workingMemory.degradeStrengths();
			
			// handle loop
			if (updated) {
				ticksWithoutUpdates = 0;
			}
			else {
				ticksWithoutUpdates++;
			}
			if (updated || ticksWithoutUpdates < STAGNANT_TRIGGER_TOLERANCE) {
				trigger();
			}
		}
	}
	
	private Queue<Object> getInputStream(InputDesignator designator) {
		switch (designator) {
		case COMMAND: return commandStream;
		case CONSCIOUS_FEEDBACK: return consciousFeedbackStream;
		default:
			throw new UnsupportedOperationException(InputDesignator.class.getSimpleName()+" "+designator+" not recognised");
		}
	}
	
	private void trigger() {
		triggerQueue.offer(new Trigger());
	}

	private void trigger(boolean runIfNotRunning) {
		trigger();
		if (runIfNotRunning) {
			controlLoop();
		}
	}
}
