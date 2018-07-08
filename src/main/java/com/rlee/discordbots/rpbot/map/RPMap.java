package com.rlee.discordbots.rpbot.map;

import java.util.*;

import net.dv8tion.jda.core.entities.Message;

/**
 * A 2D visual text map
 * @author RLee
 *
 */
public class RPMap {
	@Deprecated
	private ArrayList<ArrayList<RPMapEntity<?>>> entityMap; //Visual map that contains data on region

	private Map<Character, RPMapEntity<?>> entityLookup; //Lookup for all unique entities that are generated
	private LinkedList<RPMapEntity<?>> entities; //List of all entities in this map

	private EntityCache entityCache;
	private boolean isCacheUpToDate;
	private RPMapEntity<?> nextEntity;

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
		entities = new LinkedList<>();
		entityMap = new ArrayList<>();
		entityLookup = new LinkedHashMap<>();

		entityCache = new EntityCache();
		isCacheUpToDate = false;

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
	public <E> void setAt(int rowIndex, int colIndex, char c, E e) {
		RPMapEntity<E> entity = new RPMapEntity<E>(c, e, new RPCoordinate(rowIndex, colIndex));

		//TODO Delete this test function and use a hashmap implementation
		ArrayList<RPMapEntity<?>> row = entityMap.get(rowIndex);
		row.set(colIndex, entity);
		//TODO Flag: Good code after this point, keep from here on

		entities.add(entity);
		checkAndModifyLookup(entity);
	}

	/**
	 * Check the EntityLookup and modify it based on the given entity.
	 * If the given entity's symbol is already listed, then delete the entry from the lookup.
	 * If the entity is not listed, then add the entry to the lookup.
	 * @param entity
	 */
	private void checkAndModifyLookup(RPMapEntity<?> entity) {
		char symbol = entity.getSymbol();
		if (entityLookup.containsKey(symbol)) {
			//TODO Decide on implementation. Present implementation: Remove all non-unique entries from lookup
//			entityLookup.remove(symbol);
		} else {
			entityLookup.put(symbol, entity);
		}
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

		checkBuildCache(new RPCoordinate(bottomEdge, leftEdge));

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

	private void checkBuildCache(RPCoordinate bottomLeftCorner) {
		RPCoordinate topRightCorner = calculateTopRightCorner(bottomLeftCorner, rowCount, colCount);

		//TODO Add more sophisticated checks for whether or not cache needs to be rebuilt or just updated
		if (!checkCacheUpToDate(bottomLeftCorner, topRightCorner)) {
			entityCache.setBottomLeftCorner(bottomLeftCorner);
			entityCache.setTopRightCorner(topRightCorner);

			entityCache.buildCache(entities);
			entityCache.sortCachedEntitiesByCoordinates(true);
			Iterator<RPMapEntity<?>> iterator = entityCache.iterator();
			if (iterator.hasNext()) {
				nextEntity = iterator.next();
			}
		}
	}

	private boolean checkCacheUpToDate(RPCoordinate bottomLeftCorner, RPCoordinate topRightCorner) {
		if (!isCacheUpToDate) {
			return isCacheUpToDate;
		}

		isCacheUpToDate = false;
		if (!entityCache.getBottomLeftCorner().equals(bottomLeftCorner)) {
			return isCacheUpToDate;
		}

		if (!entityCache.getTopRightCorner().equals(topRightCorner)) {
			return isCacheUpToDate;
		}

		isCacheUpToDate = true;
		return isCacheUpToDate;

	}

	/**
	 * Calculate the top right corner, inclusive
	 * @param bottomLeftCorner
	 * @param rowCount
	 * @param colCount
	 * @return
	 */
	private RPCoordinate calculateTopRightCorner(RPCoordinate bottomLeftCorner, int rowCount, int colCount) {
		return new RPCoordinate(bottomLeftCorner.getRow() + rowCount - 1, bottomLeftCorner.getCol() + colCount - 1);
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
		
		if (showBorder) {
			sb.append(colDividerChar);
		}
		
		ArrayList<RPMapEntity<?>> row = entityMap.get(rowIndex); //FIXME Delete this line
		RPCoordinate coord = new RPCoordinate(rowIndex, leftEdge);

		System.out.println("!!! RPCoord (r,c): " + coord.getRow() + ", " + coord.getCol());

		for (int i = leftEdge; i < leftEdge + minColCount; i++) {
//			sb.append(DELETE_ME_showEntityCell(row, i)); //FIXME Delete this
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

	@Deprecated
	private String DELETE_ME_showEntityCell(ArrayList<RPMapEntity<?>> row, int colIndex) {
		StringBuilder sb = new StringBuilder();
		
		RPMapEntity<?> entity = null;
		
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
		
		entity = row.get(colIndex);
		if (entity != null) {
			sb.append(entity.getSymbol());
		} else {
			sb.append(blankChar);
		}

		//Right padding
		for (int i = 0; i < blankCharCount; i++) {
			sb.append(blankChar);
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
		if (entityCache.existingIterator().hasNext()) {
			nextEntity = entityCache.existingIterator().next();
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

	/**
	 * Get whether or not the given coordinate is between the bottom left corner and the top right corner, inclusively
	 * @param coordinate
	 * @param bottomLeftCorner
	 * @param topRightCorner
	 * @return True if the coordinate is within, false if not.
	 */
	static boolean isInRegion(RPCoordinate coordinate, RPCoordinate bottomLeftCorner, RPCoordinate topRightCorner) {
		int left = bottomLeftCorner.getCol(), bottom = bottomLeftCorner.getRow();
		int right = topRightCorner.getCol(), top = topRightCorner.getRow();
		int x = coordinate.getCol(), y = coordinate.getRow(); //Entity coordinates

		return (x >= left && x <= right && y >= bottom && y <= top);
	}
}
