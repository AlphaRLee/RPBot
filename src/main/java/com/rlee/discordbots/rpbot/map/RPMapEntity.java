package com.rlee.discordbots.rpbot.map;

public class RPMapEntity <E> {
	private char symbol;
	private E entity;
	
	public RPMapEntity(char symbol, E entity) {
		this.symbol = symbol;	//TODO Add support for 2D shape instead of single char
		this.entity = entity;
	}
	
	public char getSymbol() {
		return symbol;
	}
	
	public void setSymbol(char symbol) {
		this.symbol = symbol;
	}
	
	public E getEntity() {
		return entity;
	}
	
	public void setEntity(E entity) {
		this.entity = entity;
	}
	
	/**
	 * <p>Get a legend on what this map entity represents.
	 * Returns a string with the output</p>
	 * <p>symbol: entity</p>
	 * <p>where symbol is the symbol representing this entity
	 * and entity represents {@link #getEntity()} with the {@link #toString()} method invoked
	 * @return The string representation of this entity in a legend or null if entity is set to null
	 */
	public String getLegend() {
		if (entity == null) {
			return null;
		}
		
		return symbol + ": " + entity.toString();
	}
}
