/**
 * Conscious Calculator - Emulation of a conscious calculator.
 * Copyright © 2019 Malcolm Lett (malcolm.lett at gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
