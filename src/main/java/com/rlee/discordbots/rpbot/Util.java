package com.rlee.discordbots.rpbot;

import java.util.Collection;

public class Util {
	public static int boundRange(int value, int min, int max) {
		return Math.min(Math.max(value, min), max);
	}

	public static String replaceWhitespaces(String string) {
		return replaceWhitespaces(string, false);
	}

	public static String replaceWhitespaces(String string, boolean toLowerCase) {
		String output = string.trim().replace(' ', '-');

		if (toLowerCase) {
			output = output.toLowerCase();
		}

		return output;
	}

	public static boolean isEmptyString(String string) {
		return string == null || string.isEmpty();
	}

	public static boolean isEmptyCollection(Collection<?> collection) {return collection == null || collection.isEmpty(); }
}
