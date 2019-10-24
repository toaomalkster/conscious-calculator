package lett.malcolm.consciouscalculator.emulator;


import org.junit.Test;

/**
 * @author Malcolm Lett
 */
public class TryEmulator {
	@Test
	public void evaluate3Plus5() {
		Emulator emulator = new Emulator();
		emulator.sendCommand("3 + 5");
	}

	@Test
	public void evaluateTestableEquation() {
		Emulator emulator = new Emulator();
		emulator.sendCommand("3 + 5 = 9");
	}
	
	@Test
	public void solve3PlusUnknownEquals8() {
		Emulator emulator = new Emulator();
		emulator.sendCommand("3 + ? = 8");
	}
}
