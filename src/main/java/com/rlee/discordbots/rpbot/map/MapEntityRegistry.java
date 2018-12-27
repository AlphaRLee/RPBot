package com.rlee.discordbots.rpbot.map; // TODO decide on nesting inside map or registry section (preference to map)

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import com.rlee.discordbots.rpbot.regitstry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

class MapEntityRegistry implements Registry {
	enum SearchType {
		SYMBOL,
		COORDINATE,
		ENTITY
	}

	private TreeMap<RPCoordinate, RPMapEntity<?>> entitiesByCoordinate;
	private Map<Character, RPMapEntity<?>> entityLookupByChar; //Lookup for all unique entities that are generated

	MapEntityRegistry() {
		entitiesByCoordinate = new TreeMap<>();
		entityLookupByChar = new LinkedHashMap<>();
	}

	@Override
	public boolean containsName(String name) {
		// TODO Decide on implementation
		return false;
	}

	Object getEntity(RPCoordinate coordinate) {
		return entitiesByCoordinate.get(coordinate).getEntity();
	}

	/**
	 * Get the unique entity represented by the given character
	 * @param c The character representation of the entity
	 * @return The entity, or null if zero or more than 1 instance found.
	 */
	Object getEntity(Character c) {
		return entityLookupByChar.get(c).getEntity();
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
		RPMapEntity<E> mapEntity = new RPMapEntity<E>(symbol, entity, coordinate);

		entitiesByCoordinate.put(coordinate, mapEntity);
		checkAndModifyLookup(mapEntity);
	}

	TreeMap<RPCoordinate, RPMapEntity<?>> getEntitiesByCoordinate() {
		return entitiesByCoordinate;
	}

	/**
	 * Check the EntityLookup and modify it based on the given entity.
	 * If the given entity's symbol is already listed, then delete the entry from the lookup.
	 * If the entity is not listed, then add the entry to the lookup.
	 * @param entity
	 */
	private void checkAndModifyLookup(RPMapEntity<?> entity) {
		char symbol = entity.getSymbol();
		if (entityLookupByChar.containsKey(symbol)) {
			//TODO Decide on implementation. Present implementation: Remove all non-unique entries from lookup
//			entityLookupByChar.remove(symbol);
		} else {
			entityLookupByChar.put(symbol, entity);
		}
	}

	RPMapEntity<?> getEntityByType(String arg, SearchType searchType) throws NullPointerException, InvalidCoordinateException {
		if (searchType == null) {
			throw new NullPointerException("SearchType must be specified.");
		}

		if (Util.isEmptyString(arg)) {
			return null;
		}

		RPMapEntity<?> entity = null;
		switch (searchType) {
			case SYMBOL:
				entity = entityLookupByChar.get(arg.charAt(0));
				break;
			case COORDINATE:
				entity = entitiesByCoordinate.get(new CoordinateParser().parseCoordinates(arg));
				break;
			case ENTITY:
				break;
		}

		return entity;
	}
}
