package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.exception.EntityCacheNotBuiltException;

import java.util.*;

/**
 * A utility to cache information from an RPMap about the entities stored within
 */
class EntityCache implements Iterable<RPMapEntity<?>> {
	private ArrayList<RPMapEntity<?>> cachedEntities;
	private RPCoordinate bottomLeftCorner, topRightCorner; //Corners of the cached region, inclusive

	private boolean hasBeenBuilt;

	private Iterator<RPMapEntity<?>> iterator;

	EntityCache() {
		hasBeenBuilt = false;
	}

	//TODO Delete me!
//	ArrayList<RPMapEntity<?>> getCachedEntities() {
//		return cachedEntities;
//	}

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
	void buildCache(LinkedList<RPMapEntity<?>> entities) {
		cachedEntities = new ArrayList<>();
		for (RPMapEntity<?> entity : entities) {
			if (RPMap.isInRegion(entity.getCoordinate(), bottomLeftCorner, topRightCorner)) {
				cachedEntities.add(entity);
			}
		}

		hasBeenBuilt = true;
	}

	/**
	 * Sort the cached entities by their coordinates
	 */
	void sortCachedEntitiesByCoordinates(boolean invertRows) {
//		Collections.sort(cachedEntities, Comparator.comparing(RPMapEntity::getCoordinate));

		Collections.sort(cachedEntities, (leftEntity, rightEntity) -> leftEntity.getCoordinate().compareTo(rightEntity.getCoordinate(), invertRows));
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public Iterator<RPMapEntity<?>> iterator() {
		iterator = cachedEntities.iterator();
		return iterator;
	}

	/**
	 * Return the last iterator created from the {@link #iterator} method.
	 * Useful for iterating over the course of multiple methods
	 * @return
	 */
	Iterator<RPMapEntity<?>> existingIterator() {
		return iterator;
	}
}
