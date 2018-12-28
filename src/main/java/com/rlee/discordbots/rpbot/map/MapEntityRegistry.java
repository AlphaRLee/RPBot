package com.rlee.discordbots.rpbot.map; // TODO decide on nesting inside map or registry section (preference to map)

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import com.rlee.discordbots.rpbot.regitstry.Registry;

import java.util.*;

class MapEntityRegistry implements Registry {
	enum SearchType {
		SYMBOL,
		COORDINATE,
		ENTITY
	}

	private TreeMap<RPCoordinate, RPMapEntityList> entitiesByCoordinate;
	private Map<Character, RPMapEntity<?>> entitiesBySymbol; //Lookup for all unique entities that are generated
	private Map<String, RPMapEntity<?>> entitiesByName; // Entities by full name

	MapEntityRegistry() {
		entitiesByCoordinate = new TreeMap<>();
		entitiesBySymbol = new LinkedHashMap<>();
		entitiesByName = new HashMap<>();
	}

	@Override
	public boolean containsName(String name) {
		// TODO Decide on implementation
		return false;
	}

	/**
	 * Get the first entity listed at the given coordinate
	 * @param coordinate The coordinate to get the entity at
	 * @return The first entity, or null if no entity exists at the coordinate
	 */
	Object getEntity(RPCoordinate coordinate) {
		RPMapEntityList entityList = entitiesByCoordinate.get(coordinate);
		if (Util.isEmptyCollection(entityList)) {
			return null;
		}

		return entityList.get(0).getEntity();
	}

	/**
	 * Get the list of entities at the given coordinate
	 * @param coordinate
	 * @return
	 */
	RPMapEntityList getEntityList(RPCoordinate coordinate) {
		return entitiesByCoordinate.get(coordinate);
	}

	/**
	 * Get the unique entity represented by the given character
	 * @param symbol The character representation of the entity
	 * @return The entity. Always return first inserted in case of symbol conflict
	 */
	Object getEntity(Character symbol) {
		return entitiesBySymbol.get(symbol).getEntity();
	}

	/**
	 * Get the unique entity represented by the given name
	 * @param name The name of the entity. Case insensitive
	 * @return The entity. Always return first inserted in case of name conflict
	 */
	Object getEntity(String name) {
		return entitiesByName.get(name.toLowerCase()).getEntity();
	}

	/**
	 * Set a new entity at the given coordinates. Will overwrite any existing entity at the coordinates
	 * @param rowIndex The row to set the entity at
	 * @param colIndex The column to set the entity at
	 * @param symbol A single char representation of the entity to insert
	 * @param entity The entity to insert in the map
	 * @param <E> The class of the entity
	 */
	<E> void setEntity(int rowIndex, int colIndex, char symbol, E entity) {
		setEntity(new RPCoordinate(rowIndex, colIndex), symbol, entity);
	}

	<E> void setEntity(RPCoordinate coordinate, char symbol, E entity) {
		setEntity(coordinate, new RPMapEntity<E>(symbol, entity, coordinate));
	}

	void setEntity(RPCoordinate coordinate, RPMapEntity<?> mapEntity) {
		RPMapEntityList entityList = entitiesByCoordinate.get(coordinate);
		if (entityList == null) {
			entityList =  new RPMapEntityList();
			entitiesByCoordinate.put(coordinate, entityList);
		}

		entityList.addFirst(mapEntity);
		checkAndModifyLookup(mapEntity);
	}

	TreeMap<RPCoordinate, RPMapEntityList> getEntitiesByCoordinate() {
		return entitiesByCoordinate;
	}

	/**
	 * Check the EntityLookup and modify it based on the given entity.
	 * If the given entity's symbol is already listed, then do nothing.
	 * If the entity is not listed, then add the entry to the lookup.
	 * @param entity
	 */
	private void checkAndModifyLookup(RPMapEntity<?> entity) {
		char symbol = entity.getSymbol();
		if (entitiesBySymbol.containsKey(symbol)) {
			//TODO Change to store redundant entries in arraylist. Present implementation: Do nothing
		} else {
			entitiesBySymbol.put(symbol, entity);
		}

		// Repeat process for entities by name
		String name = entity.getName().toLowerCase();
		if (entitiesByName.containsKey(name)) {
			//TODO Change to store redundant entries in arraylist. Present implementation: Do nothing
		} else {
			entitiesByName.put(name, entity);
		}
	}

