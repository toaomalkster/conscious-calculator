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
package lett.malcolm.consciouscalculator.emulator.events;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lett.malcolm.consciouscalculator.emulator.interfaces.Percept;

/**
 * This class defines rules around what data types and structures may be used in
 * {@code Event.data} and other similar generic data values.
 * 
 * Generic data values may only be one of the following types, which may be recursive:
 * <ul>
 * <li> null
 * <li> String
 * <li> Integer
 * <li> Double
 * <li> Boolean
 * <li> Percept
 * <li> List
 * <li> Map<by string>
 * </ul>
 * Note: when adding any types to the list above, they MUST implement equals() and hashCode()
 * for 'exact' match.
 * 
 * 
 * Other rules for data types:
 * <ul>
 * <li> Cycles and graphs are permitted, but all referenced objects should exist within the data structure -- cannot be verified automatically
 * </ul>
 * 
 * These data rules ensure that arbitrary events can be processed by processors which don't necessarily know 
 * the data structure a-priori. This enables more generic processors.
 * Additionally, it makes it easier and more deterministic to clone and persist data.
 * 
 * <h3>Problematic potentially supportable data types</h3>
 * The following data types could possibly be supported by this class, but they are problematic
 * because we cannot deterministically cycle between object and marshaled representations without
 * loss.
 * <ul>
 * <li> Set - marshals to the same thing as a List
 * <li> Enum - loses the type when marshaling, so unmarshals to a string.
 * </ul> 
 * 
 * @author Malcolm Lett
 */
// TODO should it clean-up collection sub-types to basic List/Map (could possibly convert any Collection to List at same time)
public class DataRules {
	private static final int EXPECTED_MAX_NUMBER_OF_OBJECTS = 100;
	private static final Set<Class<?>> IMMUTABLE_SIMPLE_TYPES = new HashSet<Class<?>>();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	// Want to approximate numbers of words in a statement.
	// Average English word length is approximately 5. Plus add one for space character.
	private static final int LETTERS_PER_SIZE_UNIT = 6;
	
	static {
		IMMUTABLE_SIMPLE_TYPES.add(String.class);
		IMMUTABLE_SIMPLE_TYPES.add(Integer.class);
		IMMUTABLE_SIMPLE_TYPES.add(Double.class);
		IMMUTABLE_SIMPLE_TYPES.add(Boolean.class);
	}

	/**
	 * Checks that the object meets the data rules.
	 * @param obj
	 * @throws IllegalArgumentException if not valid
	 */
	public static void assertValid(Object obj) {
		assertValid(obj, new CycleHandler());
	}
	
	/**
	 * Calculates the total size of the given object.
	 * Provides an indicative number in a unified way that works across all data types.
	 * For example, used to help set {@code Event.size}.
	 * 
	 * Not a real size, so don't try to use it to determine the number of items an a collection.
	 * 
	 * @param obj
	 * @return
	 */
	public static int measureSize(Object obj) {
		assertValid(obj);
		return measureSize(obj, new CycleHandler());
	}
	
