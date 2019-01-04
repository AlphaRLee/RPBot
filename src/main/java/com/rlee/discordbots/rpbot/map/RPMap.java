package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import net.dv8tion.jda.core.entities.Message;

import javax.naming.NameAlreadyBoundException;

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
	private MapPrinter mapPrinter;
	private MapLegendPrinter mapLegendPrinter;

	private Message sourceMessage;
	
	public RPMap(String name) {
		this.name = name;

		coordinateParser = new CoordinateParser();
		mapEntityRegistry = new MapEntityRegistry();
		mapPrinter = new MapPrinter();
		mapLegendPrinter = new MapLegendPrinter();
	}

	public String getName() {
		return name;
	}

	public void setSourceMessage(Message sourceMessage) {
		this.sourceMessage = sourceMessage;
	}

	public RPMapEntity<?> getEntity(String name) {
		return mapEntityRegistry.getEntity(name);
	}

	/**
	 * Add a new entity at the given coordinates.
	 * Always allows for auto-renaming
	 * @param rowIndex The row to set the entity at
	 * @param colIndex The column to set the entity at
	 * @param symbol A single char representation of the entity to insert
	 * @param entity The entity to insert in the map
	 * @param <E> The class of the entity
	 * @throws NameAlreadyBoundException Thrown if renaming is not allowed and the name is already taken.
	 */
	public <E> String addEntity(int rowIndex, int colIndex, char symbol, E entity) throws NameAlreadyBoundException {
		return addEntity(new RPCoordinate(rowIndex, colIndex), symbol, entity, true);
	}

	public <E> String addEntity(RPCoordinate coordinate, char symbol, E entity, boolean allowAutoRename) throws NameAlreadyBoundException {
		return mapEntityRegistry.addEntity(coordinate, new RPMapEntity<E>(symbol, entity, coordinate), allowAutoRename);
	}

	public boolean removeEntity(RPMapEntity<?> mapEntity) {
		return mapEntityRegistry.removeEntity(mapEntity);
	}

	public void setEntitySymbol(RPMapEntity<?> mapEntity, char symbol) {
		mapEntityRegistry.setEntitySymbol(mapEntity, symbol);
	}

	public void clearEntities() {
		mapEntityRegistry.clearEntities();
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

	public String showLegendBySymbols() {
		return mapLegendPrinter.showLegendBySymbols(name, mapEntityRegistry.getEntitiesBySymbol());
	}

	String showLegendByParsedArg(String arg) throws InvalidSearchTypeException {
		if (Util.isEmptyString(arg)) {
			return null;
		}

		String invalidSearchTypeExceptionMessage = "Selection could not be performed: " + arg + " is not recognized as a symbol or a coordinate.";

		// TODO Move this dirty code elsewhere inside MapEntityRegistry
		MapEntityRegistry.SearchType searchType = mapEntityRegistry.parseSearchType(arg);
		switch (searchType) {
			case SYMBOL:
				char symbol = arg.charAt(0);
				return mapLegendPrinter.getSymbolLegend(symbol, mapEntityRegistry.getEntityList(symbol));
			case COORDINATE:
				try {
					RPCoordinate coord = parseCoordinates(arg);
					return mapLegendPrinter.getCoordinateLegend(coord, mapEntityRegistry.getEntityList(coord));
				} catch (InvalidCoordinateException e) {
					throw new InvalidSearchTypeException(invalidSearchTypeExceptionMessage);
				}
			default:
				throw new InvalidSearchTypeException(invalidSearchTypeExceptionMessage);
		}
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

	void moveEntityToCoordinate(RPMapEntity<?> mapEntity, RPCoordinate destCoordinate) {
		mapEntityRegistry.moveEntityToCoordinate(mapEntity, destCoordinate);
	}

	RPMapEntity<?> parseMapEntity(String mapEntityArg) throws AmbiguousSelectionException {
		return mapEntityRegistry.parseEntity(mapEntityArg);
	}

	RPMapEntityList parseMapEntityList(String mapEntityArg) throws InvalidCoordinateException {
		return mapEntityRegistry.parseMapEntityList(mapEntityArg);
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
