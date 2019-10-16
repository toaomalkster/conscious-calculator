package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.time.Instant;

public interface Event {
	public String guid();
	
	public Instant timestamp();
	
	public double strength();
	
	/**
	 * Comparative size of the event.
	 * Represents how much space taken up in Working Memory, Short Term Memory, or Long Term Memory. 
	 * @return 0 or positive number
	 */
	public int size();
	
	public Object data();
}