	private RPMapEntity<?> getOnlyEntityAtCoordinate(String coordinateArg) throws AmbiguousSelectionException, InvalidCoordinateException {
		RPMapEntity<?> entity;

		RPMapEntityList entityList = entitiesByCoordinate.get(new CoordinateParser().parseCoordinates(coordinateArg));
		if (Util.isEmptyCollection(entityList)) {
			// TODO what happens if the user selects a valid but empty coordinate? Presently returns null (and null is appropriately checked later)
			// Should it instead attempt to parse as entity name?
			return null;
		}

		if (entityList.size() > 1) {
			throw new AmbiguousSelectionException("Selection ambiguous: multiple entities have the same coordinate.");
		}

		return entityList.get(0);
	}

	/**
	 * Attempt to parse an entity
	 * @param mapEntityArg
	 * @return
	 */
	RPMapEntity<?> parseEntity(String mapEntityArg) throws AmbiguousSelectionException {
		if (Util.isEmptyString(mapEntityArg)) {
			return null;
		}

		return parseEntityByType(mapEntityArg, parseSearchType(mapEntityArg));
	}

	/**
	 * Attempt to parse the search type of the arg based on the following rules:
	 * <br/>If the arg is just 1 character long, it is assumed to be a symbol
	 * <br/>If the arg is 2 characters long, it is assumed to be a coordinate
	 * <br/>All other cases are assumed to be an entity
	 * @param arg The string to parse the search type of
	 * @return The assumed search type
	 */
	private SearchType parseSearchType(String arg) {
		int offset = 0;

		// Testing for negative coordinates
		// Also test that input has enough chars to be a coordinate (e.g. -A7 valid, -A invalid)
		if (arg.charAt(0) == '-' && arg.length() > 2) {
			offset += 1;
		}
		// Test for 2nd negative char and long enough to be a coordinate (e.g. A-7, -A-7 valid, A-, -A- invalid)
		if (arg.substring(1 + offset).length() > 1 && arg.charAt(1 + offset) == '-') {
			offset += 1;
		}

		switch (arg.length() - offset) {
			case 1:
				return SearchType.SYMBOL;
			case 2:
				return SearchType.COORDINATE;
			default:
				return SearchType.ENTITY;
		}
	}

	RPMapEntity<?> parseEntityByType(String arg, SearchType searchType) throws NullPointerException, AmbiguousSelectionException {
		if (searchType == null) {
			throw new NullPointerException("SearchType must be specified.");
		}

		if (Util.isEmptyString(arg)) {
			return null;
		}


		RPMapEntity<?> entity = null;
		switch (searchType) {
			case SYMBOL:
				entity = entitiesBySymbol.get(arg.charAt(0));
				break;
			case COORDINATE:
				try {
					entity = getOnlyEntityAtCoordinate(arg);
				} catch (InvalidCoordinateException e) {
					entity = entitiesByName.get(arg.toLowerCase()); // Assume the arg is an entity instead of a coordinate
				}
				break;
			case ENTITY:
				entity = entitiesByName.get(arg.toLowerCase());

				if (entity == null) {
					try {
						entity = getOnlyEntityAtCoordinate(arg);
					} catch (InvalidCoordinateException e) {
						// Well, we tried. It's totally unparsable. Do nothing
					}
				}
				break;
		}

		return entity;
	}

	void moveEntityToCoordinate(RPMapEntity<?> mapEntity, RPCoordinate destCoordinate) throws NullPointerException {
		if (mapEntity == null) {
			throw new NullPointerException("mapEntity cannot be null");
		}

		RPCoordinate srcCoordinate = mapEntity.getCoordinate();

		// Get the entity from the src list
		RPMapEntityList srcEntityList = entitiesByCoordinate.get(srcCoordinate);
		// Theoretically should never be null/empty list

		srcEntityList.remove(mapEntity);
		// Optimize cleanup by removing old references
		// Functional overhead is very minor while saving on memory overhead can have large benefits
		if (srcEntityList.isEmpty()) {
			entitiesByCoordinate.remove(srcCoordinate);
		}

		setEntity(destCoordinate, mapEntity);

		mapEntity.setCoordinate(destCoordinate);
	}
}
