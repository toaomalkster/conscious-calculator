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
package lett.malcolm.consciouscalculator.emulator.events;


import static lett.malcolm.consciouscalculator.testutils.AssertThrows.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Test;

import lett.malcolm.consciouscalculator.emulator.interfaces.EventTag;

/**
 * @author Malcolm Lett
 */
public class DataRulesTests {
	@Test
	public void acceptsValidTypes() {
		DataRules.assertValid(null);
		DataRules.assertValid("some text");
		DataRules.assertValid(3);
		DataRules.assertValid(0.51234);
		DataRules.assertValid(true);
		DataRules.assertValid(listOf("some", "text"));
		DataRules.assertValid(listOf(3, 5, 9));
		DataRules.assertValid(mapOf("A", 3, "B", true, "C", "text"));
		
		DataRules.assertValid(listOf("some", 3, 0.42, true,
				mapOf(
						"A", listOf(true, "true"),
						"B", false,
						"C", mapOf("crayons", 3, "marmalade", 5))));
	}
	
	@Test
	public void rejectsInvalidTypes() {
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid(3L));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid(setOf("A", "B")));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid((Supplier<Boolean>) () -> true));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid(new Object()));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid((float) 2.2));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid('h'));
		assertThrows(IllegalArgumentException.class, () -> DataRules.assertValid(EventTag.COMPLETED));
	}
	
	@Test
	public void sizesSimpleTypes() {
		assertThat(DataRules.measureSize(null), is(1));
		assertThat(DataRules.measureSize(3), is(1));
		assertThat(DataRules.measureSize(31234234), is(1));
		assertThat(DataRules.measureSize(123423.234324), is(1));
		assertThat(DataRules.measureSize(true), is(1));
		assertThat(DataRules.measureSize(false), is(1));
		assertThat(DataRules.measureSize("A"), is(1));
		assertThat(DataRules.measureSize("buy"), is(1));
		assertThat(DataRules.measureSize("hello"), is(1));
		assertThat(DataRules.measureSize("oranges and lemons"), is(3));
	}
	
	@Test
	public void sizeSimpleCollectionTypes() {
		assertThat(DataRules.measureSize(listOf()), is(1));
		assertThat(DataRules.measureSize(mapOf()), is(1));

		assertThat(DataRules.measureSize(listOf("A", "B", "C", "D", "E")), is(1 + 5));
		assertThat(DataRules.measureSize(listOf(null, null, null, null, null)), is(1 + 5));
		assertThat(DataRules.measureSize(listOf(listOf(), mapOf(), listOf(), mapOf(), listOf())), is(1 + 5));
		
		// in maps, the keys are not counted
		assertThat(DataRules.measureSize(mapOf("A", 1, "B", 2, "C", 3)), is(1 + 3));
		assertThat(DataRules.measureSize(mapOf("really-long-key-name", 1)), is(1 + 1));
	}
		
	// complex object:
	// [                                 +1       (collection itself)
	//   "some", 3, 0.42, true,          +4
	//   {                               +1       (collection itself)
	//     A: [true, "true"],            +1 +2    (collection + 2 items x 1 unit each)
	//     B: false,                     +1
	//     C: {                          +1       (collection itself)
	//       crayons: 3,                 +1       (value only)
	//       marmalade: 5                +1       (value only)
	//     }
	//   }
	// ]
	// total: 13
	@Test
	public void sizeComplexObject() {
		assertThat(DataRules.measureSize(listOf("some", 3, 0.42, true,
				mapOf(
						"A", listOf(true, "true"),
						"B", false,
						"C", mapOf("crayons", 3, "marmalade", 5)))),
				is(13));
		
	}
	
	@Test
	public void clonesObjects() {
		assertThat(DataRules.clone(null), is(nullValue()));
		assertThat(DataRules.clone("some text"), is("some text"));
		assertThat(DataRules.clone(3), is(3));
		assertThat(DataRules.clone(0.5234), is(0.5234));
		assertThat(DataRules.clone(true), is(true));
		assertThat(DataRules.clone(listOf("A", 3, true)), is(listOf("A", 3, true)));
		assertThat(DataRules.clone(mapOf("A", 3, "B", true)), is(mapOf("A", 3, "B", true)));
		
		assertThat(DataRules.clone(
				listOf("some", 3, 0.42, true,
						mapOf(
								"A", listOf(true, "true"),
								"B", false,
								"C", mapOf("crayons", 3, "marmalade", 5)))),
				
				is(listOf("some", 3, 0.42, true,
						mapOf(
								"A", listOf(true, "true"),
								"B", false,
								"C", mapOf("crayons", 3, "marmalade", 5)))));
	}

	@Test
	public void marshalsSimpleTypes() {
		assertThat(DataRules.marshal(null), is("null"));
		assertThat(DataRules.marshal("some text"), is("\"some text\""));
		assertThat(DataRules.marshal(3), is("3"));
		assertThat(DataRules.marshal(0.5234), is("0.5234"));
		assertThat(DataRules.marshal(true), is("true"));
	}

	// FIXME sets and lists marshal to same thing
	@Test
	public void marshalsSimpleCollectionTypes() {
		assertThat(DataRules.marshal(listOf("some", "text")), is("[\"some\",\"text\"]"));
		assertThat(DataRules.marshal(listOf(3, 5, 9)), is("[3,5,9]"));
		assertThat(DataRules.marshal(mapOf("A", 3, "B", true, "C", "text")), is("{\"A\":3,\"B\":true,\"C\":\"text\"}"));
	}
	
	@Test
	public void unmarshalsSimpleTypes() {
		assertThat(DataRules.unmarshal("null"), is(nullValue()));
		assertThat(DataRules.unmarshal("3"), is(3));
		assertThat(DataRules.unmarshal("0.5234"), is(0.5234));
		assertThat(DataRules.unmarshal("true"), is(true));
		assertThat(DataRules.unmarshal("false"), is(false));
	}

	// FIXME cannot unmarshal sets
	@Test
	public void unmarshalsSimpleCollectionTypes() {
		assertThat(DataRules.unmarshal("[\"some\",\"text\"]"), is(listOf("some", "text")));
		assertThat(DataRules.unmarshal("[3,5,9]"), is(listOf(3, 5, 9)));
		assertThat(DataRules.unmarshal("{\"A\":3,\"B\":true,\"C\":\"text\"}"), is(mapOf("A", 3, "B", true, "C", "text")));
	}
	
	@Test
	public void toStringsSimpleTypes() {
		assertThat(DataRules.stringOf(null), is("null"));
		assertThat(DataRules.stringOf("some text"), is("some text"));
		assertThat(DataRules.stringOf(3), is("3"));
		assertThat(DataRules.stringOf(0.5234), is("0.5234"));
		assertThat(DataRules.stringOf(true), is("true"));
	}

	@Test
	public void toStringsCollectionAndComplexTypes() {
		assertThat(DataRules.stringOf(listOf("some", "text")), is("[some,text]"));
		assertThat(DataRules.stringOf(listOf(3, 5, 9)), is("[3,5,9]"));
		assertThat(DataRules.stringOf(mapOf("A", 3, "B", true, "C", "text")), is("{A:3,B:true,C:text}"));
	}
	
	private static List<Object> listOf(Object... items) {
		return Arrays.asList(items);
	}

	private static Set<Object> setOf(Object... items) {
		return new HashSet<>(Arrays.asList(items));
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Map<String, T> mapOf(Object... keyValuePairs) {
		Map<String, T> map = new HashMap<>();
		for (int i=0; i < keyValuePairs.length; i+=2) {
			String key = (String) keyValuePairs[i];
			T value = (T) keyValuePairs[i+1];
			map.put(key, value);
		}
		return map;
	}
}
