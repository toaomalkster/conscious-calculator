package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.time.Instant;
import java.util.Set;

public interface Event extends Cloneable {
	
	/**
	 * Deep clone.
	 * @return
	 */
	public Event clone();
	
	public String guid();
	
	public Instant timestamp();
	
	public double strength();
	
	/**
	 * Comparative size of the event.
	 * Represents how much space taken up in Working Memory, Short Term Memory, or Long Term Memory. 
	 * @return 0 or positive number
	 */
	public int size();
	
	/**
	 * Meta-data, used to identify broad categories of events an different scales,
	 * and to flag state against the event.
	 * eg: to flag that the event was a request, or that it has been completed.
	 * @return
	 */
	public Set<EventTag> tags();
	
	public Object data();

	public void setStrength(double strength);

	public void setTimestamp(Instant timestamp);

	public void setSize(int size);

	public void setGuid(String guid);

	public void setTags(Set<EventTag> tags);

	public void setData(Object data);
}
