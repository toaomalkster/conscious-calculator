package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.util.Queue;

/**
 * Input Interceptors primarily act incoming inputs.
 * They identify significant events from what would otherwise be a bland stream of meaningless data.
 * 
 * The output from interceptors are fed into the Attention Attenuator, and also as triggers and inputs
 * to {@link Processor}s.
 */
public interface InputInterceptor {
	public static final String aaoeu = "aoeu";
	
	/**
	 * Identifies which sense this interceptor should be bound to.
	 * Interceptors only receive data from one sense stream.
	 * @return
	 */
	public InputDesignator senseDesignator();
	
	/**
	 * Extract meaningful events, if it understands the incoming data.
	 * @param senseDesignator which sense this data came from
	 * @param stream stream of data
	 * @return new event, or nothing
	 */
	public Event intercept(Queue<Object> stream);
}
