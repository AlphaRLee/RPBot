package com.rlee.discordbots.rpbot.map;

import java.util.*;

import net.dv8tion.jda.core.entities.Message;

/**
 * A 2D visual text map
 * @author RLee
 *
 */
public class RPMap {
	static final int ROW_RADIX = 10;
	static final int COL_RADIX = 27; //Column uses Base 27 system: .=0 A=1 B=2 ... y=25 Z=26 A.=27 AA=28
	static final char ALPHA_ZERO_CHAR = '.'; //Does that look weird? Yes, that looks very weird. Base 27 numbering system!

	private String name;

	private LinkedList<RPMapEntity<?>> entities; //List of all entities in this map
	private Map<Character, RPMapEntity<?>> entityLookup; //Lookup for all unique entities that are generated

	private EntityCache entityCache;
	private boolean isCacheUpToDate;

	private RPMapPrinter mapPrinter;
	private Message sourceMessage;
	
	public RPMap(String name) {
		this.name = name;
		entities = new LinkedList<>();
		entityLookup = new LinkedHashMap<>();

		entityCache = new EntityCache();
		isCacheUpToDate = false;

		mapPrinter = new RPMapPrinter();
	}

	public String getName() {
		return name;
	}

	public void setSourceMessage(Message sourceMessage) {
		this.sourceMessage = sourceMessage;
	}

	public <E> void setAt(int rowIndex, int colIndex, char c, E e) {
		RPMapEntity<E> entity = new RPMapEntity<E>(c, e, new RPCoordinate(rowIndex, colIndex));

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
		return showMap(0, 0, mapPrinter.getRowCount(), mapPrinter.getColCount());
	}

	public String showMap(int bottomRow, int leftCol) {
		return showMap(bottomRow, leftCol, mapPrinter.getRowCount(), mapPrinter.getColCount());
	}

	public String showMap(int bottomRow, int leftCol, int rowCount, int colCount) {
		checkBuildCache(new RPCoordinate(bottomRow, leftCol), rowCount, colCount);
		return mapPrinter.showMap(bottomRow, leftCol, rowCount, colCount, entityCache.getCachedEntities());
	}

	private void checkBuildCache(RPCoordinate bottomLeftCorner, int rowCount, int colCount) {
		RPCoordinate topRightCorner = calculateTopRightCorner(bottomLeftCorner, rowCount, colCount);

		//TODO Add more sophisticated checks for whether or not cache needs to be rebuilt or just updated
		if (!checkCacheUpToDate(bottomLeftCorner, topRightCorner)) {
			entityCache.setBottomLeftCorner(bottomLeftCorner);
			entityCache.setTopRightCorner(topRightCorner);

			entityCache.buildCache(entities);
			entityCache.sortCachedEntitiesByCoordinates(true);
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
