package com.rlee.util;

import java.util.Objects;

class CommonEntry <V> {
	Object[] keys;
	V value;

	CommonEntry(V value, Object... keys) {
		//Assumes that MultiKeyMap will validate inputs
		this.keys = keys;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CommonEntry)) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		return hashCode() == ((CommonEntry<V>) obj).hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, keys);
	}
}
