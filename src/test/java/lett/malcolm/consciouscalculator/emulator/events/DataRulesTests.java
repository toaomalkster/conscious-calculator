package lett.malcolm.consciouscalculator.emulator.events;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class DataRulesTests {

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
		assertThat(DataRules.marshal(setOf("some", "text")), is("[\"some\",\"text\"]"));
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
		assertThat(DataRules.stringOf(listOf("some", "text")), is("[\"some\",\"text\"]"));
		assertThat(DataRules.stringOf(listOf(3, 5, 9)), is("[3,5,9]"));
		assertThat(DataRules.stringOf(setOf("some", "text")), is("[\"some\",\"text\"]"));
		assertThat(DataRules.stringOf(mapOf("A", 3, "B", true, "C", "text")), is("{\"A\":3,\"B\":true,\"C\":\"text\"}"));
	}
	
	private static <T> List<T> listOf(T... items) {
		return Arrays.asList(items);
	}
	
	private static <T> Set<T> setOf(T... items) {
		return new HashSet<>(listOf(items));
	}
	
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
