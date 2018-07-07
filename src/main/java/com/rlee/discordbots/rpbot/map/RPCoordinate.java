package com.rlee.discordbots.rpbot.map;

public class RPCoordinate {
	private int row, col;	//Row number and column number, using traditional 2D array notation
	
	public RPCoordinate(int row, int col) {
		this.setRow(row);
		this.setCol(col);
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	
}
