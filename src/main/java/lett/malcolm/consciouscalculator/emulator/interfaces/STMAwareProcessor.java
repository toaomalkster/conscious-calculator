package lett.malcolm.consciouscalculator.emulator.interfaces;


import lett.malcolm.consciouscalculator.emulator.ShortTermMemory;

/**
 * Marks processors that require access to Short Term Memory.
 * 
 * @author Malcolm Lett
 */
public interface STMAwareProcessor extends Processor {

	public void setSTM(ShortTermMemory memory);
}
