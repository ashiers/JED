package com.tacticalenterprisesltd;

public class ColumnOrder
{
	private int index = -1;
	private int column = -1;
	public enum Dir {
		ASCENDING{
			public String toString(){
				return "asc";
			}
		},
		DECENDING{
			public String toString(){
				return "desc";
			}
		}
	}
	private Dir direction = Dir.ASCENDING; //default
	/**
	 * Constructor
	 * @param indexvalue
	 */
	public ColumnOrder(int indexvalue)
	{
		index = indexvalue;
	}
	/**
	 * Set an index value for this ColumnOrder object
	 * @param value
	 */
	public void setIndex(int value)
	{
		index = value;
	}
	/**
	 * Get the index value for this ColumnOrder object
	 * @return
	 */
	public int getIndex()
	{
		return index;
	}
	/**
	 * Set to which Column this ColumnOrder object is associated
	 * @param value
	 */
	public void setColumn(int value)
	{
		column = value;
	}
	/**
	 * Get to which Column this ColumnOrder object is associated
	 * @return
	 */
	public int getColumn()
	{
		return column;
	}
	/**
	 * Set the direction of the order "asc" or "dec".
	 * Be sure to use the Enum named Dir.
	 * @param value
	 */
	public void setDirection(Dir value)
	{
		direction = value;
	}
	/**
	 * Get the direction of the order "asc" or "dec".
	 * @return
	 */
	public Dir getDirection()
	{
		return direction;
	}
	
	@Override
	public String toString()
	{
		return "index: " + index + " column: " + column + " direction: " + direction;
	}
}

