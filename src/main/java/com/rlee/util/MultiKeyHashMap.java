package com.rlee.util;

import java.util.HashMap;
import java.util.Map;

public class MultiKeyHashMap<V> extends MultiKeyMap<V> {

	public MultiKeyHashMap(Class<?>... keyClasses) throws IllegalArgumentException {
		super(keyClasses);
	}

	@Override
	protected void implementMaps() {
		maps = new HashMap[keyClasses.length];
		for (int i = 0; i < keyClasses.length; i++) {
			maps[i] = new HashMap<>();
		}
	}
}
