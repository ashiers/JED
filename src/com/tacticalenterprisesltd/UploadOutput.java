package com.tacticalenterprisesltd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
/**
 * This class contains the members required to return a response to the client
 * side whenever files are uploaded to the server.
 * An instance of this class is passed to the constructor of an instance of GSon
 * for processing as a JSON string.
 * 
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class UploadOutput 
{
	public ArrayList<LinkedHashMap<String,Object>> data = new ArrayList<LinkedHashMap<String,Object>>();
	public LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>> files =
	      new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>>();
	public ResponseUpload upload = new ResponseUpload();
	public void addDataRow(LinkedHashMap<String,Object> row)
	{
	   data.add(row);
	}
	public ArrayList<LinkedHashMap<String,Object>> getData()
	{
	   return data;
	}
	  
	public class ResponseUpload
	{
	  public String id = "";
	}
}
