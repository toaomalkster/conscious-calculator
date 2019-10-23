package lett.malcolm.consciouscalculator.emulator.events;

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

import java.time.Clock;

import lett.malcolm.consciouscalculator.emulator.interfaces.Event;

/**
 * Identifies a moment when thought seems to have either completely stalled,
 * or is going around in loops.
 */
public class StuckThoughtEvent extends BaseEvent implements Event {
	public StuckThoughtEvent(Clock clock, String latestEventGuid) {
		super(clock);
		this.references().add(latestEventGuid);
		
		// best not to store a null value
		this.setData(true);
	}
}
