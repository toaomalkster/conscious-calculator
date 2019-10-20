package lett.malcolm.consciouscalculator.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for Map<String,Object> maps.
 */
public class MapBuilder<K, V> {
	private Map<K, V> map = new HashMap<>();
	
	public static MapBuilder<String, Object> aDataMap() {
		return new MapBuilder<String, Object>();
	}
	
	public MapBuilder<K, V> with(K key, V value) {
		map.put(key, value);
		return this;
	}
	
	public Map<K, V> build() {
		return map;	}
}
