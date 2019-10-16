package lett.malcolm.consciouscalculator.emulator;

import org.junit.Test;

public class TryEmulator {
	@Test
	public void try1() {
		Emulator emulator = new Emulator();
		emulator.sendCommand("3 + 5");
	}
}
