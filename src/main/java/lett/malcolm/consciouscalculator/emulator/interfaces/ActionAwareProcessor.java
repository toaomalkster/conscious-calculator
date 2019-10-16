package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.util.Queue;

public interface ActionAwareProcessor extends Processor {

	public void setOutputStream(Queue<String> stream);
}
