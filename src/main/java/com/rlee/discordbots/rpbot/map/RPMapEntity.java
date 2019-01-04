package com.rlee.discordbots.rpbot.map;

class RPMapEntity <E> {
	private E entity;

	private char symbol;
	private String name;
	private RPCoordinate coordinate;

	private boolean suppressedOutput; // TODO Consume this flag somewhere

	/**
	 * Create a new RPMapEntity.
	 * The suppressedOutput flag is always set to false.
	 * @param symbol
	 * @param entity
	 * @param coordinate
	 */
	RPMapEntity(char symbol, E entity, RPCoordinate coordinate) {
		this(symbol, entity, coordinate, false);
	}

	/**
	 * Create a new RPMapEntity
	 * @param symbol
	 * @param entity
	 * @param coordinate
	 * @param suppressedOutput Set to true to suppress output for batch messages, such as the legend of the RPMap
	 */
	RPMapEntity(char symbol, E entity, RPCoordinate coordinate, boolean suppressedOutput) {
		this.entity = entity;

		this.symbol = symbol;	//TODO Add support for 2D shape instead of single char
								//TODO Add support for entity larger than 1 cell
		this.name = entity.toString();
		this.coordinate = coordinate;

		this.suppressedOutput = suppressedOutput;
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

	boolean isSuppressedOutput() {
		return suppressedOutput;
	}

	void setSuppressedOutput(boolean suppressedOutput) {
		this.suppressedOutput = suppressedOutput;
	}
}
