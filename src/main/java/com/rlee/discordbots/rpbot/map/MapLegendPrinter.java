package com.rlee.discordbots.rpbot.map;

import com.rlee.discordbots.rpbot.Util;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

class MapLegendPrinter {
	private static final String LIST_DELIMITER = ", ";

	/**
	 * Get a string representation of the entities with the given symbol.
	 * Will print out the entities with their coordinates sequentially.
	 * @param symbol The symbol to prefix the output with.
	 * @param symbolEntityList The entities to print out.
	 * @return The string for the particular entities with the given symbol.
	 */
	String getSymbolLegend(Character symbol, RPMapEntityList symbolEntityList) {
		if (symbol == null) {
			return null;
		}

		String prefix = "`" + symbol + "` - ";
		StringJoiner sj = new StringJoiner(LIST_DELIMITER); // Join elements of a list together without the trailing comma

		if (Util.isEmptyCollection(symbolEntityList)) {
			return prefix + "(No entity with given symbol)";
		}

		for (RPMapEntity<?> entity : symbolEntityList) {
			sj.add(entity.getName() + " [**" + entity.getCoordinate() + "**]");
		}

		return prefix + sj.toString();
	}

	/**
	 * Get a string representation of the entities with the given coordinate.
	 * will out the entities with their symbols sequentially.
	 * @param coordinate The coordinate to prefix the output with.
	 * @param coordinateEntityList The entities to print out.
	 * @return The string for the particular entities with the given coordinate.
	 */
	String getCoordinateLegend(RPCoordinate coordinate, RPMapEntityList coordinateEntityList) {
		if (coordinate == null) {
			return null;
		}

		String prefix = "**" + coordinate + "** - ";
		StringJoiner sj = new StringJoiner(LIST_DELIMITER); // Join elements of a list together without the trailing comma

		if (Util.isEmptyCollection(coordinateEntityList)) {
			return prefix + "(No entity at given coordinate)";
		}

		for (RPMapEntity<?> entity : coordinateEntityList) {
			sj.add(entity.getName() + " [`" + entity.getSymbol() + "`]");
		}

		return prefix + sj.toString();
	}

	/**
	 * Get the legend of an RPMap sorted by its symbols.
	 * @param mapName The name of the RPMap to print out
	 * @param entitiesBySymbols The entities to print out
	 * @return A string representation of the legend.
	 */
	String showLegendBySymbols(String mapName, TreeMap<Character, RPMapEntityList> entitiesBySymbols) {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("__**" + mapName + "** Legend__:");

		String entryLegend;
		for (Map.Entry<Character, RPMapEntityList> entry : entitiesBySymbols.entrySet()) {
			if (entry.getKey() == null || Util.isEmptyCollection(entry.getValue())) {
				continue;
			}

			entryLegend = getSymbolLegend(entry.getKey(), entry.getValue());
			if (Util.isEmptyString(entryLegend)) {
				continue;
			}

			sj.add(entryLegend);
		}

		return sj.toString();
	}
}
