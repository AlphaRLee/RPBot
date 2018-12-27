package com.rlee.discordbots.rpbot.map;

class RPMapEntity <E> {
	private char symbol;
	private String name;
	private E entity;

	private RPCoordinate coordinate;

	RPMapEntity(char symbol, E entity, RPCoordinate coordinate) {
		this.symbol = symbol;	//TODO Add support for 2D shape instead of single char
								//TODO Add support for entity larger than 1 cell
		this.entity = entity;
		this.name = entity.toString();
		this.coordinate = coordinate;
	}
	
	char getSymbol() {
		return symbol;
	}
	
	void setSymbol(char symbol) {
		this.symbol = symbol;
	}

	String getName() { return name; }

	void setName(String name) { this.name = name; }

	E getEntity() {
		return entity;
	}
	
	void setEntity(E entity) {
		this.entity = entity;
	}

	RPCoordinate getCoordinate() {
		return coordinate;
	}

	void setCoordinate(RPCoordinate coordinate) {
		this.coordinate = coordinate;
	}

	/**
	 * <p>Get a legend on what this map entity represents.
	 * Returns a string with the output</p>
	 * <p>symbol: entity</p>
	 * <p>where symbol is the symbol representing this entity
	 * and entity represents {@link #getEntity()} with the {@link #toString()} method invoked
	 * @return The string representation of this entity in a legend or null if entity is set to null
	 */
	String getLegend() {
		if (entity == null) {
			return null;
		}
		
		return symbol + ": " + name;
	}
}
