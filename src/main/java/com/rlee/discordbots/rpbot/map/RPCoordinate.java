package com.rlee.discordbots.rpbot.map;

import java.util.Objects;

public class RPCoordinate implements Comparable<RPCoordinate> {
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

	@Override
	public String toString() {
		return CoordinateParser.rpCoordinateToString(this);
	}

	/**
	 * Evaluates whether or not the given object is an instance of RPCoordinate and has the same row and col
	 * @param obj
	 * @return True if the two have matching rows and cols, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof  RPCoordinate)) {
			return false;
		}

		RPCoordinate coordinate = (RPCoordinate) obj;
		return getRow() == coordinate.getRow() && getCol() == coordinate.getCol();
	}

	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * <p>
	 * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)
	 * <p>
	 * <p>The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.
	 * <p>
	 * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.
	 * <p>
	 * <p>It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 * <p>
	 * <p>In the foregoing description, the notation
	 * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
	 * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
	 * <tt>0</tt>, or <tt>1</tt> according to whether the value of
	 * <i>expression</i> is negative, zero or positive.
	 *
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                              from being compared to this object.
	 */
	@Override
	public int compareTo(RPCoordinate o) {
		return compareTo(o, true);
	}

	int compareTo(RPCoordinate o, boolean invertRows) {
		int inverter = invertRows ? -1 : 1;
		if (row < o.row) {
			return -1 * inverter;
		} else if (row > o.row) {
			return 1 * inverter;
		}

		if (col < o.col) {
			return -1;
		} else if (col > o.col) {
			return 1;
		}

		return 0;
	}

	/**
	 * Get the hashCode as defined by
	 * Object.hash(getRow(), getCol())
	 * @return The hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(row, col);
	}
}
