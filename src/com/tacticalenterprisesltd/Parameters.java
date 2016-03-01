package com.tacticalenterprisesltd;

import java.util.*;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * This class contains all the parameters passed to the server side from DataTables
 * on the client side.  All parameters represent the communications link between
 * the client side and server side. This version of JED Parameters adheres to the parameters
 * supported by DataTables v1.10.10 and Editor v1.5.3.
 * <p>This version of JED contains two additional classes: Column and ColumnOrder.
 * For server side processing, DataTables is using many parameters while distinguishing
 * the various properties of each column, such as whether or not it can be searchable or sorted.
 * Therefore, we've translated those parameters into actual classes to match up. 
 * 
 * @author Alan Shiers
 * @version 1.5.0
 */
//See: http://datatables.net/manual/server-side

public class Parameters
{
	private String action = "";
	private String upload_rowid = "";
	private int draw = -1;
	private int start = -1;
	private int length = -1;	
	private String searchValue = "";
	private Boolean searchRegex = false;	
	private ArrayList<Column> columns = new ArrayList<Column>();
	private ArrayList<ColumnOrder> colorders = new ArrayList<ColumnOrder>();
	//The data TreeMap stores keys in sorted order
	private TreeMap<String,String> data = new TreeMap<String,String>();
	private Logger logger;
	
    public Parameters()
    {
    	super();
    	logger = Logger.getLogger(Parameters.class);
    }
	
	/**
	 * Call this method once you have collected all the parameters beginning with the word "columns".
	 * This method will then translate all those parameters into actual objects named Column and store
	 * them all in an ArrayList&lt;Column&gt;<br>
	 * EXAMPLE DATA:<br>
     * columns[0][data]	0<br>
     * columns[0][name]<br>	
     * columns[0][orderable]	true<br>
     * columns[0][search][regex]	false<br>
     * columns[0][search][value]<br>	
     * columns[0][searchable]	true<br>
     * columns[1][data]	1<br>
     * columns[1][name]<br>	
     * columns[1][orderable]	true<br>
     * columns[1][search][regex]	false<br>
     * columns[1][search][value]<br>	
     * columns[1][searchable]	true<br>
     * columns[2][data]	2<br>
     * columns[2][name]<br>	
     * columns[2][orderable]	true<br>
     * columns[2][search][regex]	false<br>
     * columns[2][search][value]<br>	
     * columns[2][searchable]	true<br>
     * columns[3][data]	3<br>
     * columns[3][name]<br>	
     * columns[3][orderable]	true<br>
     * columns[3][search][regex]	false<br>
     * columns[3][search][value]<br>	
     * columns[3][searchable]	true<br>
     * columns[4][data]	4<br>
     * columns[4][name]<br>	
     * columns[4][orderable]	true<br>
     * columns[4][search][regex]	false<br>
     * columns[4][search][value]<br>	
     * columns[4][searchable]	true<br>
     * draw	1<br>
     * length	10<br>
     * order[0][column]	0<br>
     * order[0][dir]	asc<br>
     * search[regex]	false<br>
     * search[value]<br>	
     * start	0<br>
     * 
	 * @param cols
	 * @throws IllegalArgumentException
	 */
	public void createColumnObjects(Map<String,String> cols)throws IllegalArgumentException
	{
	  if(cols == null)
		  throw new IllegalArgumentException("Map is null");
	  if(cols.size() == 0)
		  throw new IllegalArgumentException("Size of Map is zero.");
	  try{	    
	      String attrData = "[data]";
	      String attrName = "[name]";
	      String attrOrderable = "[orderable]";
	      String attrSearchRegex = "[search][regex]";
	      String attrSearchValue = "[search][value]";
	      String attrSearchable = "[searchable]";
		  //Sort through all the paramters to find each distinct index value
		  ArrayList<Integer> indexvalues = new ArrayList<Integer>();
		  String key = "columns";
		  Iterator<String>  keys = cols.keySet().iterator();
		  String mapkey = "";		  
		  while (keys.hasNext())
		  {
		    mapkey = keys.next();
		   
		   if(mapkey.indexOf(key) > -1 )
		   {
			  //Get the index value
			  int start = 0;
			  int end = 0;
			  start = mapkey.indexOf("[");
			  end = mapkey.indexOf("]");
			  int indexvalue = Integer.parseInt(mapkey.substring(start + 1,end));
			  if(haveIndex(indexvalues,indexvalue) == false)
			  {
				  indexvalues.add(new Integer(indexvalue));
			  }			  
		    }
		  }//end while
		  //Make sure all index values are in ascending order
		  Collections.sort(indexvalues);
		  /*Print off all the index values
		  System.out.println("indexvalues:");
		  for(int i = 0; i < indexvalues.size(); i++)
		  {
			System.out.println(indexvalues.get(i));
		  }
		  */
		  //Now, create instances of class Column while initializing with an index value
		  for(int i = 0; i < indexvalues.size(); i++)
		  {
			columns.add(new Column(indexvalues.get(i)));  
		  }
		  //Set all other attributes for each Column instance
		  Column tempCol = null;
		  for(int i = 0; i < columns.size(); i++)
		  {
			 tempCol = columns.get(i);
			 tempCol.setData(cols.get("columns[" + tempCol.getIndex() + "]" + attrData));
			 tempCol.setName(cols.get("columns[" + tempCol.getIndex() + "]" + attrName));
			 if(cols.get("columns[" + tempCol.getIndex() + "]" + attrOrderable) != null)
			    tempCol.setOrderable(new Boolean(cols.get("columns[" + tempCol.getIndex() + "]" + attrOrderable)));
			 if(cols.get("columns[" + tempCol.getIndex() + "]" + attrSearchRegex) != null)
			    tempCol.setSearchRegex(new Boolean(cols.get("columns[" + tempCol.getIndex() + "]" + attrSearchRegex)));
			 tempCol.setSearchValue(cols.get("columns[" + tempCol.getIndex() + "]" + attrSearchValue));
			 if(cols.get("columns[" + tempCol.getIndex() + "]" + attrSearchable) != null)
			    tempCol.setSearchable(new Boolean(cols.get("columns[" + tempCol.getIndex() + "]" + attrSearchable)));			 
		  }	    
	  }
	  catch(Exception e)
	  {
		  if(Editor.isLoggingEnabled())
		     logger.error("Log4J error: " +  Editor.getFullStackTrace(e));
		  else
			 e.printStackTrace();
	  }
	}
	
