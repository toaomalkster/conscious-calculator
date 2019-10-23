package lett.malcolm.consciouscalculator.utils;

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

import java.math.BigDecimal;

/**
 * @author Malcolm Lett
 */
public class QuantityUtils {
	public static String toShortMillisString(long millis) {
		if (millis < 1000) {
			return millis + "ms";
		}
		else {
			return String.format("%.1fs", millis / 1000.0);
		}
	}
	
	private static double round(double value, int decimalPlaces) {
		// TODO try this
		BigDecimal decimal = new BigDecimal(value).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
		return decimal.doubleValue();
		//new BigDecimal(String.valueOf(value)).setScale(yourScale, BigDecimal.ROUND_HALF_UP);
	}
}
