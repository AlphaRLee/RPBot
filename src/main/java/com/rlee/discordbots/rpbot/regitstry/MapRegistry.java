package com.rlee.discordbots.rpbot.regitstry;

import com.rlee.discordbots.rpbot.RPBot;
import com.rlee.discordbots.rpbot.game.RPGame;
import com.rlee.discordbots.rpbot.map.RPMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapRegistry implements Registry {
	private RPGame game;

	//RPMaps by names
	private Map<String, RPMap> rpMaps;

	private RPMap activeMap;

	public MapRegistry(RPGame game) {
		this.game = game;
		rpMaps = new LinkedHashMap<>();
		activeMap = null;
	}

	public RPGame getGame() {
		return game;
	}

	public RPMap getMap(String name) {
		return rpMaps.get(name.toLowerCase());
	}

	/**
	 * Add a new RPMap into the registry. Will overwrite any existing maps with the same name.
	 * @param rpMap The map to add. Must have a name.
	 * @throws IllegalArgumentException Thrown if the map has no name or map is null.
	 */
	public void addMap(RPMap rpMap) throws IllegalArgumentException {
		if (rpMap == null) {
			throw new IllegalArgumentException("RPMap must not be null.");
		}
		if (RPBot.isEmptyString(rpMap.getName())) {
			throw new IllegalArgumentException("RPMap must have a name.");
		}

		rpMaps.put(rpMap.getName().toLowerCase(), rpMap);
	}

	/**
	 * Remove the map with the given name.
	 * @param name The name of the map to remove.
	 * @return The map that was removed, or null if no entry was found.
	 */
	public RPMap removeMap(String name) {
		return rpMaps.remove(name.toLowerCase());
	}

	/**
	 * Remove the RPMap. Will only remove if the map assigned to the given map's name matches the given map.
	 * @param rpMap The map to remove.
	 * @return True if properly removed, false if not.
	 */
	public boolean removeMap(RPMap rpMap) {
		return rpMaps.remove(rpMap.getName(), rpMap);
	}

	public boolean containsName(String name) {
		return rpMaps.containsKey(name.toLowerCase());
	}

	public boolean isEmpty() {
		return rpMaps.isEmpty();
	}

	public RPMap getActiveMap() {
		 return activeMap;
	}

	public void setActiveMap(RPMap rpMap) {
		this.activeMap = rpMap;
	}
}
