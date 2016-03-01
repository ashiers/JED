package com.tacticalenterprisesltd;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * This class contains the members required to return a response to the client
 * side whenever all processing (filtering, pagination, etc.) is performed on the client side.
 * An instance of this class is passed to the constructor of an instance of GSon
 * for processing as a JSON string.  More specifically, it is used only for operations
 * create a new record, edit a record, or delete a record.  Unlike NonSSPOutput, it
 * doesn't include the additional members: <i>options</i> and <i>files</i>.  For the operations indicated
 * these members are not required.
 * 
 * @author Alan Shiers
 * @version 1.5.0
 *
 */

public class CreateEditDeleteOutput implements BasicOutput
{
	  public ArrayList<LinkedHashMap<String,Object>> data = new ArrayList<LinkedHashMap<String,Object>>();
	  	  
	  public void addDataRow(LinkedHashMap<String,Object> row)
	  {
		   data.add(row);
	  }
	  public ArrayList<LinkedHashMap<String,Object>> getData()
	  {
		   return data;
	  }
}
