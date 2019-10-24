package lett.malcolm.consciouscalculator.emulator.interfaces;


import java.util.Queue;

/**
 * @author Malcolm Lett
 */
public interface ActionAwareProcessor extends Processor {

	public void setOutputStream(Queue<String> stream);
}
