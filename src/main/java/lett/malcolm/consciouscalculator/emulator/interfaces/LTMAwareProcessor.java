package lett.malcolm.consciouscalculator.emulator.interfaces;


import lett.malcolm.consciouscalculator.emulator.LongTermMemory;

/**
 * Marks processors that require access to Long Term Memory.
 * 
 * @author Malcolm Lett
 */
public interface LTMAwareProcessor extends Processor {

	public void setLTM(LongTermMemory memory);
}
