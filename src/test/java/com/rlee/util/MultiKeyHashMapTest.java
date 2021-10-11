package com.rlee.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

class MultiKeyHashMapTest {

	@Test
	void put() {
		MultiKeyHashMap<String> map = new MultiKeyHashMap<>(Integer.class, Double.class, Character.class);

		map.put("first", 1, 1.1, 'a');
		map.put("second", 2, 2.2, 'b');

		assertEquals("first", map.get(1));
		assertEquals("first", map.get(1.1));
		assertEquals("first", map.get('a'));

		assertEquals("second", map.get(2));
		assertEquals("second", map.get(2.2));
		assertEquals("second", map.get('b'));
	}

	@Test
	void putOverlap() {
		MultiKeyHashMap<String> map = new MultiKeyHashMap<>(Integer.class, Double.class, Character.class);

		map.put("first", 1, 1.1, 'a');
		map.put("second", 2, 2.2, 'b');

		assertEquals("first", map.get(1));
		assertEquals("first", map.get(1.1));
		assertEquals("first", map.get('a'));

		map.put("second", 1, 2.2, 'b');

		assertEquals("second", map.get(1));
		assertEquals("second", map.get(2.2));
		assertEquals("second", map.get('b'));

		assertNull(map.get(1.1));
		assertNull(map.get('a'));
		assertNull(map.get(2));
	}

	@Test
	void get() {
	}

	@Test
	void remove() {
	}

	@Test
	void isEmpty() {
	}

	@Test
	void clear() {
	}
}