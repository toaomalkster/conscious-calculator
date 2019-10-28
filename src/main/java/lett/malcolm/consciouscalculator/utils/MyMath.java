package lett.malcolm.consciouscalculator.utils;

public class MyMath {
	/**
	 * Returns a new random number within the specified range, inclusively.
	 * @param min inclusive min
	 * @param max inclusive max
	 * @return random number within range
	 */
	public static int randMinMax(int min, int max) {
		// note: +1 important because Math.random() returns in range [0.0, 1.0)
		return (int) Math.floor(Math.random() * (max - min + 1)) + min;
	}
}
