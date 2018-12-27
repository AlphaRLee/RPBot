package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;

import java.util.Arrays;

class CoordinateParser {
	private boolean startsNegative = false;

	/**
	 * Parse a string representing coordinates into an RPCoordinate system
	 * @param coordinate A string representation of coordinates. Valid examples include: A1, a1, AB12, A.10, -A1, A-1, -A-1, 1A, 12AB, -10A., -102-A.B
	 * @return An RPCoordinate representing the same location as the given coordinate.
	 * @throws NullPointerException Thrown if input is null or empty.
	 * @throws InvalidCoordinateException Thrown if the coordinate cannot be parsed.
	 */
	RPCoordinate parseCoordinates(String coordinate) throws NullPointerException, InvalidCoordinateException {
		if (Util.isEmptyString(coordinate)) {
			throw new NullPointerException("Coordinate cannot be null or empty.");
		}

		char[] coordChars = coordinate.trim().toUpperCase().toCharArray(); //Clean up input
		byte charType = getFirstCoordCharType(coordChars);

		byte firstCharType = charType;
		int secondDimIndex = 1; //Start of second dimension
		boolean secondDimNotFound = true;
		while ((secondDimNotFound = charType == firstCharType) && secondDimIndex < coordChars.length) {
			charType = getCoordCharType(coordChars[secondDimIndex++]);
		}

		//Throw exception because only 1 dimension provided
		if (secondDimNotFound) {
			throw buildOneDimensionFoundException(firstCharType);
		}

		secondDimIndex--; //Decrement again after incrementing by 1 too many earlier

		//Get char arrays representing the row and column
		int row = 0, col = 0;
		char[] rowChars = null, colChars = null;
		switch (firstCharType) {
			case Character.UPPERCASE_LETTER:
				colChars = Arrays.copyOf(coordChars, secondDimIndex);
				rowChars = Arrays.copyOfRange(coordChars, secondDimIndex, coordChars.length);
				break;
			case Character.DECIMAL_DIGIT_NUMBER:
				rowChars = Arrays.copyOf(coordChars, secondDimIndex);
				colChars = Arrays.copyOfRange(coordChars, secondDimIndex, coordChars.length);
				break;
		}

		return new RPCoordinate(parseRow(rowChars), parseCol(colChars));
	}

	private byte getCoordCharType(char coordinateChar) throws InvalidCoordinateException {
		if (coordinateChar == '-') {
			return Character.DASH_PUNCTUATION; //Negative char, but no indication on rows or cols
		} else if (Character.isAlphabetic(coordinateChar) || coordinateChar == RPMap.ALPHA_ZERO_CHAR) {
			return Character.UPPERCASE_LETTER;
		} else if (Character.isDigit(coordinateChar)) {
			return Character.DECIMAL_DIGIT_NUMBER;
		} else {
			throw new InvalidCoordinateException("Cannot read character **" + coordinateChar + "**.");
		}
	}

	private byte getFirstCoordCharType(char[] coordChars) throws InvalidCoordinateException {
		byte charType = getCoordCharType(coordChars[0]);
		startsNegative = (charType == Character.DASH_PUNCTUATION);

		if (startsNegative) {
			return getCoordCharTypeAfterDash(coordChars);
		}

		return charType;
	}

	private byte getCoordCharTypeAfterDash(char[] coordChars) throws InvalidCoordinateException {
		if (coordChars.length < 2) {
			//Following char after dash is missing
			throw new InvalidCoordinateException("Missing coordinates after '-' character.");
		}

		byte charType = getCoordCharType(coordChars[1]);
		if (charType == Character.DASH_PUNCTUATION) {
			throw new InvalidCoordinateException("Too many '-' characters.");
		}

		return charType;
	}

	private InvalidCoordinateException buildOneDimensionFoundException(byte coordCharType) {
		String missingDimension = "[INTERNAL ERROR, CONTACT DEVELOPER. SORRY HAVE A COOKIE :cookie:]";
		switch (coordCharType) {
			case Character.UPPERCASE_LETTER:
				missingDimension = "row (Y coordinate)";
				break;
			case Character.DECIMAL_DIGIT_NUMBER:
				missingDimension = "column (X coordinate)";
				break;
		}
		return new InvalidCoordinateException("No " + missingDimension + " has been specified.");
	}

	private int parseRow(char[] rowChars) throws InvalidCoordinateException {
		String rowStr = new String(rowChars);
		int row;

		try {
			row = Integer.parseInt(rowStr, RPMap.ROW_RADIX);
		} catch (NumberFormatException e) {
			throw new InvalidCoordinateException("Row (Y coordinate) cannot be read from **" + rowStr + "**.");
		}

		return row;
	}

	private int parseCol(char[] colChars) throws InvalidCoordinateException {
		InvalidCoordinateException e = new InvalidCoordinateException("Column (X coordinate) cannot be read from **" + new String(colChars) + "**.");

		boolean isNegative = false;
		int col = 0;
		char c;
		for (int i = 0; i < colChars.length; i++) {
			col *= RPMap.COL_RADIX;

			c = colChars[i];
			if (c == '-') {
				if (isNegative) {
					throw e;
				} else {
					isNegative = true;
					continue;
				}
			} else if (Character.isAlphabetic(c)) {
				col += c - 'A' + 1;
			} else if (c == RPMap.ALPHA_ZERO_CHAR) {
				//Add 0 to col (i.e. do nothing
			} else {
				throw e; //Invalid char
			}
		}

		return col * (isNegative ? -1 : 1);
	}

	static String rpCoordinateToString(RPCoordinate coord) {
		StringBuilder sb = new StringBuilder();

		int colNumber = coord.getCol();
		if (colNumber == 0) {
			sb.append(RPMap.ALPHA_ZERO_CHAR);
		} else {
			if (colNumber < 0) {
				sb.append('-');
				colNumber *= -1;
			}

			int colDigit;
			String colString = "";
			while (colNumber > 0) {
				colDigit = colNumber % RPMap.COL_RADIX;
				if (colDigit == 0) {
					colString = RPMap.ALPHA_ZERO_CHAR + colString;
				} else {
					colString = (char) ('A' + colNumber - 1) + colString;
				}
				colNumber /= RPMap.COL_RADIX;
			}
			sb.append(colString);
		}

		sb.append(coord.getRow());
		return sb.toString();
	}
}
