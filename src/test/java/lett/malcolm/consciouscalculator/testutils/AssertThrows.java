package lett.malcolm.consciouscalculator.testutils;

import junit.framework.AssertionFailedError;

/**
 * Brutally copied from JUnit5 -- until I've upgrade to JUnit 5.
 * 
 * {@code AssertThrows} is a collection of utility methods that support asserting
 * an exception of an expected type is thrown.
 *
 * @since 5.0
 */
public class AssertThrows {

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable) {

		try {
			executable.run();
		}
		catch (Throwable actualException) {
			if (expectedType.isInstance(actualException)) {
				return (T) actualException;
			}
			else {
				String message = String.format("Expected %s to be thrown, but %s was thrown.", expectedType.getName(), actualException.getClass().getName());
				throw new AssertionFailedError(message);
			}
		}

		String message = String.format("Expected %s to be thrown, but nothing was thrown.", expectedType.getName());
		throw new AssertionFailedError(message);
	}

}