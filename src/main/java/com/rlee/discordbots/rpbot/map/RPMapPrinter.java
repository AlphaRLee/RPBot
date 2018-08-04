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
	 * Get the entity map as a string starting with the given coordinates for the top-left corner
	 * @param leftEdge
	 * @param bottomEdge
	 */
	String showMap(int leftEdge, int bottomEdge, int rowCount, int colCount, ArrayList<RPMapEntity<?>> sortedCachedEntities) {
		iterator = sortedCachedEntities.listIterator();
		if (iterator.hasNext()) {
			nextEntity = iterator.next();
		}

		this.rowCount = rowCount;
		this.colCount = colCount;

		int minRowCount = Math.min(rowCount, maxRowCount); //The "actual" row count. Used for edge case where maxRowCount < rowCount
		maxRowIndexWidth = Math.max(getDigits(bottomEdge).length, getDigits(bottomEdge + minRowCount).length);
		maxColIndexWidth = Math.max(getDigits(leftEdge).length, getDigits(leftEdge + minRowCount).length);
		StringBuilder blankRowIndexBuilder = new StringBuilder();
		for (int i = 0; i < maxRowIndexWidth; i++) {
			blankRowIndexBuilder.append(' ');
		}
		blankRowIndex = blankRowIndexBuilder.toString();

		StringBuilder sb = new StringBuilder("```\n");

		blankInnerRow = buildInnerRow(blankChar, colDividerChar);
		if (showBorder) {
			if (rowDivider == null) {
				rowDivider = buildInnerRow(rowDividerChar, cornerDividerChar);
			}

			sb.append(rowDivider).append("\n");
		}

		//NOTE Reverse iteration used to flip Y axis
		for (int i = bottomEdge + minRowCount - 1; i >= bottomEdge; i--) {
			sb.append(showRow(i, leftEdge, bottomEdge));

			if (showBorder) {
				sb.append(rowDivider).append("\n");
			}
		}

		return sb.append("```").toString();
	}

	/**
	 * Print one row of the entity map.
	 * Always appends a newline character to the end
	 * @param rowIndex
	 * @param leftEdge
	 * @return
	 */
	private String showRow(int rowIndex, int leftEdge, int bottomEdge) {
		int minColCount = Math.min(colCount, maxColCount);

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
//		if (showBorder) {
			sb.append(colDividerChar);
//		}

		RPCoordinate coord = new RPCoordinate(rowIndex, leftEdge);
		for (int i = leftEdge; i < leftEdge + minColCount; i++) {
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
		int minColCount = Math.min(colCount, maxColCount);

		StringBuilder innerRowBuilder = new StringBuilder(blankRowIndex);
//		if (showBorder) {
			innerRowBuilder.append(endChar);
//		}

		StringBuilder cellBuilder = new StringBuilder();
		for (int i = 0; i < maxCharWidth; i++) {
			cellBuilder.append(lengthChar);
		}
		if (showBorder) {
			cellBuilder.append(endChar);
		}
		String blankRowUnit = cellBuilder.toString();

		for (int i = 0; i < minColCount; i++) {
			innerRowBuilder.append(blankRowUnit);
		}

		return innerRowBuilder.toString();
	}

	private String showRowIndex(int index) {
		int[] digits = getPaddedDigits(index, maxRowIndexWidth);
		StringBuilder sb = new StringBuilder();
		int i = 0;

		//Edge case: Explicitly search for index == 0 for efficiency
		if (index == 0) {
			while (i < digits.length - 1) {
				sb.append(' ');
				i++;
			}
			return sb.append('0').toString();
		}

		while (digits[i] == 0) {
			sb.append(' ');
			i++;
		}

		//Edge case: The only -1 instance represents a - char
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

	private int[] getPaddedDigits(int index, int maxIndexWidth) {
		//TODO Implement support for negative indices
		int[] indexDigits = getDigits(index);

		int[] paddedDigits = new int[maxIndexWidth];
		int i = 0;
		while (i < maxIndexWidth - indexDigits.length) {
			paddedDigits[i] = 0;
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
