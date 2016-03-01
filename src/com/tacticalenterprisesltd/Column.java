package com.tacticalenterprisesltd;

public class Column
{
	private int index = -1;
	private String name = "";
	private String data = "";
	private Boolean searchable = false;
	private String searchValue = "";
	private Boolean searchRegex = false;
	private Boolean orderable = false;
	
	/**
	 * Constructor
	 * @param indexvalue
	 */
	public Column(int indexvalue)
	{
		index = indexvalue;
	}
	/**
	 * Set an index value for this particular column
	 * @param value
	 */
	public void setIndex(int value)
	{
		index = value;
	}
	/**
	 * Get an index value for this particular column
	 * @return int
	 */
	public int getIndex()
	{
		return index;
	}
	/**
	 * Set a Name for this particular column
	 * @param value
	 */
	public void setName(String value)
	{
		if(value != null)
		   name = value;
	}
	/**
	 * Get the name of this particular column
	 * @return String
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Set the data for this column
	 * @param value
	 */
	public void setData(String value)
	{
		if(value != null)
		   data = value;
	}
	/**
	 * Get the data for this column
	 * @return
	 */
	public String getData()
	{
		return data;
	}
	/**
	 * Set whether or not this column is searchable. The default is false.
	 * @param value
	 */
	public void setSearchable(boolean value)
	{
		searchable = value;
	}
	/**
	 * Determine if this column is searchable.
	 * @return
	 */
	public boolean getSearchable()
	{
		return searchable;
	}
	/**
	 * Set a search value
	 * @param value
	 */
	public void setSearchValue(String value)
	{
		if(value != null)
		   searchValue = value;
	}
	/**
	 * Get the search value
	 * @return
	 */
	public String getSearchValue()
	{
		return searchValue;
	}
	/**
	 * Set the search regex value
	 * @param value
	 */
	public void setSearchRegex(boolean value)
	{
		searchRegex = value;
	}
	/**
	 * Get the search regex value
	 * @return
	 */
	public boolean getSearchRegex()
	{
		return searchRegex;
	}
	/**
	 * Set whether or not this column can be sorted
	 * @param value
	 */
	public void setOrderable(boolean value)
	{
		orderable = value;
	}
	/**
	 * Get whether or not this column can be sorted
	 * @return
	 */
	public boolean getOrderable()
	{
		return orderable;
	}
	@Override
	public String toString()
	{
		String strname = name;
		String strdata = data;
		String strSearchValue = searchValue;
		if(strname.equals(""))
			strname = "NO VALUE";
		if(strdata.equals(""))
			strdata = "NO VALUE";
		if(strSearchValue.equals(""))
			strSearchValue = "NO VALUE";
		return "index: " + index + " data: " + strdata + " name: " + strname + " orderable: " + orderable + " searchRegex: " + searchRegex + " searchValue: " +
		strSearchValue + " searchable: " + searchable; 
	}
}

