package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
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
	static final char MULTIPLE_ENTITIES_CHAR = '+';

	private String name;

	private static CoordinateParser coordinateParser;
	private MapEntityRegistry mapEntityRegistry;
	private RPMapPrinter mapPrinter;

	private Message sourceMessage;
	
	public RPMap(String name) {
		this.name = name;

		coordinateParser = new CoordinateParser();
		mapEntityRegistry = new MapEntityRegistry();
		mapPrinter = new RPMapPrinter();
	}

	public String getName() {
		return name;
	}

	public void setSourceMessage(Message sourceMessage) {
		this.sourceMessage = sourceMessage;
	}

	public Object getEntity(int row, int col) {
		return mapEntityRegistry.getEntity(new RPCoordinate(row, col));
	}

	public Object getEntity(RPCoordinate coordinate) {
		return mapEntityRegistry.getEntity(coordinate);
	}

	/**
	 * Get the unique entity represented by the given character
	 * @param c The character representation of the entity
	 * @return The entity, or null if zero or more than 1 instance found.
	 */
	public Object getEntity(Character c) {
		return mapEntityRegistry.getEntity(c);
	}

	/**
	 * Set a new entity at the given coordinates. Will overwrite any existing entity at the coordinates
	 * @param rowIndex The row to set the entity at
	 * @param colIndex The column to set the entity at
	 * @param symbol A single char representation of the entity to insert
	 * @param entity The entity to insert in the map
	 * @param <E> The class of the entity
	 */
	public <E> void setEntity(int rowIndex, int colIndex, char symbol, E entity) {
		mapEntityRegistry.setEntity(new RPCoordinate(rowIndex, colIndex), symbol, entity);
	}

	public <E> void setEntity(RPCoordinate coordinate, char symbol, E entity) {
		mapEntityRegistry.setEntity(coordinate, symbol, entity);
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
		return showMap(new RPCoordinate(bottomRow, leftCol), rowCount, colCount);
	}

	public String showMap(RPCoordinate bottomLeftCorner, int rowCount, int colCount) {
		return mapPrinter.showMap(bottomLeftCorner, rowCount, colCount, mapPrinter.buildPrintableEntities(bottomLeftCorner, rowCount, colCount, mapEntityRegistry.getEntitiesByCoordinate()));
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

//	void moveEntityToCoordinate(String entityArg, String destCoordinateArg) throws InvalidCoordinateException {
//		RPCoordinate destCoordinate = parseCoordinates(destCoordinateArg);
//		RPMapEntity<?> mapEntity = mapEntityRegistry.parseEntity(entityArg);
//		mapEntityRegistry.moveEntityToCoordinate(mapEntity, destCoordinate);
//	}

	void moveEntityToCoordinate(RPMapEntity<?> mapEntity, RPCoordinate destCoordinate) {
		mapEntityRegistry.moveEntityToCoordinate(mapEntity, destCoordinate);
	}

	RPMapEntity<?> parseMapEntity(String mapEntityArg) throws AmbiguousSelectionException {
		return mapEntityRegistry.parseEntity(mapEntityArg);
	}

	static RPCoordinate parseCoordinates(String coordinateArg) throws InvalidCoordinateException {
		return coordinateParser.parseCoordinates(coordinateArg);
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