	private boolean haveIndex(ArrayList<Integer> indexes, int value)
	{
		boolean haveit = false;
		for(int i = 0; i < indexes.size(); i++)
		{
			if(indexes.get(i) == value)
			{
				haveit = true;
				break;
			}
		}
		return haveit;
	}
	/**
	 * Call this method when you want to reference a particular Column
	 * @param index
	 * @return Column
	 */
	public Column getColumn(int index)
	{
		Column col = null;
		for(int i = 0; i < columns.size(); i++)
		{
			col = columns.get(i);
			if(col.getIndex() == index)
			{
				break;
			}
		}
		return col;
	}
	/**
	 * Call this method once you have collected all the parameters beginning with the word "order".
	 * This method will then translate all those parameters into actual objects named ColumnOrder and store
	 * them all in an ArrayList&lt;ColumnOrder&gt;<br>
     * Example:<br>
     * order[0][column]	1<br>
     * order[0][dir]	asc<br>	 
	 * @param orders
	 * @throws IllegalArgumentException
	 */
	public void createColumnOrderObjects(Map<String,String> orders)throws IllegalArgumentException
	{
		if(orders == null)
			  throw new IllegalArgumentException("Map is null");
		if(orders.size() == 0)
			  throw new IllegalArgumentException("Size of Map is zero.");
		try{	    
		      String attrColumn = "[column]";
		      String attrDir = "[dir]";
		      
			  //Sort through all the paramters to find each distinct index value
			  ArrayList<Integer> indexvalues = new ArrayList<Integer>();
			  String key = "order";
			  Iterator<String>  keys = orders.keySet().iterator();
			  String mapkey = "";		  
			  while (keys.hasNext())
			  {
			    mapkey = keys.next();
			   
			   if(mapkey.indexOf(key) > -1 )
			   {
				  //Get the index value
				  int start = 0;
				  int end = 0;
				  start = mapkey.indexOf("[");
				  end = mapkey.indexOf("]");
				  int indexvalue = Integer.parseInt(mapkey.substring(start + 1,end));
				  if(haveIndex(indexvalues,indexvalue) == false)
				  {
					  indexvalues.add(new Integer(indexvalue));
				  }			  
			    }
			  }//end while
			  //Make sure all index values are in ascending order
			  Collections.sort(indexvalues);
			  /*Print off all the index values
			  System.out.println("indexvalues:");
			  for(int i = 0; i < indexvalues.size(); i++)
			  {
				System.out.println(indexvalues.get(i));
			  }
			  */
			  //Now, create instances of class ColumnOrder while initializing with an index value
			  for(int i = 0; i < indexvalues.size(); i++)
			  {
				colorders.add(new ColumnOrder(indexvalues.get(i)));  
			  }
			  //Set all other attributes for each ColumnOrder instance
			  ColumnOrder tempOrder = null;
			  for(int i = 0; i < colorders.size(); i++)
			  {
				 tempOrder = colorders.get(i);
				 if(orders.get("order[" + tempOrder.getIndex() + "]" + attrColumn) != null)
				    tempOrder.setColumn(Integer.parseInt(orders.get("order[" + tempOrder.getIndex() + "]" + attrColumn)));
				 if(orders.get("order[" + tempOrder.getIndex() + "]" + attrDir) != null)
				 {
					String dir = orders.get("order[" + tempOrder.getIndex() + "]" + attrDir);
					if(dir.equals("asc"))
					   tempOrder.setDirection(ColumnOrder.Dir.ASCENDING);
					else
					   tempOrder.setDirection(ColumnOrder.Dir.DECENDING);
				 }		 
			  }	    
		  }
		  catch(Exception e)
		  {
			  if(Editor.isLoggingEnabled())
				 logger.error("Log4J error: " +  Editor.getFullStackTrace(e));
			  else
				 e.printStackTrace();
		  }	
	}
	/**
	 * Get the number of elements in the ArrayList&lt;ColumnOrder&gt;
	 * @return int
	 */
	public int getColumnOrdersSize()
	{
		return colorders.size();
	}
	/**
	 * Get a reference to a particular ColumnOrder object
	 * @param index
	 * @return
	 */
	public ColumnOrder getColumnOrder(int index)
	{
		ColumnOrder co = null;
		for(int i = 0; i < colorders.size(); i++)
		{
			co = colorders.get(i);
			if(co.getIndex() == index)
			{
				break;
			}
		}
		return co;
	}
	/**
	 * Get a reference to all the Column objects
	 * @return ArrayList&lt;Column&gt;
	 */
	public ArrayList<Column> getColumns()
	{
		return columns;
	}
	/**
	 * Get a reference to all the ColumnOrder objects
	 * @return ArrayList&lt;ColumnOrder&gt;
	 */
	public ArrayList<ColumnOrder> getColumnOrders()
	{
		return colorders;
	}
	/**
	 * Set the draw value
	 * @param value
	 */
	public void setDraw(int value)
	{
		draw = value;
	}
	/**
	 * Get the draw value
	 * @return int
	 */
	public int getDraw()
	{
		return draw;
	}
	/**
	 * Set the length, which is the number of records to display.
	 * @param value
	 */
	public void setLength(int value)
	{
		length = value;
	}
	/**
	 * Get the length
	 * @return int
	 */
	public int getLength()
	{
		return length;
	}
	/**
	 * Set the start value, regarding the SQL LIMIT clause, from &quot;start&quot; to &quot;end&quot;
	 * @param value
	 */
	public void setStart(int value)
	{
		start = value;
	}
	/**
	 * Get the start value.
	 * @return int
	 */
	public int getStart()
	{
		return start;
	}
	/**
	 * Set the search regex value
	 * @param value
	 */
	public void setSearchRegex(Boolean value)
	{
		searchRegex = value;
	}
	/**
	 * Get the search regex value
	 * @return
	 */
	public Boolean getSearchRegex()
	{
		return searchRegex;
	}
	/**
	 * Set the search value
	 * @param value
	 */
	public void setSearchValue(String value)
	{
		searchValue = value;
	}
	/**(
	 * Get the search value
	 * @return
	 */
	public String getSearchValue()
	{
		return searchValue;
	}
	/**
	 * Set the row id of the selected record from datatables
	 * during a file upload.
	 * @param value
	 */
	public void setUploadRowId(String value)
	{
		upload_rowid = value;
	}
	/**
	 * Get the row id of the selected record from datatables
	 * during a file upload.
	 * @return
	 */
	public String getUploadRowId()
	{
		return upload_rowid;
	}
	
