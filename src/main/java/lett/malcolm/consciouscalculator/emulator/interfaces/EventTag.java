package lett.malcolm.consciouscalculator.emulator.interfaces;

/*-
 * #%L
 * Conscious Calculator
 * %%
 * Copyright (C) 2019 Malcolm Lett
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * @author Malcolm Lett
 */
public enum EventTag {
	/**
	 * The event was triggered by a request, probably from the COMMAND input stream.
	 * Upon completion, the user hopes to obtain a response via an Action to the output stream.
	 */
	REQUEST,
	
	/**
	 * The event has been handled by a processor.
	 */
	HANDLED,
	
	/**
	 * A requested action has been completed.
	 * Mainly intended for mirroring the {@link #REQUEST} tag.
	 */
	COMPLETED,
	
	/**
	 * This event represents the accepted resolution/solution/answer
	 * to a prior event.
	 */
	CONCLUSION
}
