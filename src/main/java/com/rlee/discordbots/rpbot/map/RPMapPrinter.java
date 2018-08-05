package com.rlee.discordbots.rpbot.map;

import java.util.ArrayList;
import java.util.ListIterator;

class RPMapPrinter {
	private ListIterator<RPMapEntity<?>> iterator;
	private RPMapEntity<?> nextEntity;

	private boolean showBorder = true;
	private static final char rowDividerChar = '\u2014'; //The \u2014 is unicode for long dash character
	private static final char colDividerChar = '|';
	private static final char cornerDividerChar = '+';
	private static final char blankChar = ' ';

	private static final int BLANK_DIGIT = -999;

	private int maxCharWidth = 3;
	private int maxCharHeight = 1;
	private int maxRowCount = 10;
	private int maxColCount = 10;

	private int rowCount;
	private int colCount;

	private int maxRowIndexWidth;
	private int maxColIndexWidth;

	private String rowDivider = null;
	private String blankInnerRow = null;
	private String blankRowIndex;

	RPMapPrinter() {
		this(8, 8);
	}

	RPMapPrinter(int rowCount, int colCount) {
		this.rowCount = rowCount;
		this.colCount = colCount;
	}

	int getRowCount() {
		return rowCount;
	}

	void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	int getColCount() {
		return colCount;
	}

	void setColCount(int colCount) {
		this.colCount = colCount;
	}

	/**
	 * Get the entity map as a string starting with the given coordinates for the bottom-left corner
	 * @param leftCol
	 * @param bottomRow
	 */
	String showMap(int bottomRow, int leftCol, int rowCount, int colCount, ArrayList<RPMapEntity<?>> sortedCachedEntities) {
		iterator = sortedCachedEntities.listIterator();
		if (iterator.hasNext()) {
			nextEntity = iterator.next();
		}

		this.rowCount = rowCount;
		this.colCount = colCount;

		int minRowCount = Math.min(rowCount, maxRowCount); //The "actual" row count. Used for edge case where maxRowCount < rowCount
		maxRowIndexWidth = Math.max(getDigits(bottomRow).length, getDigits(bottomRow + minRowCount).length);
		maxColIndexWidth = Math.max(getDigits(leftCol, RPMap.ALPHA_RADIX).length, getDigits(leftCol + minRowCount, RPMap.ALPHA_RADIX).length);
		StringBuilder blankRowIndexBuilder = new StringBuilder();
		for (int i = 0; i < maxRowIndexWidth; i++) {
			blankRowIndexBuilder.append(' ');
		}
		blankRowIndex = blankRowIndexBuilder.toString();

		StringBuilder sb = new StringBuilder("```\n");

		blankInnerRow = buildInnerRow(blankChar, colDividerChar);
		if (rowDivider == null) {
			rowDivider = buildInnerRow(rowDividerChar, cornerDividerChar);
		}

		//NOTE Reverse iteration used to flip Y axis
		for (int i = bottomRow + minRowCount - 1; i >= bottomRow; i--) {
			if (showBorder) {
				sb.append(rowDivider).append("\n");
			}

			sb.append(showRow(i, leftCol));
		}

		sb.append(rowDivider).append("\n");
		sb.append(showColIndex(leftCol));
		return sb.append("```").toString();
	}

	/**
	 * Print one row of the entity map.
	 * Always appends a newline character to the end
	 * @param rowIndex
	 * @param leftCol
	 * @return
	 */
	private String showRow(int rowIndex, int leftCol) {
		int netColCount = Math.min(colCount, maxColCount);

		int blankInnerRowCount = (int) maxCharHeight / 2; //Integer division
		StringBuilder sb = new StringBuilder();

		//Upper inner-row padding
		boolean isEven = maxCharHeight % 2 == 0;
		if (isEven) {
			blankInnerRowCount--;
		}
		for (int i = 0; i < blankInnerRowCount; i++) {
			sb.append(blankInnerRow).append("\n");
		}
		if (isEven) {
			blankInnerRowCount++;
		}

		sb.append(showRowIndex(rowIndex));
		sb.append(colDividerChar);

		RPCoordinate coord = new RPCoordinate(rowIndex, leftCol);
		for (int i = leftCol; i < leftCol + netColCount; i++) {
			coord.setCol(i);
			sb.append(showEntityCell(coord));

			if (showBorder) {
				sb.append(colDividerChar);
			}
		}
		sb.append("\n");

		//Lower inner-row padding
		for (int i = 0; i < blankInnerRowCount; i++) {
			sb.append(blankInnerRow).append("\n");
		}

		return sb.toString();
	}

	private String showEntityCell(RPCoordinate coord) {
		if (nextEntity == null || !nextEntity.getCoordinate().equals(coord)) {
			return showBlankEntityCell();
		}

		StringBuilder sb = new StringBuilder();
		int blankCharCount = (int) maxCharWidth / 2; //Integer divison
		boolean isEven = maxCharWidth % 2 == 0;

		//Left padding
		if (isEven) {
			blankCharCount--;
		}
		for (int i = 0; i < blankCharCount; i++) {
			sb.append(blankChar);
		}
		if (isEven) {
			blankCharCount++;
		}

		sb.append(nextEntity.getSymbol());

		//Right padding
		for (int i = 0; i < blankCharCount; i++) {
			sb.append(blankChar);
		}

		//Assign next entity
		if (iterator.hasNext()) {
			nextEntity = iterator.next();
		} else {
			nextEntity = null;
		}

		return sb.toString();
	}

