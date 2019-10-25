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