	/**
	 * Set the action: create, edit, delete
	 * @param value
	 */
	public void setAction(String value)
	{
		action = value;
	}
	/**
	 * Get the action
	 * @return String
	 */
	public String getAction()
	{
		return action;
	}
	
	/**
	 * Add data
	 * @param key
	 * @param value
	 */
	public void addData(String key, String value)
	{
		data.put(key, value);
	}
	
	/**
	 * When data contains keys like: 
	 * data[row_1][FIRSTNAME], data[row_13][FIRSTNAME]
	 * where both the row id and the field name appear, we can
	 * extract both values to just an integer (the row id) and string values.
	 * Each pair of values will be presented as a string delimited by a colon. i.e. &quot;13:FIRSTNAME&quot; 
	 * Once you have all the keys, you can then call method getDataValue(int rowID, String fieldKey) to get the value.
	 * @return an ArrayList&lt;String&gt;
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getDataKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		String temp = "";
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  if(!mapkey.contains("many-count"))
		  {
		   temp = getRowNumber(mapkey) + ":" + getFieldName(mapkey);
		   keys.add(temp);
		  }
		}
		return keys;
	}
	
	
	/**
	 * Get multiple id values.  This is often used for deletion of row data and 
	 * obtaining the ids of multiple records that require editing.
	 * The data hashmap stores keys like:
	 * data[row_1][FIRSTNAME], data[row_13][FIRSTNAME], etc.
	 * Use this method to extract the row ids and return an array of integers.
	 * @return String[]
	 */
	@SuppressWarnings("unchecked")
	public int[] getDistinctIdValues()
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		int temp = -1;
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  temp = getRowNumber(mapkey);
		  if (ids.contains(temp) == false)
		  {
		    ids.add(new Integer(temp));
		  }		  
		}
		int[] idvalues = new int[ids.size()];
		
		for(int i = 0; i < ids.size(); i++)
		{
			idvalues[i] = ids.get(i).intValue();
		}
		return idvalues;	
	}
	
	/**
	 * The data hashmap stores keys like:
	 * data[LASTNAME], data[FIRSTNAME] or
	 * data[employees][LASTNAME], data[employees][FIRSTNAME] or
	 * data[0], data[1] or
	 * data[row_1][FIRSTNAME], data[row_13][FIRSTNAME], etc.
	 * This is a legacy method.
	 * Useful only for single row editing. For multi-row editing use getDataValue(int rowID, String fieldKey)
	 * @param fieldKey
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public String getDataValue(String fieldKey)
	{
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		String value = "";
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  if(mapkey.contains("[" + fieldKey + "]"))
		  {
		     value = (String)thisEntry.getValue();
		  }
		}
		return value;
	}
	/**
	 * The data hashmap stores keys like:
	 * data[LASTNAME], data[FIRSTNAME] or
	 * data[employees][LASTNAME], data[employees][FIRSTNAME] or
	 * data[0], data[1] or
	 * data[row_1][FIRSTNAME], data[row_13][FIRSTNAME], etc.
	 * Use this method when dealing with multi-row edits and you know the record/row id value.
	 * @param fieldKey
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public String getDataValue(int rowID, String fieldKey)
	{
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		String value = "";
		Entry<String,String> thisEntry = null;
		
		while (entries.hasNext())
		{
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  //NOTE: When checking our condition we add the closing square bracket to ensure
		  //we are checking for a whole number and not just part of a number.  The ending
		  //square bracket is supposed to denote the end of the row id value.i.e.: [row_12]
		  //On multiple lookups you could have a search for row_1 which is part of the string "[row_12]"
		  if(mapkey.contains("[" + Constants.IDPREFIX + String.valueOf(rowID) + "]") && mapkey.contains("[" + fieldKey + "]"))
		  {
			  value = (String)thisEntry.getValue();
		  }		  
		}
		return value;
	}
	
	/**
	 * The data hashmap stores keys like:
	 * data[row_12][employees][LASTNAME], data[row_12][employees][FIRSTNAME] or
	 * Call this method on a Join when you specifically want to
	 * ensure you have the right field.  It is possible to have fields by the same name
	 * in two different tables, so specifying both the table name and field name
	 * ensures a more accurate search on the parameter passed.
	 * This is a legacy method.  When the data hashmap contains something like the following:
	 * data[employees][LASTNAME], data[employees][FIRSTNAME]
	 * you can use this method to specify the table name and field name in the parameters list.
	 * @param id
	 * @param tableKey
	 * @param fieldKey
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public String getDataValue(int id, String tableKey, String fieldKey)
	{
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		String value = "";
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  if(mapkey.contains("[" + Constants.IDPREFIX + String.valueOf(id) + "]") && mapkey.contains("[" + tableKey + "]") && mapkey.contains("[" + fieldKey + "]"))
		  {
		     value = (String)thisEntry.getValue();
			 break;
		  }
		}
		return value;
	}
	/**
	 * The data hashmap stores keys like:
	 * data[employees][LASTNAME], data[employees][FIRSTNAME]
	 * This method is used on new records only.  That's because we don't have an ID yet to pass as a parameter to the method.
	 * The parameters passed from client side don't have an ID value.  One has to be created by the database.
	 * @param tableKey
	 * @param fieldKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getDataValue(String tableKey, String fieldKey)
	{
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		String value = "";
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  if(mapkey.contains("[" + tableKey + "]") && mapkey.contains("[" + fieldKey + "]"))
		  {
		     value = (String)thisEntry.getValue();
			 break;
		  }
		}
		return value;
	}
	
	/**
	 * Call this method when needing multiple values for a single
	 * field.  This would be for instances where we are dealing with
	 * checkboxes or select lists on the client side and multiple
	 * selection of options are permitted.
	 * The data hashmap stores keys like:
	 * data[row_12][access][0][id]	1
	 * data[row_12][access][1][id]	3
	 * data[row_12][access][2][id]	4
	 * data[row_12][department][id]	2
	 * data[row_12][employees][first_name]	Bob
	 * data[row_12][employees][last_name]	Richards
	 * 
	 * @param id
	 * @param tablename
	 * @return String[] array
	 */
	@SuppressWarnings("unchecked")
	public String[] getDataValues(int id, String tablename)
	{
		ArrayList<String> values = new ArrayList<String>();
		String[] array = null;
		Iterator<?> entries = data.entrySet().iterator();
		String mapkey = "";
		Entry<String,String> thisEntry = null;
		while (entries.hasNext()) {
		  thisEntry = (Entry<String,String>) entries.next();
		  mapkey = (String)thisEntry.getKey();
		  //NOTE: When checking our condition we add the closing square bracket to ensure
		  //we are checking for a whole number and not just part of a number.  The ending
		  //square bracket is supposed to denote the end of the row id value.i.e.: [row_12]
		  //On multiple lookups you could have a search for row_1 which is part of the string "[row_12]"
		  if(mapkey.contains("[" + Constants.IDPREFIX + String.valueOf(id) + "]") && mapkey.contains("[" + tablename + "]"))
		  {
			 //IF WE COME IN HERE, WE ARE UPDATING AN EXISTING RECORD
			 //Ignore the parameter that has the substring "many-count"
			 //We don't need it.
			 if(!mapkey.contains("many-count"))
		        values.add((String)thisEntry.getValue());
		  }
		  else if(mapkey.contains("data[0]") && mapkey.contains("[" + tablename + "]"))
		  {
			 //IF WE COME IN HERE, WE ARE PROCESSING A NEW RECORD
			 //data doesn't have row_# when processing a new record.
			 //Instead, the client side just gives us data[0]... as a parameter
			 if(!mapkey.contains("many-count"))
			     values.add((String)thisEntry.getValue()); 
		  }
		}
		if(values.size() > 0)
		{
			array = new String[values.size()];
			for(int i = 0; i < values.size(); i++)
			{
				array[i] = values.get(i);
			}
		}
		return array;
	}
	
	/**
	 * On an action process of type EDIT determine if we are dealing with a Multi-Row edit or not.
	 * @return a boolean value
	 */
	@SuppressWarnings("unchecked")
	public boolean isMultiRowEdit()
	{
		boolean result = false;
		int rownumber = -1;
		if (action.equals(Constants.EDIT))
		{
			//Iterate over the data and determine if we are dealing with multiple rows
			Iterator<?> entries = data.entrySet().iterator();
			String mapkey = "";
			Entry<String,String> thisEntry = null;
			int temp = -1;
			while (entries.hasNext()) {
			  thisEntry = (Entry<String,String>) entries.next();
			  mapkey = (String)thisEntry.getKey();
			  temp = getRowNumber(mapkey);
			  if(rownumber == -1)
			  {
			      rownumber = temp;
			  }
			  else if(rownumber != temp)
			  {
				 result = true;
				 break;
			  }
			}
		}
		return result;
	}
	
	//Given a string like: "data[row_23][firstname]" we can extract the actual row number 23
	protected int getRowNumber(String value)
	{
			int index1 = -1;
			int index2 = -1;
			String temp = "";
			index1 = value.indexOf("[");
			index2 = value.indexOf("]");
			temp = value.substring(index1 + 1, index2);
			//exclude the prefix "row_"
			return Integer.parseInt(temp.substring(Constants.IDPREFIX.length(),temp.length()));
	}
	//Given a string like: "data[row_23][firstname]" we can extract the actual field name firstname
	protected String getFieldName(String value)
	{
			int index1 = -1;
			int index2 = -1;
			String temp = "";
			index1 = value.lastIndexOf("[");
			index2 = value.lastIndexOf("]");
			temp = value.substring(index1 + 1, index2);
			return temp;		
	}
	
	
	/**
	 * Get the number of data entries
	 * @return
	 */
	public int getDataSize()
	{
		return data.size();		
	}
	/**
	 * Get all the data values
	 * @return String[] array
	 */
	public String[] getDataValues()
	{
		Collection<String> values = data.values();
		String[] items = new String[values.size()];
		Iterator<String> itr = values.iterator();
		int i = 0;
		while(itr.hasNext())
		{
			items[i] = itr.next();
			i++;
		}
		return items;
	}
	
	@SuppressWarnings("unchecked")
	public String toString()
	{
	  String params = "\nPARAMETERS RECEIVED: \n\n";
	  if(!action.isEmpty())
		  params = params + "\taction: " + action + "\n";
	  if(!upload_rowid.isEmpty())
		  params = params + "\tupload_rowid: " + upload_rowid + "\n";
	  if(draw != -1)
		  params = params + "\tdraw: " + draw + "\n";
	  if(start != -1)
		  params = params + "\tstart: " + start + "\n";
	  if(length != -1)
		  params = params + "\tlength: " + length + "\n";
	  if(!searchValue.isEmpty())
		  params = params + "\tsearchValue: " + searchValue + "\n";
	  if(searchRegex != false)
		  params = params + "\tsearchRegex: " + searchRegex + "\n";
	  if(columns.size() > 0)
	  {
	    //Iterate over the columns
		params = params + "COLUMNS:\n\n";
	    Column col = null;
	    for(int i = 0; i < columns.size(); i++)
	    {
		  col = columns.get(i);
		  params = params + "\t" + col.toString() + "\n\n";
	    }
	  }
	  if(colorders.size() > 0)
	  {
		  //Iterate over the colorders
		  params = params + "\nCOLUMN ORDERS:\n\n";
		  ColumnOrder co = null;
		  for(int i = 0; i < colorders.size(); i++)
		  {
			  co = colorders.get(i);
			  params = params + "\t" + co.toString() + "\n";
		  }
	  }
	  if(data.size() > 0)
	  {
		  //Iterate over the data
		  params = params + "DATA:\n\n";
		  Iterator<?> entries = data.entrySet().iterator();
		  String mapkey = "";
		  String value = "";
		  Entry<String,String> thisEntry = null;
		  while (entries.hasNext()) {
			  thisEntry = (Entry<String,String>) entries.next();
			  mapkey = (String)thisEntry.getKey();
			  value = (String)thisEntry.getValue();
			  params = params + "\t" + mapkey + " = " + value + "\n";
		  }
	  }
	  return params;     
	}
}


