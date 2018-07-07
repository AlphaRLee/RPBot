package com.rlee.discordbots.rpbot.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import net.dv8tion.jda.core.entities.Message;

/**
 * A 2D visual text map
 * @author RLee
 *
 */
public class RPMap {
	private ArrayList<ArrayList<RPMapEntity<?>>> entityMap; //Visual map that contains data on region
	private Map<Character, RPMapEntity<?>> entityLookup; //Lookup for all entities that are generated
	
	private boolean showBorder = true;
	private static final char rowDividerChar = '\u2014'; //The \u2014 is unicode for long dash character
	private static final char colDividerChar = '|';
	private static final char cornerDividerChar = '+';
	private static final char blankChar = ' ';
	
	private int maxCharWidth = 3;
	private int maxCharHeight = 1;
	private int maxRowCount = 8;
	private int maxColCount = 8;
	
	private int rowCount;
	private int colCount;
	
	private String rowDivider = null; 
	private String blankInnerRow = null;
	
	private Message sourceMessage;
	
	public RPMap() {
		this(8, 8);
	}
	
	public RPMap(int rowCount, int colCount) {
		
		entityMap = new ArrayList<>();
		entityLookup = new LinkedHashMap<>();
		this.rowCount = rowCount;
		this.colCount = colCount;
		
		fillEntityMap();
	}
	
	@Deprecated
	private void fillEntityMap() {
		//TODO Get rid of this test method
		ArrayList<RPMapEntity<?>> row;
		while (rowCount >= entityMap.size()) {
			row = new ArrayList<RPMapEntity<?>>();
			while (colCount >= row.size()) {
				row.add(null);
			}
			entityMap.add(row);
		}
	}
	
	public void setSourceMessage(Message sourceMessage) {
		this.sourceMessage = sourceMessage;
	}
		
	@Deprecated
	public void setAt(int rowIndex, int colIndex, RPMapEntity entity) {
		//TODO Delete this test function and use a hashmap implementation
		ArrayList<RPMapEntity<?>> row = entityMap.get(rowIndex);
		row.set(colIndex, entity);
	}
	
	/**
	 * Get the entity map as a string starting with the top left corner at (0, 0)
	 * @return
	 */
	public String showMap() {
		return showMap(0, 0);
	}
	
	/**
	 * Get the entity map as a string starting with the given coordinates for the top-left corner
	 * @param leftEdge
	 * @param bottomEdge
	 */
	public String showMap(int leftEdge, int bottomEdge) {
		StringBuilder sb = new StringBuilder("```\n");
		
		blankInnerRow = buildInnerRow(blankChar, colDividerChar);
		if (showBorder) {
			if (rowDivider == null) {
				rowDivider = buildInnerRow(rowDividerChar, cornerDividerChar);
			}
			
			sb.append(rowDivider).append("\n");
		}
		
		int minRowCount = Math.min(rowCount, maxRowCount);
		//NOTE Reverse iteration used to flip Y axis
		for (int i = bottomEdge + minRowCount - 1; i >= bottomEdge; i--) {
			sb.append(showRow(i, leftEdge));
			
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
	private String showRow(int rowIndex, int leftEdge) {
		int minColCount = Math.min(colCount, maxColCount);
		
		int blankInnerRowCount = (int) maxCharHeight / 2; //Integer division
		StringBuilder sb = new StringBuilder();
		
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
		
		if (showBorder) {
			sb.append(colDividerChar);
		}
		
		ArrayList<RPMapEntity<?>> row = entityMap.get(rowIndex);
		for (int i = leftEdge; i < leftEdge + minColCount; i++) {
			sb.append(showEntityCell(row, i));
			
			if (showBorder) {
				sb.append(colDividerChar);
			}
		}
		sb.append("\n");
		
		for (int i = 0; i < blankInnerRowCount; i++) {
			sb.append(blankInnerRow).append("\n");
		}
		
		return sb.toString();
	}
	
	private String showEntityCell(ArrayList<RPMapEntity<?>> row, int colIndex) {	
		StringBuilder sb = new StringBuilder();
		
		RPMapEntity<?> entity = null;
		
		int blankCharCount = (int) maxCharWidth / 2; //Integer divison
		boolean isEven = maxCharWidth % 2 == 0;
		
		if (isEven) {
			blankCharCount--;
		}
		for (int k = 0; k < blankCharCount; k++) {
			sb.append(blankChar);
		}
		if (isEven) {
			blankCharCount++;
		}
		
		entity = row.get(colIndex);
		if (entity != null) {
			sb.append(entity.getSymbol());
		} else {
			sb.append(blankChar);
		}
		
		for (int k = 0; k < blankCharCount; k++) {
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
		int minRowCount = Math.min(rowCount, maxRowCount);
		
		StringBuilder innerRowBuilder = new StringBuilder();
		if (showBorder) {
			innerRowBuilder.append(endChar);
		}
		
		StringBuilder cellBuilder = new StringBuilder();
		for (int i = 0; i < maxCharWidth; i++) {
			cellBuilder.append(lengthChar);
		}
		if (showBorder) {
			cellBuilder.append(endChar);
		}
		String blankRowUnit = cellBuilder.toString();
		
		for (int i = 0; i < minRowCount; i++) {
			innerRowBuilder.append(blankRowUnit);
		}
		
		return innerRowBuilder.toString();
	}
}
