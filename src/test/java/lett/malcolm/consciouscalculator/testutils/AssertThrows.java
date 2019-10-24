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
