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
package lett.malcolm.consciouscalculator.testutils;

import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class MyHamcrest {
	public static <T> Matcher<Iterable<? super T>> hasItem(Predicate<T> predicate) {
		Matcher<T> itemMatcher = new DiagnosingMatcher<T>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("matches predicate");
			}

			@Override
			@SuppressWarnings("unchecked")
			protected boolean matches(Object itemObj, Description mismatchDescription) {
				T item;
				try {
					item = (T) itemObj;
				} catch (ClassCastException e) {
					mismatchDescription.appendText("was a ").appendValue(itemObj.getClass().getName());
					return false;
				}
				
				if (!predicate.test(item)) {
					mismatchDescription.appendText("was ").appendValue(item);
					return false;
				}
				return true;
			}
		};
		
		return Matchers.hasItem(itemMatcher);
	}
}
