package lett.malcolm.consciouscalculator.emulator.interfaces;


import java.util.Queue;

/**
 * Input Interceptors primarily act incoming inputs.
 * They identify significant events from what would otherwise be a bland stream of meaningless data.
 * 
 * The output from interceptors are fed into the Attention Attenuator, and also as triggers and inputs
 * to {@link Processor}s.
 * 
 * Interceptors are allowed to hold state. This is especially important as intereptors need to detect
 * events (such as "changes"), from input streams. Changes cannot be detected without an idea of what came
 * before.
 * 
 * @author Malcolm Lett
 */
public interface InputInterceptor {
	/**
	 * Identifies which input this interceptor should be bound to.
	 * Interceptors only receive data from one input stream.
	 * @return
	 */
	public InputDesignator inputDesignator();
	
	/**
	 * Extract meaningful events, if it understands the incoming data.
	 * @param senseDesignator which sense this data came from
	 * @param stream stream of data
	 * @return new event, or nothing
	 */
	public Event intercept(Queue<Object> stream);
}
