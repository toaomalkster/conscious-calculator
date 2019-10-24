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
package lett.malcolm.consciouscalculator.emulator.interfaces;

import java.util.List;

/**
 * Haven't totally decided on scope or purpose of this yet.
 * 
 * I think this represents only pre-programmed facts, as a quick way of pulling together
 * both a unique reference (guid), and other meta-data such as allowed data types
 * and it's "name" when spoken about.
 * In Long Term Memory, this is all stored across multiple Percepts, whereas this holds that together
 * in a convenient place for use within pre-programmed processors.
 * 
 * It's possible this could be used for learned facts too, as needed by learned processors.
 * 
 * The "name" of the fact is the class name, without the "Fact" suffix.
 * 
 * @author Malcolm Lett
 */
public interface Fact {

	/**
	 * Pre-programmed facts have the simple classname as the guid.
	 * Otherwise it's a unique guid.
	 * @return
	 */
	public String guid();
	
	/**
	 * Recognised data types for this fact.
	 * @return null, empty, or some
	 */
	public List<Class<?>> dataTypes();
}
