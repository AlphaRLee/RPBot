package com.rlee.discordbots.rpbot.map; // TODO decide on nesting inside map or registry section (preference to map)

import com.rlee.discordbots.rpbot.Util;
import com.rlee.discordbots.rpbot.exception.InvalidCoordinateException;
import com.rlee.discordbots.rpbot.regitstry.Registry;

import javax.naming.NameAlreadyBoundException;
import java.util.*;

class MapEntityRegistry implements Registry {
	enum SearchType {
		SYMBOL,
		COORDINATE,
		ENTITY
	}

	private TreeMap<RPCoordinate, RPMapEntityList> entitiesByCoordinate;
	private TreeMap<Character, RPMapEntityList> entitiesBySymbol; //Lookup for all unique entities that are generated
	private Map<String, RPMapEntity<?>> entitiesByName; // Entities by full name

	static final String AUTO_RENAME_INFIX = "-";
	static final int MAX_AUTO_RENAME_ATTEMPTS = 10000;

	MapEntityRegistry() {
		entitiesByCoordinate = new TreeMap<>(); // Store entries sorted by coordinate
		entitiesBySymbol = new TreeMap<>(); // Store entries alphabetically
		entitiesByName = new HashMap<>();
	}

	@Override
	public boolean containsName(String name) {
		// TODO Decide on implementation
		return false;
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
	 * Get teh list of entities with the given symbol
	 * @param symbol
	 * @return
	 */
	RPMapEntityList getEntityList(Character symbol) {
		return entitiesBySymbol.get(symbol);
	}

	/**
	 * Get the unique entity represented by the given name
	 * @param name The name of the entity. Case insensitive
	 * @return The entity. Always return first inserted in case of name conflict
	 */
	RPMapEntity<?> getEntity(String name) {
		return entitiesByName.get(name.toLowerCase());
	}

	/**
	 * Add an entity at the given coordinate
	 * @param coordinate
	 * @param mapEntity
	 * @param allowAutoRename If true, then any name collisions will be resolved by adding a unique name, if false then the entity is not added in the case of a name collision
	 * @return The name of the entity (which can potentially be re-named)
	 * @throws NameAlreadyBoundException Thrown if renaming is not allowed and the name is already taken.
	 */
	String addEntity(RPCoordinate coordinate, RPMapEntity<?> mapEntity, boolean allowAutoRename) throws NameAlreadyBoundException {
		String outputName = addEntityByName(mapEntity, allowAutoRename);
		addEntityToMappedList(entitiesByCoordinate, mapEntity.getCoordinate(), mapEntity, true);
		addEntityToMappedList(entitiesBySymbol, mapEntity.getSymbol(), mapEntity);

		return outputName;
	}

	/**
	 * Add an entity to the appropriate map of RPMapEntityLists. This always adds the entity at the end of the RPMapEntityList
	 * @param mappedList
	 * @param key The key of the map
	 * @param mapEntity
	 * @param <K> The type of the key of the map
	 */
	private <K> void addEntityToMappedList(Map<K, RPMapEntityList> mappedList, K key, RPMapEntity<?> mapEntity) {
		addEntityToMappedList(mappedList, key, mapEntity, false);
	}

	/**
	 * Add an entity to the appropriate map of RPMapEntityLists
	 * @param mappedList
	 * @param key The key of the map
	 * @param mapEntity
	 * @param addFirst If set to true, then the entity is added as the first element within the RPMapEntityList
	 * @param <K> The type of the key of the map
	 */
	private <K> void addEntityToMappedList(Map<K, RPMapEntityList> mappedList, K key, RPMapEntity<?> mapEntity, boolean addFirst) {
		RPMapEntityList entityList = mappedList.get(key);
		if (entityList == null) {
			entityList =  new RPMapEntityList();
			mappedList.put(key, entityList);
		}

		if (addFirst) {
			entityList.addFirst(mapEntity);
		} else {
			entityList.add(mapEntity);
		}
	}

	/**
	 * Check the EntityLookup and modify it based on the given entity.
	 * If the given entity's symbol is already listed, then do nothing.
	 * If the entity is not listed, then add the entry to the lookup.
	 * @param entity
	 * @return The name of the entity (which can potentially be re-named)
	 * @throws NameAlreadyBoundException Thrown if renaming is not allowed and the name is already taken.
	 */
	private String addEntityByName(RPMapEntity<?> entity, boolean allowAutoRename) throws NameAlreadyBoundException {
		// Allow only unique entries for names
		String name = entity.getName().toLowerCase();
		if (entitiesByName.containsKey(name)) {
			if (allowAutoRename) {
				String entityRename = getUniqueEntityName(entity.getName());
				if (entityRename == null) {
					throw new NameAlreadyBoundException("The maximum number of entities with the name " + name + " already exists.");
				}

				entity.setName(entityRename);
				entitiesByName.put(entityRename.toLowerCase(), entity);
			} else {
				throw new NameAlreadyBoundException("The name " + name + " is already bound to another entity.");
			}
		} else {
			entitiesByName.put(name, entity);
		}

		return entity.getName();
	}

	/**
	 * Remove an entity from the RPMap.
	 * @param mapEntity
	 * @return True if the entity was successfully removed, false if not (e.g. Entity does not exist)
	 */
	boolean removeEntity(RPMapEntity<?> mapEntity) {
		boolean removeEntitySymbolResult = false;
		RPMapEntityList entityListBySymbol = getEntityList(mapEntity.getSymbol());
		if (!Util.isEmptyCollection(entityListBySymbol)) {
			removeEntitySymbolResult = entityListBySymbol.remove(mapEntity);
			if (entityListBySymbol.isEmpty()) {
				entitiesBySymbol.remove(mapEntity.getSymbol());
			}
		}

		boolean removeEntityCoordinateResult = false;
		RPMapEntityList entityListAtCoordinate = getEntityList(mapEntity.getCoordinate());
		if (!Util.isEmptyCollection(entityListAtCoordinate)) {
			removeEntityCoordinateResult = entityListAtCoordinate.remove(mapEntity);
			if (entityListAtCoordinate.isEmpty()) {
				entitiesByCoordinate.remove(mapEntity.getSymbol()); // Clear empty lists for memory efficiency
			}
		}

		RPMapEntity<?> removedEntity = entitiesByName.remove(mapEntity.getName().toLowerCase());
		boolean removeEntityNameResult = (removedEntity != null);

		return removeEntitySymbolResult || removeEntityCoordinateResult || removeEntityNameResult;
	}

	void clearEntities() {
		entitiesBySymbol.clear();
		entitiesByCoordinate.clear();
		entitiesByName.clear();
	}

	/**
	 * Get a unique name based on the given entity name
	 * @param entityNameSeed The entity name to build a unique name from
	 * @return The unique name or null if no valid unique name could be found
	 */
	private String getUniqueEntityName(String entityNameSeed) {
		// Try cycling through to find the first valid auto-rename
		String entityRename;
		int i;

		// Note starting index on 1
		for (i = 1; i < MAX_AUTO_RENAME_ATTEMPTS; i++) {
			entityRename = entityNameSeed + AUTO_RENAME_INFIX + i;
			if (!entitiesByName.containsKey(entityRename.toLowerCase())) {
				return entityRename; // Successfully found unique name
			}
		}

		return null; // No unique name found TODO throw exception instead
	}

	TreeMap<RPCoordinate, RPMapEntityList> getEntitiesByCoordinate() {
		return entitiesByCoordinate;
	}

	TreeMap<Character,RPMapEntityList> getEntitiesBySymbol() {
		return entitiesBySymbol;
	}

	private RPMapEntityList parseEntityListAtCoordinate(String coordinateArg) throws InvalidCoordinateException {
		return entitiesByCoordinate.get(new CoordinateParser().parseCoordinates(coordinateArg));
	}

	/**
	 * @param entityList
	 * @param ambiguousSelectionMessage
	 * @return
	 * @throws AmbiguousSelectionException
	 */
	private RPMapEntity<?> getOnlyEntityFromList(RPMapEntityList entityList, String ambiguousSelectionMessage) throws AmbiguousSelectionException {
		if (Util.isEmptyCollection(entityList)) {
			return null;
		}

		if (entityList.size() > 1) {
			throw new AmbiguousSelectionException(ambiguousSelectionMessage);
		}

		 return entityList.get(0);
	}

	RPMapEntityList parseEntityListBySymbol(String symbolArg) {
		return entitiesBySymbol.get(symbolArg.charAt(0)); // TODO Should give user leeway and let selections be case-insensitive?
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
	 * Parse an entity and get the RPMapEntityList associated with the parsed entity
	 * @param mapEntityArg
	 * @return
	 */
	RPMapEntityList parseMapEntityList(String mapEntityArg) throws InvalidCoordinateException {
		if (Util.isEmptyString(mapEntityArg)) {
			return null;
		}

		return parseEntityListByType(mapEntityArg, parseSearchType(mapEntityArg));
	}

	/**
	 * Attempt to parse the search type of the arg based on the following rules:
	 * <br/>If the arg is just 1 character long, it is assumed to be a symbol
	 * <br/>If the arg is 2 characters long, it is assumed to be a coordinate
	 * <br/>All other cases are assumed to be an entity
	 * @param arg The string to parse the search type of
	 * @return The assumed search type
	 */
	SearchType parseSearchType(String arg) {
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
				entity = getOnlyEntityFromList(parseEntityListBySymbol(arg), "Selection ambiguous: multiple entities have the same symbol.");
				break;
			case COORDINATE:
				try {
					entity = getOnlyEntityFromList(parseEntityListAtCoordinate(arg), "Selection ambiguous: multiple entities have the same coordinate.");
				} catch (InvalidCoordinateException e) {
					entity = entitiesByName.get(arg.toLowerCase()); // Assume the arg is an entity instead of a coordinate
				}
				break;
			case ENTITY:
				entity = entitiesByName.get(arg.toLowerCase());

				if (entity == null) {
					try {
						entity = getOnlyEntityFromList(parseEntityListAtCoordinate(arg), "Selection ambiguous: multiple entities have the same coordinate.");
					} catch (InvalidCoordinateException e) {
						// Well, we tried. It's totally unparsable. Do nothing
					}
				}
				break;
		}

		return entity;
	}

	/**
	 * @deprecated Is this being used?
	 * @param arg
	 * @param searchType
	 * @return
	 * @throws NullPointerException
	 * @throws InvalidCoordinateException
	 */
	private RPMapEntityList parseEntityListByType(String arg,  SearchType searchType) throws  NullPointerException, InvalidCoordinateException {
		if (searchType == null) {
			throw new NullPointerException("SearchType must be specified.");
		}

		if (Util.isEmptyString(arg)) {
			return null;
		}

		RPMapEntityList entityList = null;
		switch (searchType) {
			case SYMBOL:
				entityList = parseEntityListBySymbol(arg);
				break;
			case COORDINATE:
				entityList = parseEntityListAtCoordinate(arg);
				break;
			case ENTITY:
				// TODO Determine behaviour here. Currently passes to null (not supported)
				break;
		}

		return entityList;
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

		addEntityToMappedList(entitiesByCoordinate, destCoordinate, mapEntity, true);
		mapEntity.setCoordinate(destCoordinate);
	}
}