	private String showBlankEntityCell() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < maxCharWidth; i++) {
			sb.append(blankChar);
		}
		return sb.toString();
	}

	/**
	 * Build a single line of a row
	 * <p>E.g. +---+---+---+</p>
	 * @param lengthChar The character to put in between intersections
	 * @param endChar The charcter to put on the end of the row
	 * @return The string representation of a row
	 */
	private String buildInnerRow(char lengthChar, char endChar) {
		int netColCount = Math.min(colCount, maxColCount);

		StringBuilder innerRowBuilder = new StringBuilder(blankRowIndex);
		innerRowBuilder.append(endChar);

		StringBuilder cellBuilder = new StringBuilder();
		for (int i = 0; i < maxCharWidth; i++) {
			cellBuilder.append(lengthChar);
		}
		if (showBorder) {
			cellBuilder.append(endChar);
		}
		String blankRowUnit = cellBuilder.toString();

		for (int i = 0; i < netColCount; i++) {
			innerRowBuilder.append(blankRowUnit);
		}

		return innerRowBuilder.toString();
	}

	private String showRowIndex(int index) {
		int[] digits = getPaddedDigits(index, maxRowIndexWidth);
		StringBuilder sb = new StringBuilder();
		int i = 0;

		//Edge case: Explicitly search for index == 0 for efficiency
//		if (index == 0) {
//			while (i < digits.length - 1) {
//				sb.append(' ');
//				i++;
//			}
//			return sb.append('0').toString();
//		}

		while (digits[i] == BLANK_DIGIT) {
			sb.append(blankChar);
			i++;
		}

		//Test for the only -1 instance represents a - char
		if (digits[i] < 0) {
			sb.append('-');
			i++;
		}

		while (i < digits.length) {
			sb.append(Integer.toString(digits[i]));
			i++;
		}

		return sb.toString();
	}

	private String showColIndex(int leftCol) {
		int netColCount = Math.min(colCount, maxColCount);
		int[][] numericColIndices = new int[netColCount][maxColIndexWidth];

		for (int i = 0, j = leftCol; i < netColCount; i++, j++) {
			numericColIndices[i] = getPaddedDigits(j, maxColIndexWidth, RPMap.ALPHA_RADIX);

//			//Edge case
//			if (j % RPMap.ALPHA_RADIX == 0) {
//				numericColIndices[i][maxColIndexWidth - 1] = BLANK_DIGIT;
//			}
		}

		StringBuilder sb = new StringBuilder();
		//NOTE: Performing "transposed" 2d array iteration
		for (int i = 0; i < maxColIndexWidth; i++) {
			sb.append(blankRowIndex).append(blankChar);
			for (int j = 0; j < netColCount; j++) {
				sb.append(showColIndexCell(numericColIndices[j][i]));

				if (showBorder) {
					sb.append(blankChar);
				}
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	private String showColIndexCell(int indexDigit) {
		StringBuilder sb = new StringBuilder();
		int blankCharCount = (int) maxCharWidth / 2; //Integer divison
		boolean isEven = maxCharWidth % 2 == 0;

		//Left padding
		if (isEven) {
			blankCharCount--;
		}
		for (int i = 0; i < blankCharCount; i++) {
			sb.append(blankChar);
		}
		if (isEven) {
			blankCharCount++;
		}

		if (indexDigit == BLANK_DIGIT) {
			sb.append(blankChar);
		} else if (indexDigit == 0) {
			sb.append(RPMap.ALPHA_ZERO_CHAR);
		} else if (indexDigit < 0) {
			sb.append('-'); //Negative sign symbol
		} else {
			sb.append((char) ('A' + indexDigit - 1));
		}

		//Right padding
		for (int i = 0; i < blankCharCount; i++) {
			sb.append(blankChar);
		}

		return sb.toString();
	}

	private int[] getPaddedDigits(int index, int maxIndexWidth) {
		return getPaddedDigits(index, maxIndexWidth, 10);
	}

	private int[] getPaddedDigits(int index, int maxIndexWidth, int radix) {
		//TODO Implement support for negative indices
		int[] indexDigits = getDigits(index, radix);

		int[] paddedDigits = new int[maxIndexWidth];
		int i = 0;
		while (i < maxIndexWidth - indexDigits.length) {
			paddedDigits[i] = BLANK_DIGIT;
			i++;
		}

		//NOTE: i has not been reset to 0
		int j = 0; //Counter for digits
		while (j < indexDigits.length ) {
			paddedDigits[i] = indexDigits[j];
			i++;
			j++;
		}

		return paddedDigits;
	}

	private int[] getDigits(int n) {
		return getDigits(n, 10);
	}

	private int[] getDigits(int n, int radix) {
		//Edge case:
		if (n == 0) {
			return new int[] {0}; //Return an array with just the number 0 in it
		}

		int digitCount = 0;
		int i = Math.abs(n);
		while (i > 0) {
			i /= radix;
			digitCount++;
		}

		boolean negativeSign = (n < 0);
		int[] digits = new int[digitCount + (negativeSign ? 1 : 0)];
		i = Math.abs(n);
		int j = digitCount - 1 + (negativeSign ? 1 : 0) ;
		while (i > 0) {
			digits[j] = i % radix;
			i /= radix;
			j--;
		}

		if (negativeSign) {
			digits[0] = -1;
		}

		return digits;
	}
}