	/**
	 * Checks whether the provided object graphs are identical. 
	 * @param obj1 must meet data rules
	 * @param obj2 must meet data rules
	 */
	public static boolean isSame(Object obj1, Object obj2) {
		assertValid(obj1);
		assertValid(obj2);
		
		// now can use straight Object.equals()
		return (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
	}
	
	/**
	 * Returns a deep clone of the given object.
	 * @param obj
	 * @return
	 */
	public static <T> T clone(T obj) {
		assertValid(obj);
		return clone(obj, new CycleHandler());
	}
	
	/**
	 * Generates a convenient string representation, primarily intended for use
	 * in logging.
	 * Similar to {@link #marshal(Object)}, but uses a simplified notation
	 * for simple scalar types, that looses the exact data type in some cases.
	 * Uses the result of {@link #marshal(Object)} for collection and complex data structures.
	 * 
	 * Strings returned by this method <em>cannot</em> be unmarshalled back to the original data structure.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String stringOf(Object obj) {
		assertValid(obj);
		
		if (obj == null) {
			return "null";
		}
		else if (obj instanceof Boolean) {
			return Boolean.TRUE.equals(obj) ? "true" : "false";
		}
		else if (IMMUTABLE_SIMPLE_TYPES.contains(obj.getClass())) {
			return String.valueOf(obj);
		}
		else if (obj instanceof Percept) {
			return String.valueOf(obj);
		}
		else if (obj instanceof Collection) {
			StringBuilder buf = new StringBuilder();
			buf.append("[");
			boolean first = true;
			for (Object item: (Collection<?>)obj) {
				if (!first) buf.append(",");
				buf.append(stringOf(item));
				first = false;
			}
			buf.append("]");
			return buf.toString();
		}
		else if (obj instanceof Map) {
			StringBuilder buf = new StringBuilder();
			buf.append("{");
			boolean first = true;
			for (Map.Entry<String, Object> entry: ((Map<String, Object>)obj).entrySet()) {
				if (!first) buf.append(",");
				buf.append(entry.getKey()).append(":").append(stringOf(entry.getValue()));
				first = false;
			}
			buf.append("}");
			return buf.toString();
		}
		else {
			throw new UnsupportedOperationException("Don't know how to handle " + obj.getClass().getName());
		}
	}
	
	/**
	 * Marshals simple scalar types to a deterministic string representation,
	 * and everything else to JSON string, in compact mode.
	 * 
	 * The marshaled format can be reliably converted back to original data structure
	 * via a call to {@link #unmarshal(String)}.
	 * @param obj
	 * @return
	 */
	public static String marshal(Object obj) {
		assertValid(obj);
		try {
			return OBJECT_MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// unexpected
			throw new IllegalArgumentException("Unable to marshal object: " + e.getMessage(), e);
		}
	}

	/**
	 * Unmarshals from strings generated by {@link #marshal(Object)}.
	 * @param json
	 * @return
	 */
	public static Object unmarshal(String json) {
		try {
			return OBJECT_MAPPER.readValue(json, Object.class);
		} catch (IOException e) {
			// unexpected
			throw new IllegalArgumentException("Unable to unmarshal string: " + json, e);
		}
	}
	
	/**
	 * Recursively checks that the object meets the data rules.
	 * @param obj
	 * @throws IllegalArgumentException if not valid
	 */
	@SuppressWarnings("unchecked")
	private static void assertValid(Object obj, CycleHandler cycles) {
		if (obj == null) {
			return;
		}
		
		if (!cycles.observeAndIsDuplicate(obj)) {
			if (!isValidImmediateType(obj)) {
				throw new IllegalArgumentException("Objects of type " + obj.getClass().getName()+" not permitted by Data Rules");
			}
			
			// recurse
			if (obj instanceof List) {
				for (Object item: (List<Object>) obj) {
					assertValid(item, cycles);
				}
			}
			else if (obj instanceof Map) {
				for (Map.Entry<String,Object> entry: ((Map<String,Object>) obj).entrySet()) {
					assertValid(entry.getValue(), cycles);
				}
			}
		}
	}
	
	/**
	 * Recursively calculates the total size of the given object.
	 * Assumes already validated object types.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static int measureSize(Object obj, CycleHandler cycles) {
		if (obj == null) {
			// rationale: a list of N nulls = 1 + N (and a neural network could use the list size to determine things)
			return 1;
		}
		else if (cycles.observeAndIsDuplicate(obj)) {
			// 1 unit for a reference
			return 1;
		}
		else {
			if (obj instanceof String) {
				// one 'unit' for each group of characters
				return (int) Math.ceil(((String) obj).length() / (double)LETTERS_PER_SIZE_UNIT);
			}
			else if (IMMUTABLE_SIMPLE_TYPES.contains(obj.getClass())) {
				return 1;
			}
			else if (obj instanceof Percept) {
				return ((Percept) obj).size();
			}
			
			// recursive types
			// (1 for the collection itself)
			int size = 1;
			
			// recurse
			if (obj instanceof List) {
				for (Object item: (List<Object>) obj) {
					size += measureSize(item, cycles);
				}
			}
			else if (obj instanceof Map) {
				// in maps, the keys are not counted
				// -- treated like Class member lookup-lists in Java program: they are stored only once in executional memory
				for (Map.Entry<String,Object> entry: ((Map<String,Object>) obj).entrySet()) {
					size += measureSize(entry.getValue(), cycles);
				}
			}
			else {
				throw new UnsupportedOperationException("Don't know how to handle " + obj.getClass().getName());
			}
			
			return size;
		}
	}
	
	/**
	 * Recursively generates a deep clone of the given object.
	 * Assumes already validated obj types.
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T clone(T obj, CycleHandler cycles) {
		if (obj == null) {
			return null;
		}
		
		// handle cycles
		Object clone = cycles.getObservedMirror(obj);
		if (clone != null) {
			return (T) clone;
		}

		// clone
		if (IMMUTABLE_SIMPLE_TYPES.contains(obj.getClass())) {
			// immutable, so no need to clone
			clone = obj;
		}
		else if (obj instanceof Percept) {
			clone = ((Percept) obj).clone();
		}
		else if (obj instanceof List) {
			List<Object> cloneList = new ArrayList<>();
			for (Object item: (List<Object>) obj) {
				cloneList.add(clone(item, cycles));
			}
			clone = cloneList;
		}
		else if (obj instanceof Map) {
			Map<String,Object> cloneMap = new HashMap<>();
			for (Map.Entry<String,Object> entry: ((Map<String,Object>) obj).entrySet()) {
				cloneMap.put(entry.getKey(), clone(entry.getValue(), cycles));
			}
			clone = cloneMap;
		}
		else {
			throw new UnsupportedOperationException("Don't know how to handle " + obj.getClass().getName());
		}
		
		// track and return
		cycles.observeMirror(obj, clone);
		return (T) clone;
	}
	
	/**
	 * Uses object reference for identity.
	 * 
	 * Works in two modes:
	 * - object only
	 * - map from 'object' to 'mirror'
	 * 
	 * Where docs refer to 'object', they always mean on the 'key' side of the map.
	 */
	private static class CycleHandler {
		private Map<Object, Object> observed = new IdentityHashMap<>(EXPECTED_MAX_NUMBER_OF_OBJECTS);
		
		/**
		 * Records that an obj has been observed, and indicates whether it was previously observed.
		 * Always ignores nulls.
		 * @param obj
		 * @return
		 */
		public boolean observeAndIsDuplicate(Object obj) {
			if (obj != null) {
				return observed.put(obj, obj) != null;
			}
			return false;
		}

		/**
		 * Always ignores nulls.
		 * @param obj
		 * @param mirror
		 */
		public void observeMirror(Object obj, Object mirror) {
			if (obj != null) {
				observed.put(obj, mirror);
			}
		}
		
		public Object getObservedMirror(Object obj) {
			return observed.get(obj);
		}
	}

	/**
	 * Non-recursive.
	 * @param obj
	 * @return
	 */
	private static boolean isValidImmediateType(Object obj) {
		if (IMMUTABLE_SIMPLE_TYPES.contains(obj.getClass())) {
			return true;
		}
		else if (obj instanceof Percept) {
			return true;
		}
		else if (obj instanceof List) {
			return true;
		}
		else if (obj instanceof Map) {
			return true;
		}
		return false;
	}
}
