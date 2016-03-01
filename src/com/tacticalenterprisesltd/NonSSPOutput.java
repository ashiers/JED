package com.tacticalenterprisesltd;

import java.util.*;
/**
 * This class contains the members required to return a response to the client
 * side whenever all processing (filtering, pagination, etc.) is performed on the client side.
 * An instance of this class is passed to the constructor of an instance of GSon
 * for processing as a JSON string.
 * 
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class NonSSPOutput implements BasicOutput
{
  public ArrayList<LinkedHashMap<String,Object>> data = new ArrayList<LinkedHashMap<String,Object>>();
  public LinkedHashMap<String,Object> options = new LinkedHashMap<String,Object>();
  public LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>> files =
      new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>>();
  
  public void addDataRow(LinkedHashMap<String,Object> row)
  {
	   data.add(row);
  }
  public ArrayList<LinkedHashMap<String,Object>> getData()
  {
	   return data;
  }
  
  
}


