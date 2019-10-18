package lett.malcolm.consciouscalculator.utils;

import java.math.BigDecimal;

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
