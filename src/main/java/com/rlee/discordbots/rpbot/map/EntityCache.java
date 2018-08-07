package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.exception.EntityCacheNotBuiltException;

import java.util.*;

/**
 * A utility to cache information from an RPMap about the entities stored within
 */
class EntityCache {
	@Deprecated
	private ArrayList<RPMapEntity<?>> cachedEntities; //TODO Delete deprecated: cachedEntities is being replaced by a cached Treemap

	private NavigableMap<RPCoordinate, RPMapEntity<?>> cachedEntitiesByCoordinate;

	private RPCoordinate bottomLeftCorner, topRightCorner; //Corners of the cached region, inclusive

	private boolean hasBeenBuilt;

	EntityCache() {
		hasBeenBuilt = false;
	}

	ArrayList<RPMapEntity<?>> getCachedEntities() {
		return cachedEntities;
	}

	RPCoordinate getBottomLeftCorner() {
		return bottomLeftCorner;
	}

	void setBottomLeftCorner(RPCoordinate bottomLeftCorner) {
		this.bottomLeftCorner = bottomLeftCorner;
	}

	RPCoordinate getTopRightCorner() {
		return topRightCorner;
	}

	void setTopRightCorner(RPCoordinate topRightCorner) {
		this.topRightCorner = topRightCorner;
	}

	/**
	 * Update the cached entities to reflect the bottom left corner and the row and col counts.
	 * Requires that {@link EntityCache#buildCache(LinkedList)} has been executed at least once.
	 * @throws EntityCacheNotBuiltException Thrown if the entity cache has not been built at least once yet.
	 */
	void updateCache() throws EntityCacheNotBuiltException {
		if (!hasBeenBuilt) {
			throw new EntityCacheNotBuiltException();
		}

		cachedEntities.removeIf((entity) -> !RPMap.isInRegion(entity.getCoordinate(), bottomLeftCorner, topRightCorner));
	}

	/**
	 * Build the cached entities from the list of given entities and the stored corners
	 * Use if cache has never been built yet or cache changes are dramatically different.
	 * For minor cache changes, consider using {@link EntityCache#updateCache()}
	 * @param entities The list of entities to build the cache from
	 */
	//TODO Delete deprecated method
	@Deprecated
	void buildCache(LinkedList<RPMapEntity<?>> entities) {
		cachedEntities = new ArrayList<>();
		for (RPMapEntity<?> entity : entities) {
			if (RPMap.isInRegion(entity.getCoordinate(), bottomLeftCorner, topRightCorner)) {
				cachedEntities.add(entity);
			}
		}

		hasBeenBuilt = true;
	}

	void buildCache(TreeMap<RPCoordinate, RPMapEntity<?>> entitiesByCoordinate) {
		//Get a subset of the coordinate map. Note inverted coordinate location to accomodate inverted sort
		cachedEntitiesByCoordinate = entitiesByCoordinate.tailMap(topRightCorner, true).headMap(bottomLeftCorner, true);
		hasBeenBuilt = true;
	}

	/**
	 * Sort the cached entities by their coordinates
	 */
	void sortCachedEntitiesByCoordinates(boolean invertRows) {
//		Collections.sort(cachedEntities, Comparator.comparing(RPMapEntity::getCoordinate));

		Collections.sort(cachedEntities, (leftEntity, rightEntity) -> leftEntity.getCoordinate().compareTo(rightEntity.getCoordinate(), invertRows));
	}
}
