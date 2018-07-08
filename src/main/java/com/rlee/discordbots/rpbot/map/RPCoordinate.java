package com.rlee.discordbots.rpbot.map;

public class RPCoordinate {
	private int row, col;	//Row number and column number, using traditional 2D array notation

	RPCoordinate(int row, int col) {
		this.row = row;
		this.col = col;
	}

	int getRow() {
		return row;
	}

	void setRow(int row) {
		this.row = row;
	}

	int getCol() {
		return col;
	}

	void setCol(int col) {
		this.col = col;
	}
}
