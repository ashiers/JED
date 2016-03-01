package com.tacticalenterprisesltd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.apache.log4j.Logger;

/**
 * The Join class is used to include additional fields from another database
 * table to the main parent table for display in DataTables. 
 * The Join class performs its own database
 * lookups based on the information you provide it.  Minimally, it requires the name of
 * the parent table and its Primary Key field.  Optionally, you can specify
 * a Join.Type. By default, this class uses a Type.STRAIGHT, which returns an
 * empty String.  Other Type options are: LEFT, RIGHT, INNER, OUTER, LEFT OUTER,
 * RIGHT OUTER. Use the enum options to access these types.  Optionally, you can also
 * affect the condition on the JOIN statement by setting a different operator
 * on the condition. By default the equals operator is used, but you can change it
 * to &lt;,&gt;,&lt;=,&gt;=, etc. <i>These options provide flexibility for convenience, however, depending on your
 * requirements the results of any query can be unknown. You can try them at your own risk!</i><br>
 * In general, the Join syntax follows this pattern as part of an SQL SELECT statement:<br>
 * <blockquote><i>Type.STRAIGHT + " JOIN " + childTableName + " ON " + parentTableName + "." + parentField + conditionOperator + childTableName + "." + childField + " ";</i></blockquote><br>
 * If using an alias for the childTable, be sure to provide the alias name as the first parameter in the constructor for the Field instance rather than
 * the actual table name.<br><br>
 * <b>Usage Example:</b><br><br>
 * <code>
 * //Parent table Primary Key<br>
 * String pKey = "ID";<br>
 * String[] tableNames = new String[]{"employees"};<br>
 * String[] fields = new String[]{"LASTNAME","FIRSTNAME","MOBILE_NUMBER","TYPE","HID","NAME"};<br>
 * ...<br> 
 * Editor editor = new Editor(db, tableNames, pKey, params);<br>
 * Join join1 = new Join(editor,tableNames[0], "mobile");<br>
 * Field field2 = new Field("mobile",fields[2], Field.Type.STRING);<br>
 * Field field3 = new Field("mobile",fields[3], Field.Type.STRING);<br>
 * //Set a validator on the fields here if needed<br>
 * join1.addField(field2);<br>
 * join1.addField(field3);<br>
 * join1.setJoin(pKey,"EMPLOYEE_ID");<br>
 * editor.addJoin(join1);<br>
 * </code><br><br>
 * There are times when you want to have on the client side a drop down list of options or
 * a set of checkboxes in your forms.  That being the case, then there is a way to obtain an array
 * of options.<br><br>
 * <code>
 * //Join the hobbies table with the employees table<br>
 * Join join2 = new Join(editor,tableNames[0],"hobbies");<br>
 * //The fields in table hobbies are read only. Table hobbies is just a lookup table.<br>
 * //Therefore, we set its fields so you can't write to them.<br>
 * Field field4 = new Field("hobbies",fields[4],Field.Type.INT);<br>
 * field4.setWrite(false);<br>
 * Field field5 = new Field("hobbies",fields[5],Field.Type.STRING);<br>
 * field5.setWrite(false);<br>
 * join2.addField(field4);<br>
 * join2.addField(field5);<br>
 * //If any inserts or updates are done, they will be done on the link table named hobbylink, not on the lookup table named hobbies.<br>
 * join2.setJoin(new String[]{pKey,"EMPLOYEE_ID"}, new String[]{"HID","HOBBY_ID"}, "hobbylink");<br>
 * editor.addJoin(join2);<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class Join
{   
  //Use the following as a guide:
  //http://www.misin.msu.edu/0/js/DataTables/extras/Editor/docs/php/class-DataTables.Editor.Join.html
  //http://editor.datatables.net/tutorials/php_join
	
  public static enum Type {LEFT {
	  public String toString()
	  {
		  return " LEFT";
	  }
  },RIGHT{
	  public String toString()
	  {
		  return " RIGHT";
	  }
  },INNER{
	  public String toString()
	  {
		  return " INNER";
	  }
  },OUTER{
	  public String toString()
	  {
		  return " OUTER";
	  }
  },LEFT_OUTER{
	  public String toString()
	  {
		  return " LEFT OUTER";
	  }
  },RIGHT_OUTER{
	  public String toString()
	  {
		  return " RIGHT OUTER";
	  }
  },STRAIGHT{
	  public String toString()
	  {
		  return "";
	  }
  }
  }; 
  

  private String parentTableName = "";
  private String childTableName = "";
  private String pField = "";
  private String[] pFields = null;
  private String cField = "";
  private String[] cFields = null;
  private ArrayList<Field> fields = null;
  private boolean canRead = true;
  private boolean canWrite = true;
  private String link_tableName = "";
  private ArrayList<Field> linkFields = null;
  private String tableAlias = "";
  private Join.Type type = Join.Type.STRAIGHT; //default
  protected boolean usingLinkTable = false;
  private String conditionOperator = "="; //default
  private Editor reference = null;
  private String pKey = "id"; //default
  private Logger logger;
  private boolean excludeOnSelect = false;
  private boolean excludeOnOutput = false;
  private String[][] results = null;
  private String strArrayQuery = "";
  private boolean uploadQuery = false;
  
    
  public Join(Editor ref, String parentTable, String childTable)
  {
	  reference = ref;
	  parentTableName = parentTable;
	  childTableName = childTable;
	  if(Editor.isLoggingEnabled())
	  {
	      logger = Logger.getLogger(Join.class);
	  }
  }
  
  public Join(Editor ref, String parentTable, String childTable, Join.Type t)
  {
	  reference = ref;
	  parentTableName = parentTable;
	  childTableName = childTable;
	  type = t;
	  if(Editor.isLoggingEnabled())
	  {
		  logger = Logger.getLogger(Join.class);
	  }
  }
  
  /**
   *  Find out if this join is to be excluded from a Select query or not
   *  @return a boolean value
   */
  public boolean getExcludeOnSelect()
  {
      return excludeOnSelect;
  }
  
  /**
   * If you are performing a query involving many joins, but you wish to exclude this join on a SELECT query, you can pass
   * a true value to the parameter of this method.
   * @param a boolean value
   */
  public void setExcludeOnSelect(boolean value)
  {
      excludeOnSelect = value;
  }
  
  /**
   * Find out if this join is to be excluded from the output JSON string or not
   * @return a boolean value
   */
  public boolean getExcludeOnOutput()
  {
      return excludeOnOutput;
  }
  /**
   * Determine if this join is set up to process String Arrays
   * @return a boolean value
   */
  public boolean isUsingLinkTable()
  {
      return usingLinkTable;
  }
  
  /**
   * Set true if you don't want this join to be included on the output JSON string during processing.
   * @param a boolean value
   */
  public void setExcludeOnOutput(boolean value)
  {
      excludeOnOutput = value;
  }
  /**
   * If your Join table has a Primary Key field you should definitely name it here.
   * @param name
   */
  public void setPrimaryKeyFieldName(String name)
  {
	  pKey = name;
  }
  /**
   * Get the name of this Joins Primary Key field
   * @return
   */
  public String getPrimaryKeyFieldName()
  {
	  return pKey;
  }
  
  /**
   * Get the child table with alias name as a String
   * if an alias is provided.
   * @return
   */
  protected String getTable()
  {
	String tbls = ""; 
	if(tableAlias.equals(""))
	{
		tbls = childTableName;
	}
	else
	{
		tbls = childTableName + " " + tableAlias;
	}
	return tbls;
  }
  
  /**
   * Set a condition operator as part of the JOIN statement.
   * The default is "=".  You can change it to 
   * &lt;,&gt;,&lt;=,&gt;=, etc.
   * @param operator
   */
  public void setConditionOperator(String operator)
  {
	  conditionOperator = operator;
  }
  /**
   * Get the condition operator
   * @return A String
   */
  public String getConditionOperator()
  {
	  return conditionOperator;
  }
  /**
   * Get the number of Fields this Join contains
   * @return an int
   */
  public int getFieldsCount()
  {
	  return fields.size();
  }
  /**
   * Set an alias name for the table in this Join.
   * @param value
   */
  public void setTableAlias(String ChildTableName, String alias)
  {
	  childTableName = ChildTableName;
	  tableAlias = alias;
  }
  /**
   * Get the alias name for the table in this Join.
   * @return
   */
  public String getTableAlias()
  {
	  return tableAlias;
  }
  /**
   * Add a Field for the LINK table
   * @param field
   */
  public void addLinkTableField(Field field)
  {
	  if(linkFields == null)
	  {
		  linkFields = new ArrayList<Field>();		  
	  }
	  linkFields.add(field);
  }
  /**
   * Discover is this Join has LINK table fields.
   * @return
   */
  public boolean haveLinkTableFields()
  {
	  boolean gottem = false;
	  if(linkFields != null)
	  {
		  gottem = true;		  
	  }
	  return gottem;
  }
  
  /**
   * Get an array of type Field of all the fields in the LINK table
   * @return
   */
  public Field[] getLinkTableFields()
  {
	  Field[] flds = new Field[linkFields.size()];
	  for(int i = 0; i < linkFields.size(); i++)
	  {
		  flds[i] = linkFields.get(i);
	  }
	  return flds;
  }
  
  /**
   * Add a Field to be included in the SELECT query.
   * @param field
   */
  public void addField(Field field)
  {
	  if(fields == null)
	  {
		  fields = new ArrayList<Field>();
	  }
	  fields.add(field);
  }
  /**
   * Get all the Fields that are contained in this Join
   * @return A Field[] array
   */
  public Field[] getFields()
  {
	Field[] flds = new Field[fields.size()]; 
	for(int i = 0; i < fields.size(); i++)
	{
		flds[i] = fields.get(i);
	}
    return flds;
  }
  /**
   * Discover if this join has fields loaded yet.
   * @return
   */
  public boolean haveFields()
  {
	  boolean gottem = false;
	  if(fields != null)
	  {
		  gottem = true;
	  }
	  return gottem;
  }
  
  /**
   * By default you can read from and write to a Join.
   * To not read from this Join, set it to false. 
   * @param value
   */
  public void setCanRead(boolean value)
  {
	  canRead = value;
  }
  /**
   * Inquire if you can read from this Join.
   * @return A boolean value.
   */
  public boolean getCanRead()
  {
	  return canRead;
  }
  /**
   * By default you can read from and write to a Join.
   * To not write to this Join, set it to false.
   * @param value
   */
  public void setCanWrite(boolean value)
  {
	  canWrite = value;
  }
  /**
   * Inquire if you can write to this Join
   * @return
   */
  public boolean getCanWrite()
  {
	  return canWrite;
  }
  /**
   * This method will perform a lookup on the database based on the id provided and
   * use the result to set the values for the fields.
   * @param db
   * @param id
   */
  protected void setFieldValues(Database db, String id)
  {
	  String flds = "";
	  Field field = null;
	  for(int i = 0; i < fields.size(); i++)
	  {
		field = fields.get(i);
		flds += field.toString() + ",";
	  }
	  flds = flds.substring(0,flds.length() - 1);
	  String query = "SELECT " + flds + " FROM " + childTableName + " WHERE " + childTableName + "." + pKey + "=" + id;
	  if(Editor.isLoggingEnabled())
			 logger.debug("Join.setFieldvalues - query: " + query);
	  String[][] result = db.executeSelect(query);
	  
	  for(int i = 0; i < result.length; i++)
	  {		  
		  for(int j = 0; j < result[0].length; j++)
		  {
			  field = fields.get(j);
			  field.setValue(result[i][j], Field.DIRECTION.FROM_DB);
		  }
	  }
  }
  
  /**
   * Set the parameters required to complete the JOIN portion of the SELECT query.
   * Use this method when you simply want to set an Object whereby you have a direct
   * ONE-TO-ONE relationship between the parent table and the child table in the database.
   * @param parentField
   * @param childField
   */
  public void setJoin(String parentField, String childField)
  {
	  pField = parentField;
	  cField = childField;	  
  }  
  /**
   * Set the parameters required to complete the JOIN portion of the SELECT query.
   * Use this method when you want to set an Array of options whereby you have a third LINK TABLE
   * between the parent and child tables. This type of relationship generally denotes
   * a ONE-T0-MANY relationship in a database. The Array it produces usually signifies
   * any number of options provided for a drop-down select box or a set of checkboxes
   * or radio buttons on the client side interface.
   * @param parentFields
   * @param childFields
   * @param link_table
   */
  public void setLinkJoin(String[] parentFields, String[] childFields, String link_table)
  {
	  pFields = parentFields;
	  cFields = childFields;
	  cField = cFields[0];
	  link_tableName = link_table;
	  usingLinkTable = true;
  }
  
  protected void runJoinSelectQueryForStringArrays(Database db)
  {
      String flds = "";
      for (int i = 0; i < fields.size(); i++)
      {
          flds += fields.get(i).toString() + ",";
      }
      flds = flds.substring(0, flds.length() - 1);
      strArrayQuery = "SELECT " + parentTableName + "." + reference.getPrimaryKey() + "," + flds + " FROM " + parentTableName + " " + toString() + " ORDER BY " + parentTableName + "." + reference.getPrimaryKey();
      logger.debug("Join.runJoinSelectQueryForStringArrays - query: " + strArrayQuery);
      results = db.executeSelect(strArrayQuery);            
  }
  
  protected void runJoinSelectQueryForUploads(Database db)
  {
      strArrayQuery = "SELECT " + parentTableName + "." + reference.getPrimaryKey() + "," + childTableName + "." + pKey + " FROM " + parentTableName + " " + toString() + " ORDER BY " + parentTableName + "." + reference.getPrimaryKey();
      logger.debug("Join.runJoinSelectQueryForUploads - query: " + strArrayQuery);
      results = db.executeSelect(strArrayQuery); 
      uploadQuery = true;
  }
  
  

  protected String[][] getResultsOfJoinSelectQuery()
  {
      return results;
  }
  
  
  //Editor sets the values of the fields in this Join class prior to calling this method, assuming
  //that each field's write member is true. 
  protected void update(Database db, int id, Parameters params)
  {	
	try{
	  if(usingLinkTable)
	  {
		  /*since we are dealing with a link table, and
		   there could potentially be multiple instances of the parents
		   primary key value within the table, to perform an update we
		   need to first perform a delete of all records having the parent
		   tables primary key.
		  */
		  int[] ids = new int[1];
		  ids[0] = id;
	      delete(db,ids);
	      //Then we insert the appropriate values using insert queries.
	      insert(db, id, params);
	  }
	  else
	  {
		  //Determine first if there is a record with the id value in the Child Table.
		  //If there isn't, then we need to do an INSERT not an UPDATE.
		  String strQuery = "SELECT COUNT(*) FROM " + childTableName + " WHERE " + cField + "=" + String.valueOf(id);		  
		  String[][] response = db.executeSelect(strQuery);
		  int resp = Integer.parseInt(response[0][0]);
		  if(resp == 0)
		  {
			 insert(db, id, params); 
		  }
		  else
		  {
		    Query query = new Query(Query.Type.UPDATE, childTableName);
		    query.setFields(this.getFields());		  
		    Field pkfield = new Field(childTableName,cField,Field.Type.INT);
		    WhereCondition[] where = new WhereCondition[1];
		    where[0] = new WhereCondition(pkfield, id, "=");
		    query.setWhereConditions(where);
		    if(Editor.isLoggingEnabled())
				 logger.debug("Join.update - query: " + query.toString());
		    db.setQuery(query);
		    db.executeInsertUpdate();
		  }
	  }
	}
	catch(Exception e)
	{
		if(Editor.isLoggingEnabled())
		{
	      logger.error(Editor.getFullStackTrace(e));
		}
	}
  }
  /*
  protected void appendInsertUpdateOutput(Database db, BasicOutput out, Parameters params)
  {
	try{
	  if(usingStringArrays)
	  {
		//The Child table fields will not have any values set because they are READ ONLY!
		//We have to provide the values based on what was sent from the client side and what
		//values already exist in the child table.
		ArrayList<LinkedHashMap<String,Object>>  list = new ArrayList<LinkedHashMap<String,Object>>();
		String[] values = params.getDataValues(childTableName);
		//Lookup all field values in the Child table
		String flds = "";
		for(int i = 0; i < fields.size(); i++)
		{
			flds += fields.get(i).toString() + ",";
		}
		flds = flds.substring(0,flds.length() - 1);
		String query = "SELECT " + flds + " FROM " + childTableName;	
		if(Editor.isLoggingEnabled())
			 logger.debug("Join.appendInsertUpdateOutput - query: " + query);
		String[][] results = db.executeSelect(query, fields.size());
		Field field = null;		
		LinkedHashMap<String,Object> map = null;
		for(int i = 0; i < values.length; i++)
		{
			map = new LinkedHashMap<String,Object>();
			for(int j = 0; j < fields.size(); j++)
			{
				field = fields.get(j);
				if(field.getExcludeOnOutput() == false)
				{
				  if(j == 0)
				  {
					map.put(field.getName(), values[i]);
				  }
				  else
				  {
					for(int k = 0; k < results.length; k++)
					{
					  if(results[k][0].equals(values[i]))	
					     map.put(field.getName(), results[k][1]);
					}
				  }
				}
			}
			list.add(map);
		}
		LinkedHashMap<String,Object> lhm = new LinkedHashMap<String,Object>();
		lhm.put(childTableName,list);
		out.addDataRow(lhm);
	  }
	  else
	  {
	    Field field = null;
	    LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
	    for(int i = 0; i < fields.size(); i++)
	    {
		  field = fields.get(i);
		  if(field.getExcludeOnOutput() == false)
		  {
		    if(field.getFieldType() == Field.Type.DATE)
		    {
			  map.put(field.getName(), field.getClientDateValue());			
		    }
		    else
		    {
			  map.put(field.getName(), field.getValue()); 		    
		    }
		  }
	    }
	    
	    if(tableAlias.equals(""))
	    {	
	       LinkedHashMap<String,Object> lhm = new LinkedHashMap<String,Object>();
		   lhm.put(childTableName,map);
		   out.addDataRow(lhm);
	    }
	    else
	    {
	       LinkedHashMap<String,Object> lhm = new LinkedHashMap<String,Object>();
		   lhm.put(tableAlias,map);
		   out.addDataRow(lhm);
	    }
	  }
	}
	catch(Exception e)
	{
		if(Editor.isLoggingEnabled())
		{
	      logger.error(Editor.getFullStackTrace(e));
		}
	}
  }
  */
  
  protected void insert(Database db, int id, Parameters params)
  {
	try{  
	  if(usingLinkTable)
	  {
		  Query query = new Query(Query.Type.INSERT, link_tableName);
		  query.setAsLinkTable(true);
		  Field linkParentField = new Field(link_tableName, pFields[1],Field.Type.INT);
		  Field linkChildField = new Field(link_tableName,cFields[1],Field.Type.INT);
		  Field[] fields = new Field[2];
		  fields[0] = linkParentField;
		  fields[1] = linkChildField;		  
		  String[] values = params.getDataValues(id, childTableName);
		  if(values != null)
		  {
		    for(int i = 0; i < values.length; i++)
		    {
			  //Set the values for each field
			  linkParentField.setValue(String.valueOf(id), Field.DIRECTION.FROM_CLIENT);
			  linkChildField.setValue(values[i], Field.DIRECTION.FROM_CLIENT);
		      query.setFields(fields);
		      db.setQuery(query);
			  db.executeInsertUpdate();
			  if(Editor.isLoggingEnabled())
				 logger.debug("Join.insert - query: " + query);
		    }	
		  }
	  }
	  else
	  {
		  Query query = new Query(Query.Type.INSERT, childTableName);
		  Field foreignKeyField = new Field(childTableName, cField, Field.Type.INT);
		  foreignKeyField.setValue(String.valueOf(id),Field.DIRECTION.FROM_CLIENT);
		  Field[] flds = new Field[fields.size() + 1];
		  flds[0] = foreignKeyField;
		  int count = 1;
		  for(int i = 0; i < fields.size(); i++)
		  {
			 flds[count] = fields.get(i);
			 count++;
		  }
		  query.setFields(flds);
		  db.setQuery(query);
		  db.executeInsertUpdate();
		  if(Editor.isLoggingEnabled())
				 logger.debug("Join.insert - query: " + query);
		  
	  }
	}
	catch(Exception e)
	{
		if(Editor.isLoggingEnabled())
		{
	      logger.error(Editor.getFullStackTrace(e));
		}
	}
  }
  
  protected void delete(Database db,int[] ids)
  {
	  try{
	    if(usingLinkTable)
	    {
		  Query[] queries = new Query[ids.length];
		  Query query = null;
		  Field linkParentField = new Field(link_tableName, pFields[1],Field.Type.INT);
		  for(int i = 0; i < ids.length; i++)
		  {
			query = new Query(Query.Type.DELETE,link_tableName);
			WhereCondition[] where = new WhereCondition[1];
			where[0] = new WhereCondition(linkParentField, ids[i], "=");
			query.setWhereConditions(where);
			queries[i] = query;
		  }
		  db.executeDeletes(queries); 		  		  
	    }
	    else
	    {
		  Query[] queries = new Query[ids.length];
		  Query query = null;
		  Field ParentIDField = new Field(childTableName, cField, Field.Type.INT);
		  for(int i = 0; i < ids.length; i++)
		  {
			query = new Query(Query.Type.DELETE,childTableName);
			WhereCondition[] where = new WhereCondition[1];
			where[0] = new WhereCondition(ParentIDField, ids[i], "=");
			query.setWhereConditions(where);
			queries[i] = query;			
		  }
		  db.executeDeletes(queries);		  
	    }
	  }
	  catch(Exception e)
	  {
		  if(Editor.isLoggingEnabled())
			{
		      logger.error(Editor.getFullStackTrace(e));
			}
	  }
  }
  
 
  //Check out this article on binary search by Josh Block:
  //http://googleresearch.blogspot.ca/2006/06/extra-extra-read-all-about-it-nearly.html
  //A negative value will be return if the key is not found!
  private int binarySearch(String[][] data, int key)
  {
	  int low = 0;
	  try{  
	  int high = data.length - 1;
	  int mid = -1;
	  int midVal = -1;
	  
	    while(low <= high)
	    {
		  //mid = low + ((high - low) / 2); //Another optional fix to the bug
		  mid = (low + high) >>> 1;
		  midVal = Integer.parseInt(data[mid][0]);
		  if (midVal < key)
		    low = mid + 1;
		  else if (midVal > key)
			high = mid - 1;
		  else
			return mid; // key found
	    }	    
	  }
	  catch(Exception e)
	  {
		  if(Editor.isLoggingEnabled())
		  {
		      logger.error(Editor.getFullStackTrace(e));
		  }
	  }
	  return -(low + 1);  // key not found.
  }
  //Check out this article on binary search by Josh Block:
  //http://googleresearch.blogspot.ca/2006/06/extra-extra-read-all-about-it-nearly.html
  //A negative value will be return if the key is not found!
  private int binarySearch(String[][] data, int key, int lowerbound, int upperbound)
  {
	  int low = lowerbound;
	  int high = upperbound;
	  int mid = -1;
	  int midVal = -1;
	  try{
	    while(low <= high)
	    {
		  //mid = low + ((high - low) / 2); //Another optional fix to the bug
		  mid = (low + high) >>> 1;
		  midVal = Integer.parseInt(data[mid][0]);
		  if (midVal < key)
		    low = mid + 1;
		  else if (midVal > key)
			high = mid - 1;
		  else
			return mid; // key found
	    }	    
	  }
	  catch(NumberFormatException nfe)
	  {
		  if(Editor.isLoggingEnabled())
		  {
		      logger.error(Editor.getFullStackTrace(nfe));
		  }
	  }
	  return -(low + 1);  // key not found.
  }
  
  //The 2D array data should be in order by the first column of ids, therefore, we can 
  //perform a binary search to find what we need that much faster.
  protected ArrayList<LinkedHashMap<String,Object>> getArrayFieldValues(String id, String[][] data)throws NullPointerException
  {	  
	  if(data == null)
      {
          throw new NullPointerException("Join.getArrayFieldValues(String id, String [,] data) - The data parameter is null.\n" + 
                  "Ensure that the names of the parent table, child table, and link table are correct.\n" + 
                  "A query is generally processed prior to this method being called. If the query fails, it is generally due to a mispelling of a table or field.\n" +
                  "Following is the query that was attempted: \n\n" + strArrayQuery + "\n\n");
      }
	  
	  /*Print the data
	  for(int i = 0; i < data.length; i++)
	  {
		  for(int j = 0; j < data[0].length; j++)
		  {
			  System.out.print("data[" + i + "][" + j + "]: " + data[i][j] + " | ");
		  }
		  System.out.println();
	  }
	  */
	  ArrayList<LinkedHashMap<String,Object>> list = new ArrayList<LinkedHashMap<String,Object>>();
	  
	  if(uploadQuery)
	  {
		  int index = 0;
		  //int count = 0;
		  LinkedHashMap<String,Object> temp = null; 
		  index = binarySearch(data, Integer.parseInt(id));
		          
		  if(index > -1)
		  {
			//Get this particular one
			temp = new LinkedHashMap<String,Object>();
			for(int j = 1; j < data[0].length; j++)
			{
			  temp.put(pKey,data[index][j]);
			  //count++;			    
			}
			//count = 0;//reset
			list.add(temp);
			  
			// search left		  
			for(int i = index -1; i >= 0; i--)
			{
			  if(data[i][0].equals(id))
			  {
				  temp = new LinkedHashMap<String,Object>();
			      for(int j = 1; j < data[0].length; j++)
				  {
				     temp.put(pKey,data[i][j]);
				     //count++;			    
				  }
				  //count = 0;//reset
				  list.add(temp);
			  }
			  else
				  break;
			}
			  		  
		    // search right
			for(int i = index + 1; i < data.length; i++)
			{
			  if(data[i][0].equals(id))
			  {
				  temp = new LinkedHashMap<String,Object>();
			      for(int j = 1; j < data[0].length; j++)
				  {
				     temp.put(pKey,data[i][j]);
				     //count++;			    
				  }
				  //count = 0;//reset
				  list.add(temp);
			  }
			  else
				  break;
			}
		  }  
	  }
	  else
	  {
	    
	    int index = 0;
	    int count = 0;
	    LinkedHashMap<String,Object> temp = null; 
	    index = binarySearch(data, Integer.parseInt(id));
	          
	    if(index > -1)
	    {
		  //Get this particular one
		  temp = new LinkedHashMap<String,Object>();
		  for(int j = 1; j < data[0].length; j++)
		  {
		    temp.put(fields.get(count).getName(),data[index][j]);
		    count++;			    
		  }
		  count = 0;//reset
		  list.add(temp);
		  
		  // search left		  
		  for(int i = index -1; i >= 0; i--)
		  {
			  if(data[i][0].equals(id))
			  {
				  temp = new LinkedHashMap<String,Object>();
		          for(int j = 1; j < data[0].length; j++)
		   		  {
		   		     temp.put(fields.get(count).getName(),data[i][j]);
		   		     count++;			    
		   		  }
		   		  count = 0;//reset
		   		  list.add(temp);
			  }
			  else
				  break;
		  }
		  		  
	      // search right
		  for(int i = index + 1; i < data.length; i++)
		  {
			  if(data[i][0].equals(id))
			  {
				  temp = new LinkedHashMap<String,Object>();
		          for(int j = 1; j < data[0].length; j++)
		   		  {
		   		     temp.put(fields.get(count).getName(),data[i][j]);
		   		     count++;			    
		   		  }
		   		  count = 0;//reset
		   		  list.add(temp);
			  }
			  else
				  break;
		  }
	    }
	  }
	  
	  return list;
  }
  /**
   * Set the name of the Parent table.
   * @param value
   */
  public void setParentTableName(String value)
  {
	  parentTableName = value;
  }
  /**
   * Get the name of the Parent table.
   * @return A String
   */
  public String getParentTableName()
  {
	  return parentTableName;
  }
  /**
   * Get the name of the Child tables primary key field.
   * @return A String
   */
  public String getChildField()
  {
	  return cField;
  }
  /**
   * Set the name of the Child Table.
   * @param value
   */
  public void setChildTableName(String value)
  {
	  childTableName = value;
  }
  /**
   * Get the name of the Child table.
   * @return A String
   */
  public String getChildTableName()
  {
	  return childTableName;
  }  
  
  /**
   * Set the name of the LINK table between the
   * Parent and Child tables.
   * @param value
   */
  public void setLinkTableName(String value)
  {
	  link_tableName = value;
	  
  }
  /**
   * Get the name of the LINK table between the
   * Parent and Child tables.
   * @return
   */
  public String getLinkTableName()
  {
	  return link_tableName;
  }
  /**
   * Set the Join Type.
   * @param t
   */
  public void setJoinType(Join.Type t)
  {
	  type = t;
  }
  /**
   * Get the Join Type.
   * @return
   */
  public Join.Type getJoinType()
  {
	  return type;
  }
  /**
   * Get the Join condition produced by this Join class.  
   * @return A String
   */
  public String toString()
  {
	  String strJoin = "";
	  if(usingLinkTable)
	  {  //This part takes care of the scenario where we are dealing with a link table
		 //between the parent and child tables.  A ONE-TO-MANY relationship.
		 strJoin = type.toString() + " JOIN " + link_tableName + " ON " + parentTableName + "." + pFields[0] + conditionOperator + link_tableName + "." + pFields[1] + " " +
			           type.toString() + " JOIN " + childTableName + " ON " + childTableName + "." + cFields[0] + conditionOperator + link_tableName + "." + cFields[1] + " " ;
		 
	  }
	  else
	  {  //This part takes care of just two tables joined together. A ONE-TO-ONE relationship.
		 if(tableAlias.equals(""))
		    strJoin = type.toString() + " JOIN " + childTableName + " ON " + parentTableName + "." + pField + conditionOperator + childTableName + "." + cField + " ";
		 else
			strJoin = type.toString() + " JOIN " + childTableName + " " + tableAlias + " ON " + parentTableName + "." + pField + conditionOperator + tableAlias + "." + cField + " "; 
	  }
	  return strJoin;
  }
  
  
}


