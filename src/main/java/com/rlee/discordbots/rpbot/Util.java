package com.rlee.discordbots.rpbot;

public class Util {
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
}
