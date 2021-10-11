
package com.rlee.util;

import java.util.Map;

public abstract class MultiKeyMap<V> {
	protected Class<?>[] keyClasses;
	protected Map<Object, CommonEntry<V>>[] maps;

	protected MultiKeyMap(Class<?>... keyClasses) throws IllegalArgumentException {
		if (keyClasses.length < 1) {
			throw new IllegalArgumentException("At least one key must be added");
		}

		this.keyClasses = keyClasses;
		implementMaps();
	}

	protected abstract void implementMaps();

	public V get(Object key) {
		Map<Object, CommonEntry<V>> map = getMapFromKeyType(key);
		if (map == null) {
			return null;
		}

		return map.get(key) != null ? map.get(key).value : null;
	}

	public void put(V value, Object... keys) throws IllegalArgumentException, ClassCastException {
		validateInputKeys(keys);
		CommonEntry<V> commonEntry = new CommonEntry<>(value, keys);

		//Remove other entries that may have associated with this common entry
		removeRelatedCommonEntries(commonEntry);

		for (int i = 0; i < keys.length; i++) {
			maps[i].put(keys[i], commonEntry);
		}
	}

	public void remove(Object key) {
		Map<Object, CommonEntry<V>> map = getMapFromKeyType(key);
		if (map == null) {
			return;
		}

		CommonEntry<V> commonEntry = map.remove(key);
		if (commonEntry == null) {
			return;
		}
		removeCommonEntry(commonEntry);
	}

	public boolean isEmpty() {
		return maps[0].isEmpty();
	}

	public void clear() {
		for (Map<Object, CommonEntry<V>> map : maps) {
			map.clear();
		}
	}

	private Map<Object, CommonEntry<V>> getMapFromKeyType(Object key) {
		for (int i = 0; i < keyClasses.length; i++) {
			if (keyClasses[i].isInstance(key)) {
				return maps[i];
			}
		}

		return null;
	}

	private void validateInputKeys(Object... keys) throws ClassCastException {
		if (keys.length != keyClasses.length) {
			throw new IllegalArgumentException("Expected " + keyClasses.length + " keys, but received " + keys.length + " keys.");
		}

		for (int i = 0; i < keys.length; i++) {
			if (!keyClasses[i].isInstance(keys[i])) {
				throw new ClassCastException("Expected key for index " + i + " to be " + keyClasses[i].getName() + ", instead got " + keys[i].getClass().getName() + ".");
			}
		}
	}

	private void removeRelatedCommonEntries(CommonEntry<V> commonEntry) {
		if (commonEntry == null) {
			return;
		}

		for (int i = 0; i < commonEntry.keys.length; i++) {
			removeCommonEntry(maps[i].remove(commonEntry.keys[i]));
		}
	}

	private void removeCommonEntry(CommonEntry<V> commonEntry) {
		if (commonEntry == null) {
			return;
		}

		for (int i = 0; i < commonEntry.keys.length; i++) {
			maps[i].remove(commonEntry.keys[i]);
		}
	}
}
