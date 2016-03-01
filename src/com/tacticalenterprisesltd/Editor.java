package com.tacticalenterprisesltd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import com.google.gson.*;
import org.apache.log4j.Logger;

/**
 * DataTables Editor base class for creating editable tables.
 *
 * Editor class instances are capable of servicing all of the requests that
 * DataTables and Editor will make from the client-side - specifically:<br>
 * -   Get data<br>
 * -   Create new record<br>
 * -   Edit existing records<br>
 * -   Delete existing records<br><br>
 *
 * This being the Java version of server side classes supporting DataTables,
 * these classes will work strictly using Objects with the syntax:<br>
 * <i>{"key1":"value1","key2":"value2","key3":{"subkey1":"value3","subkey2":"value4"}} or</i><br>
 * <i>{"key1":"value1","key2":"value2","key3":[{"subkey1":"value3","subkey2":"value4"}]}</i><br>
 * For the purposes of providing inserts and updates of data, this syntax
 * is mostly used and is best suited for OOP (Object Oriented Programming).<br><br> 
 * On the client side and in PHP the concept of associative arrays does not exist in Java.
 * The closest data structure resembling the associative array is the HashMap.  In fact,
 * to ensure that elements placed in HashMap came out in the same order while being processed
 * by GSON, I had to resort to using a special version of HashMap called the LinkedHashMap.
 * Therefore, I use the LinkedHashMap extensively throughout this library of classes.<br><br>
 * The Editor instance is configured with information regarding the
 * database table fields that you wish to make editable, and other information
 * needed to read and write to the database (table name for example!).<br><br>
 * 
 * Usage Example: A very basic example of using Editor to create a table with four fields.
 *    This is all that is needed on the server-side to create an editable
 *    table. The code below would be used in a Java Server Page or could be used
 *    even in a Servlet.<br><br>
 * <code>   
 * String dbName = "fcs_db";<br>
 * String[] tableNames = new String[]{"employees"};<br>
 * String pKey = "ID";<br>
 * String[] fields = new String[]{"LASTNAME","FIRSTNAME","TITLE","EMAIL_ADDRESS"};<br>
 * Database db = new Database(dbName);<br>
 * Editor editor = new Editor(db, tableNames, pKey, params);<br>
 * Field field0 = new Field(tableNames[0],fields[0],Field.Type.STRING);<br>
 * field0.setValidator(new Validate(Validate.Type.REQUIRED));<br>
 * editor.addField(field0);<br>
 * Field field1 = new Field(tableNames[0],fields[1],Field.Type.STRING);<br>
 * field1.setValidator(new Validate(Validate.Type.REQUIRED));<br>
 * editor.addField(field1);<br>
 * Field field2 = new Field(tableNames[0],fields[2],Field.Type.STRING);<br>
 * editor.addField(field2);<br>
 * Field field3 = new Field(tableNames[0],fields[3],Field.Type.STRING);<br>
 * field3.setValidator(new Validate(Validate.Type.EMAIL_REQUIRED));<br>
 * editor.addField(field3);<br>
 * editor.Process();<br>
 * out.println(editor.toJSONString());<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 */

public class Editor 
{
  /** params - encapsulates all the parameters passed from the client side */ 
  private Parameters params = null;
  /** db - A Database object used to interact with the database. */
  private Database db = null;
  /** query - A Query object used to assemble a query string for the database.*/
  private Query query = null;
  private ArrayList<Field> fields = null;
  /** not implemented yet */
  private ArrayList<Join> joins = new ArrayList<Join>();
  /** Primary Key: default is "id" */
  protected String pKey = "id";
  /** table = a String array of table names */
  private String table = null;
  /** where - an ArrayList of Where objects that will be used to filter records on a Query. */
  private ArrayList<WhereCondition> where = new ArrayList<WhereCondition>();
  private WhereConditionGroups whereGroups = null;
  /** orders - an ArrayList of Order objects that will be used to sort records on a Query. */
  private ArrayList<Order> orders = new ArrayList<Order>();
  
  /**
   * data - A 2D String[][] array containing all the data for every row in the query. 
   */
  private String[][] data = null;
  protected boolean usingSSP = false;
  /**
   * For SSP (server side processing), including the DT_RowId is optional.
   * If you set this to <i>true</i>, the primary key value for each record will be used. 
   */
  protected boolean includeRowID = false; 
  /**
   * For SSP (server side processing), including the DT_RowClass is optional.
   * If you set this to <i>true</i>, a DT_RowClass field will be included in
   * the JSON string. If using this option, optionally provide a prefix by setting
   * rowclassPrefix which will be appended to the value of an existing field, which
   * you indicate by setting the field name for rowclassFieldReference.
   */
  private boolean includeRowClass = false; 
  private String rowclassPrefix = "";
  private String rowclassFieldReference = "";
  private String parentTableAlias = "";  
  protected Object output = null;
  private boolean setJoinFields = false;
  private Field conditionField = null;
  private String parameterKey = "";
  private Logger logger;
  private static boolean loggingEnabled = true;
  protected boolean haveUploadInstance = false;
  private NonSSPOutput nsspout = new NonSSPOutput();
  private SSPOutput sspout = new SSPOutput();
  private boolean haveOrder = false;
  private boolean disableDeletes = false;
  private boolean disableInserts = false;
  private boolean disableUpdates = false;
  private boolean disableSelects = false;
  private boolean disableUploads = false;
  
  /**
   * Constructor for Editor
   * @param database
   * @param tableName
   * @param primaryKey
   */
  public Editor (Database database, String tableName, String primaryKey)
  {
    db = database;
    db.setEditorReference(this);
    table = tableName;
    pKey = primaryKey;
    logger = Logger.getLogger(Editor.class);    
  }
  /**
   * Constructor for Editor
   * @param database
   * @param tableName
   * @param primaryKey
   * @param parameters
   */
  public Editor (Database database, String tableName, String primaryKey, Parameters parameters)
  {
    db = database;
    db.setEditorReference(this);
    table = tableName;
    pKey = primaryKey;
    params = parameters;
    logger = Logger.getLogger(Editor.class);    
  }  
  
  
  public static String  getFullStackTrace(Throwable throwable) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      throwable.printStackTrace(pw);
      return sw.toString();
 }
  
  /**
   * To provide further flexibility,
   * you can toggle Editors ability to perform Uploads.
   * @param value
   */
  public void disableUploads(boolean value)
  {
	  disableUploads = value;
  }
  
  /**
   * To provide further flexibility,
   * you can toggle Editors ability to perform Deletes.
   * @param value
   */
  public void disableDeletes(boolean value)
  {
	  disableDeletes = value;
  }
  /**
   * To provide further flexibility,
   * you can toggle Editors ability to perform Inserts.
   * @param value
   */
  public void disableInserts(boolean value)
  {
	  disableInserts = value;
  }
  /**
   * To provide further flexibility,
   * you can toggle Editors ability to perform Updates.
   * @param value
   */
  public void disableUpdates(boolean value)
  {
	  disableUpdates = value;
  }
  /**
   * To provide further flexibility,
   * you can toggle Editors ability to perform Selects.
   * @param value
   */
  public void disableSelects(boolean value)
  {
	  disableSelects = value;
  }   
  
  protected static boolean isLoggingEnabled()
  {
	  return loggingEnabled;
  }
  
  /**
   * If you wish to disable logging call this method
   * @param propertiesFile
   */
  public static void disableLogging()
  {
	  loggingEnabled = false;	  
  }
  
  /**
   * 
   * Use this method on a Join when a Join has been flagged as READ ONLY. This means you've set its canWrite property to false and
   * don't want to write to the database.
   * However, in order to get any output from the join, you need to perform a lookup on the database in order to set the 
   * values on the Join fields during an Update operation.  This method isn't used much, but comes in handy when
   * you have a self referencing table.
   * @param value Set to true when wanting to set the Join fields with values
   * @param field which will become part of a WhereCondition.
   * @param paramKey This would be a key used to perform a Parameter lookup to obtain a value for the field.
   */
  public void setJoinFieldsOnUpdateAndInsert(boolean value, Field field, String paramKey)
  {
	 setJoinFields = value; 
	 conditionField = field;
	 parameterKey = paramKey;
  }
  
  private void setJoinFields(Join join, String conditionFieldValue )
  {
	 conditionField.setValue(conditionFieldValue, Field.DIRECTION.FROM_CLIENT);
	 Query query = new Query(Query.Type.SELECT);
	 WhereCondition[] wcs = new WhereCondition[1];
	 wcs[0] = new WhereCondition(conditionField, conditionField.getValue(), "=");
	 query.setWhereConditions(wcs);
	 query.setTable(join.getTable());
	 query.setFields(join.getFields());
	 db.setQuery(query);
	 if(loggingEnabled)
		 logger.debug("Editor.setJoinFields - query: " + query);
	 String[][] result = db.executeSelect();
	 Field[] fields = join.getFields();
	 for(int i = 0; i < result.length; i++)
	 {		 
		 for(int j = 0; j < result[0].length; j++)
		 {
			fields[j].setValue(result[i][j], Field.DIRECTION.FROM_CLIENT); 
		 } 
	 }
  }
  
  /**
   * Set an Order
   * @param value
   */
  public void addOrder(Order value)
  {
	  orders.add(value);
	  haveOrder = true;
  }
  /**
   * Determine if we have an Order object
   * @return
   */
  public boolean haveOrder()
  {
	  return haveOrder;
  }
  
    
  /**
   * Inquire if there are any instances of the Join class.
   * @return
   */
  protected boolean haveJoins()
  {
	  if(joins.size() > 0)
		  return true;
	  return false;
  }
  /**
   * Set an alias for the Parent Table.
   * @param value
   */
  public void setAliasParentTableName(String value)
  {
	  parentTableAlias = value;
  }
  /**
   * Get the alias of the Parent table	  
   * @return
   */
  public String getAliasParentTableName()
  {
	return parentTableAlias;
  }
  /**
   * Indicate that you want to include the Primary Key column
   * on the Parent table as part of your query.
   * @param value
   */
  public void IncludeRowId(boolean value)
  {
	  includeRowID = value;
  }
  /**
   * Inquire if the Primary Key column of the Parent table
   * has been set.
   * @return
   */
  public boolean IncludeRowId()
  {
	  return includeRowID;
  }
  /**
   * Indicate that you want to include a RowClass column
   * on the Parent table as part of your query. If setting 
   * to true, with this you will also need to call methods:
   * setRowclassPrefix(...) and setRowclassFieldReference(...).
   * @param value
   */
  public void IncludeRowClass(boolean value)
  {
	  includeRowClass = value;
  }
  /**
   * Inquire if RowClass has been included as part of the Parent table.
   * @return boolean
   */
  public boolean IncludeRowClass()
  {
	  return includeRowClass;
  }
  /**
   * If you set IncludeRowClass to true, you will need to
   * also set a prefix for the value that is returned.
   * @param value
   */
  public  void setRowclassPrefix(String value)
  {
	  rowclassPrefix = value;
  }
  /**
   * Get the RowClass prefix being used on the value returned.
   * @return
   */
  public String getRowclassPrefix()
  {
	  return rowclassPrefix;
  }
  /**
   * If you set IncludeRowClass to true, you will need to
   * also set a reference to one of the Fields.
   * @param value
   */
  public void setRowclassFieldReference(String value)
  {
	  rowclassFieldReference = value;
  }
  /**
   * Get the name of the Field being referenced for the RowClass.
   * @return
   */
  public String getRowclassFieldReference()
  {
	  return rowclassFieldReference;
  }
  /**
   * Set the Paramenters.
   * @param p
   */
  public void setParameters(Parameters p)
  {
	  params = p;
  }
  /**
   * Get the Parameters
   * @return Parameters
   */
  public Parameters getParameters()
  {
	  return params;
  }
  
  /**
   * A convenience method to set an instance of the Database if one
   * wasn't set in the constructor of this class.
   * @param database
   */
  public void setDatabase(Database database)
  {
    db = database;
  }
  /**
   * Get the instance of the Database.
   * @return Database
   */
  protected Database getDatabase()
  {
    return db;
  }
  /**
   * Set all the Fields required for the query on the database.
   * @param flds
   */
  public void setFields(ArrayList<Field> flds)
  {
    fields = flds;
  }
  /**
   * Add a Field required for the query on the database.
   * @param field
   */
  public void addField(Field field)
  {
	  if(fields == null)
	  {
		  fields = new ArrayList<Field>();
	  }
	  if(field.hasUpload())
		 haveUploadInstance = true;
	  fields.add(field);
  }
  /**
   * Get all the fields.
   * @return
   */
  public ArrayList<Field> getFields()
  {
    return fields;
  }
  /**
   * Add a Join instance.
   * @param j
   */
  public void addJoin(Join j)
  {
    joins.add(j);
  }
  /**
   * Get all the Joins.
   * @return
   */
  public Join[] getJoins()
  {
	Join[] jns = new Join[joins.size()];
    for(int i = 0; i < joins.size(); i++)
    {
    	jns[i] = joins.get(i);
    }
    return jns;
  }
  /**
   * Call this method to obtain a JSON string representation of any of the 
   * output classes: CreateEditOutput, FieldErrorsOutput, NonSSPOutput, SSPOutput.
   * @return A JSON String
   */
  public String toJSONString()
  {
	String out = "";  
	if(output != null)
	{
	  //Ordinarily we would create an instance of GSON like this: Gson gson = new Gson();
	  //But this time we want GSON to NOT IGNORE fields that have a null value.
	  //The Field of type IMAGE can contain a null value and we don't want it to be ignored.
	  Gson gson = new GsonBuilder().serializeNulls().create();
	  
	  out = gson.toJson(output);
	  	  
	}
	
    return out;
  }
  /**
   * Set the Parent table name.
   * @param value
   */
  public void setTableName(String value)
  {
    table = value;
  }
  /**
   * Get the Parent table name.
   * @return
   */
  public String getTableName()
  {
    return table;
  }
  /**
   * Set the Primary Key column name on the Parent table.
   * @param value
   */
  public void setPrimaryKey(String value)
  {
    pKey = value;
  }
  /**
   * Get the Primary Key column name on the Parent table.
   * @return
   */
  public String getPrimaryKey()
  {
    return pKey;
  }
  
  private boolean allFieldsValid()
  {
	  boolean ok = true;
	  ValidationMessage vm = null;
	  Field field = null;
	  //Now, check that the value provided is valid on each field
	  for(int i = 0; i < fields.size(); i++)
	  {
		 field = fields.get(i);
		 if(field.canWriteDataToDatabase())
		 {
		   if(field.hasValidator())
		   {
			 if(field.getFieldType() == Field.Type.DATE)
			 {
			   vm = field.getValidator().isValid(field.getClientDateValue());
			 }
			 else
			 {
			   vm = field.getValidator().isValid(field.getValue().toString());
			 }
			 if(vm.isValid() == false)
			 {
				//Set the fieldsError for the output
				FieldErrorsOutput err = new FieldErrorsOutput();
				LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
				map.put("name", field.getDBFieldName());
				map.put("status", vm.getMessage());
				err.fieldErrors.add(map);
				output = err;
				ok = false;
				break;
			 }
		   }
		 }
	  }
	  if(haveJoins())
	  {
		  //Check the fields in the Joins as well.
		  Join join = null;	
		  Field[] fields = null;
		  START:
		  for(int i = 0; i < joins.size(); i++)
		  {
			 join = joins.get(i);
			 fields = join.getFields();
			 for(int j = 0; j < join.getFieldsCount(); j++)
			 {
				field = fields[j];
				if(field.canWriteDataToDatabase())
				{
				  if(field.hasValidator())
				  {
					if(field.hasDateFormat())
					{
					   vm = field.getValidator().isValid(field.getClientDateValue());
					}
					else
					{
					   vm = field.getValidator().isValid(field.getValue().toString());
					}
					if(vm.isValid() == false)
					{
						//Set the fieldsError for the output
						FieldErrorsOutput err = new FieldErrorsOutput();
						LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
						map.put("name", field.getTableName() + "." + field.getName());
						map.put("status", vm.getMessage());
						err.fieldErrors.add(map);
						output = err;
						ok = false;
						break START;
					}
				  }
				}
			 }
		  }
	  }
	  return ok;
  }
  
  private boolean FieldsValid(Field[] selectfields)
  {
	  boolean ok = true;
	  ValidationMessage vm = null;
	  Field field = null;
	  //Now, check that the value provided is valid on each field
	  for(int i = 0; i < selectfields.length; i++)
	  {
		 field = selectfields[i];
		 if(field.canWriteDataToDatabase())
		 {
		   if(field.hasValidator())
		   {
			 if(field.hasDateFormat())
			 {
			   vm = field.getValidator().isValid(field.getClientDateValue());
			 }
			 else
			 {
			   if(field.getValue() != null)
			   {
			      vm = field.getValidator().isValid(field.getValue().toString());
			   }
			   else
			   {
				  //Even if the value returned is null, that in itself is a valid return.
				  vm = new ValidationMessage(true);
			   }
			 }
			 if(vm.isValid() == false)
			 {
				//Set the fieldsError for the output
				FieldErrorsOutput err = new FieldErrorsOutput();
				LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
				map.put("name", field.getDBFieldName());
				map.put("status", vm.getMessage());
				err.fieldErrors.add(map);
				output = err;
				ok = false;
				break;
			 }
		  }
	    }
	  }
	  
	  return ok;
  }
  /**
   * In some scenarios on the client side you need to display all the possible
   * options for a select dropdown combobox element or numerous checkboxes or radio buttons.
   * To include any set of options for a SELECT query which will become part of the JSON string
   * on output, you can use this method to load up any set of options through the use of a LinkedHashMap.
   * <br>Example:<br><br>
   * <code>
   * //OBTAIN ALL THE DEPARTMENT OPTIONS
   * String value = "value";
   * String label = "label";
   * //Run a quick SELECT query to get the list of possible options on all departments
   * String[][] result = editor.runSelectQuery(tableNames[1], "id", "name"); //all department options
   * ArrayList<LinkedHashMap<String,Object>> al = new ArrayList<LinkedHashMap<String,Object>>();
   * LinkedHashMap<String,Object> row = null;
   * for(int i = 0; i < result.length; i++)
   * {
   *   row = new LinkedHashMap<String,Object>();
   *   for(int j = 0; j < 2; j++)
   *   {
   *     if(j == 0)
   *       row.put(value, result[i][j]);
   *     else
   *       row.put(label, result[i][j]);
   *   }
   *   al.add(row);	   
   * }
   * LinkedHashMap<String,Object> deptOut = new LinkedHashMap<String,Object>();
   * deptOut.put("employees.dept",al);
   * //deptOut now has all the options from the departments table.
   * //Include this data using Editors method IncludeOptionsForOutput(LinkedHashMap<String,Object> opts)	 
   * editor.IncludeOptionsForOutput(deptOut);
   * </code>
   * @param opts
   */
  public void IncludeOptionsForOutput(String key, ArrayList<LinkedHashMap<String,Object>> opts)
  {
	  if(usingSSP)
	  {
		 sspout.options.put(key, opts);  
	  }
	  else
	  {
		 nsspout.options.put(key,opts); 
	  }
	 	 
  }
  
      
  /**
   * Call this method to process your queries or any file upload to the server.  
   */
  public void Process()
  {
	  if(loggingEnabled)
		  logger.debug("Editor.Process - " + params.toString());
	  
	  try{
        String action = params.getAction();
      
        //Run a test on the fields for an upload instance
        //and set the flag appropriately.
        Field fd = findFieldWithUpload();
        if(fd == null && haveUploadInstance)
           haveUploadInstance = false;
        else if(fd != null && haveUploadInstance == false)
           haveUploadInstance = true;
        
        if(action.equals(Constants.UPLOAD))
        {
          if(disableUploads) return;
          if(fd != null)
          {
        	  Upload ul = fd.getUpload();
        	  if(!params.getUploadRowId().isEmpty())
        		  ul.setRowID(Integer.parseInt(params.getUploadRowId().replace(Constants.IDPREFIX, "")));
        	  ul.Execute(db);
          }          
        }
        else if(action.equals(Constants.CREATE))
        {
          if(disableInserts)
        	  return;
          Field field = null;
          String value = null;
          //@See http://editor.datatables.net/manual/server
          //Load the fields with values provided by params
     	  for(int i = 0; i < fields.size(); i++)
     	  {
     		  field = fields.get(i);
     		  if(field.canWriteDataToDatabase())
 	   		  {
     		    if(field.hasSubstituteField())
     		    {
     			  Field substitute = field.getSubstituteField(); 
     			  value = params.getDataValue(field.getTableName(), field.getName());
     			  field.setValue(value, Field.DIRECTION.FROM_CLIENT);
     			  //The Join associated with this field would contain a lookup table - READ ONLY.
      			  //Therefore, inaccessible through other means. So we
      			  //override any restrictions on the Join and set its fields regardless.
      			  //We need to set its fields so that we get the proper output later.
     			  Join join = null;
          	      for(int j = 0; j < joins.size(); j++)
          	      {
          	   	    join = joins.get(j);
          	   	    if(join.getChildTableName().equalsIgnoreCase(substitute.getTableName()))
          	   	    {
          	   		  join.setFieldValues(db, value);
          	   		  break;
          	   	    }
          	      }
     		    }
     		    else
     		    {
     		      field.setValue(params.getDataValue(field.getName()), Field.DIRECTION.FROM_CLIENT);     		      
     		    }
 	   		  }
     	  }	
     	  if(haveJoins())
          {
   		    Join join = null;
      	    for(int i = 0; i < joins.size(); i++)
      	    {
      	   	  join = joins.get(i);
      	   	  if(join.getCanWrite())
      	   	  {
      	   	    Field[] fields = join.getFields();
      	   	    for(int j = 0; j < fields.length; j++)
       	        {
      	   		  field = fields[j];
      	   		  //We may be dealing with a lookup table that is read only,
      	   		  //So ask if we can set the value on a field.
      	   		  //When an update is performed, we'll do it on the LINK table,
      	   		  //NOT the Child table.
      	   		  if(field.canWriteDataToDatabase())
      	   		  {
      	   		    field.setValue(params.getDataValue(field.getTableName(), field.getName()), Field.DIRECTION.FROM_CLIENT);
      	   		  }
       	        }
      	   	  }
      	    } 
          }
    	  boolean ok = allFieldsValid();
    	  if(ok == false)
    	  {
    		//Send back a JSON string with error message
    		//The Java Server Page should be calling the method
    		//Editor.toJSONString() to obtain a response.
    		//The method call allFieldsValid automatically
    		//populates the FieldErrorsOutput object with the appropriate
    		//FieldError.
    		return;
    	  }
    	  query = new Query(Query.Type.INSERT,table);
    	  query.setPrimaryKey(getPrimaryKey());
          db.setQuery(query);
                    
    	  insert();
    	  
    	  if(loggingEnabled)
    		  logger.debug("Editor.Process - insert query: " + query.toString());
    	  
    	  //PREPARE THE OUTPUT
    	  CreateEditDeleteOutput out = new CreateEditDeleteOutput();
    	  LinkedHashMap<String,Object> lhm = new LinkedHashMap<String,Object>();
    	  field = new Field("", Constants.DT_ROWID, Field.Type.STRING);
    	  String recordID = String.valueOf(query.getNewRecordID());
    	  field.setValue(Constants.IDPREFIX + recordID, Field.DIRECTION.FROM_CLIENT);
     	  //fields.add(0, field);
    	  lhm.put(field.getName(),field.getValue().toString());
     	  
    	  if(haveJoins())
	      {
    		LinkedHashMap<String,Object> lhmap = new LinkedHashMap<String,Object>();
	       	for(int n = 0; n < fields.size(); n++)
	        {
	       	  field = fields.get(n);
	       	  if(field.getExcludeOnOutput() == false)
	       	  {
	       		if(field.getFieldType() == Field.Type.DATE)
	       		{
	       		  //field.setValue(results[0][n], Field.DIRECTION.FROM_DB);
  		    	  lhmap.put(field.getName(), field.getClientDateValue()); 
	       		}
  		    	else
  		    	  lhmap.put(field.getName(), field.getValue());   	        	       	        	    
	       	  }
	        }
	        lhm.put(table,lhmap); 
	        Join join = null;
 	        for(int i = 0; i < joins.size(); i++)
 	        {
 	   	         join = joins.get(i);
 	   	         if (join.getExcludeOnOutput() == false)
                 {
                     if (join.getCanRead())
                     {
                       if (join.isUsingLinkTable())
                       {
                    	   if(this.haveUploadInstance)
                     	   {
                    		  join.runJoinSelectQueryForUploads(db);
                     	   }
                     	   else
                     	   {
                     		 join.runJoinSelectQueryForStringArrays(db);
                     	   }
                    	   
                           //using arrays - ONE TO MANY relationships
                           if (join.getTableAlias().equals(""))
                           {
                        	    lhm.put(join.getChildTableName(), join.getArrayFieldValues(recordID, join.getResultsOfJoinSelectQuery()));
                           }
                           else
                           {
                               lhm.put(join.getTableAlias(), join.getArrayFieldValues(recordID, join.getResultsOfJoinSelectQuery()));
                           }
                       }
                       else
                       {
                    	   //using objects only - ONE TO ONE relationships
                 	       Field[] fields = null;
                           fields = join.getFields();
                           Field field3 = null;
                           LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                           for (int l = 0; l < fields.length; l++)
                           {
                               field3 = fields[l];
                               if (field3.getExcludeOnOutput() == false)
                               {
                                   if (field3.getFieldType() == Field.Type.DATE)
                                   {
                                       //field3.setValue(results[0][l], Field.DIRECTION.FROM_DB);
                                       //j++;
                                       map.put(field3.getName(), field3.getClientDateValue());
                                   }
                                   else
                                   {
                                       map.put(field3.getName(), field3.getValue());
                                       //j++;
                                   }

                               }
                           }
                           if (join.getTableAlias().equals(""))
                           {
                        	   lhm.put(join.getChildTableName(), map);
                           }
                           else
                           {
                               lhm.put(join.getTableAlias(), map);
                           }
                       }                           
                     }
                 }
            } //end for loop on joins
 	        out.addDataRow(lhm);
	      }
	      else
	      {
	       	//NOT DEALING WITH JOINS  
	        for(int n = 0; n < fields.size(); n++)
	        {
	       	  field = fields.get(n);
	       	  if(field.getExcludeOnOutput() == false)
	       	  {
	       		if(field.getFieldType() == Field.Type.DATE)
	       		{
	       		  lhm.put(field.getName(), field.getClientDateValue()); 
	       		}
		    	else
		    	  lhm.put(field.getName(), field.getValue().toString());
	       	  }
	         }
	         out.addDataRow(lhm);
	      }
    	  if(haveUploadInstance)
      	  {
      		CleanDatabase();
      	  }     	  
    	  output = out;
    	  
        }
        else if(action.equals(Constants.REMOVE))
        {
          if(disableDeletes) return;	
          query = new Query(Query.Type.DELETE,table);
          db.setQuery(query);          
          remove();                  
          CreateEditDeleteOutput out = new CreateEditDeleteOutput();
          if(haveUploadInstance)
      	  {
      		CleanDatabase();
      	  }
          output = out;          
        }
        else if(action.equals(Constants.EDIT))
        {
           if(disableUpdates) return;
           /*@See http://editor.datatables.net/manual/server
           This operation can involve a single row edit with parameters that look like this:
           action                   = edit
           data[row_29][extn]       = 2947
           data[row_29][first_name] = Fiona
           data[row_29][last_name]  = Green
           data[row_29][office]     = San Francisco
           data[row_29][position]   = Chief Operating Officer (COO)
           data[row_29][salary]     = 850000
           data[row_29][start_date] = 2010-03-11
           OR this operation can involve multi-row editing, so we can receive parameters that look like this:
           action               = edit
           data[row_29][employees][salary] = 110000
           data[row_34][employees][salary] = 110000
           */           
        
        	//The edit process deals with multi-row edits
        	CreateEditDeleteOutput out = new CreateEditDeleteOutput();
        	int[] ids = params.getDistinctIdValues();
        	ArrayList<String> keys = params.getDataKeys();
        	//Separate out the id and field values into a 2D String array
        	ArrayList<Field> fieldRow = null;
        	String[][] hashkeys = new String[keys.size()][2];
        	StringTokenizer tokenizer = null;
        	String row = "";
        	String fieldname = "";
        	for(int i = 0; i < keys.size(); i++)
        	{
        		tokenizer = new StringTokenizer(keys.get(i), ":");
        		row = tokenizer.nextToken();
        		fieldname = tokenizer.nextToken();
        		hashkeys[i][0] = row;
        		hashkeys[i][1] = fieldname;
        		
        	}
        	
        	//Iterate over the hashkeys, discover the field names and locate them
        	//in our list of Fields to set their values.  We will update one row
        	//of values at one time.
        	Field fld = null;
        	int id = -1;
        	int start = 0;
        	for(int x = 0; x < ids.length; x++)
        	{
        	  id = ids[x];
        	  fieldRow = new ArrayList<Field>();
        	  for(int i = start; i < hashkeys.length; i++)
        	  {
        		row = hashkeys[i][0];
        		fieldname = hashkeys[i][1];      		    
        		if(id == Integer.parseInt(row))
        		{           		  	
        		  for(int j = 0; j < fields.size(); j++)
        		  {
        			 if(fields.get(j).getName().equals(fieldname))
        			 {
        				fld = fields.get(j);
        				
        				//Set its value
        				if(fld.canWriteDataToDatabase())
          	   		    {
            			  if(fld.hasSubstituteField())
                 		  {
            				 Field substitute = fld.getSubstituteField(); 
                 			 String value = params.getDataValue(id, fld.getTableName(), fld.getName());
                 			 fld.setValue(value, Field.DIRECTION.FROM_CLIENT);
                 			 //The Join associated with this field would contain a lookup table - READ ONLY.
                 			 //Therefore, inaccessible through other means. So we
                 			 //override any restrictions on the Join and set its fields regardless.
                 			 //We need to set its fields so that we get the proper output later.
                 			 Join join = null;
                     	     for(int k = 0; k < joins.size(); k++)
                     	     {
                     	   	  join = joins.get(k);
                     	   	  if(join.getChildTableName().equalsIgnoreCase(substitute.getTableName()))
                     	   	  {
                     	   		join.setFieldValues(db, value);
                     	   		break;
                     	   	  }
                     	    }
                 		  }
                 		  else
                 		  {  
            		        fld.setValue(params.getDataValue(id, fieldname), Field.DIRECTION.FROM_CLIENT);
            		      }
          	   		    }
        				fieldRow.add(fld);
        				break;
        			 }
        		  }//End for loop on fields
        		}
        		else
        		{         		  
        			start = i;
        			break;        			
        		}//End else
        	  }//End For Loop on hashkeys
        	
        	
        	  //Do the same in the Joins and set the field values
     	      if(haveJoins())
     	      {
     	    	Join join = null;
     		    Field field = null;
         	    for(int i = 0; i < joins.size(); i++)
         	    {
         	   	  join = joins.get(i);
         	   	  if(join.getCanWrite())
         	   	  {
         	   	    Field[] fields = join.getFields();
         	   	    for(int j = 0; j < fields.length; j++)
          	        {
         	   		  field = fields[j];
         	   		  //We may be dealing with a lookup table that is read only,
         	   		  //So ask if we can set the value on a field.
         	   		  //When an update is performed, we'll do it on the LINK table,
         	   		  //NOT the Child table.
         	   		  if(field.canWriteDataToDatabase())
         	   		  {         	   			
         	   			field.setValue(params.getDataValue(id, field.getTableName(), field.getName()), Field.DIRECTION.FROM_CLIENT);
         	   		  }
          	        }
         	   	  }
         	    }
     	      }
        	
        	  //Convert fieldRow to an array of type Field
     	      Field[] flds = convertTo_FieldsArray(fieldRow);
			  
			  boolean ok = FieldsValid(flds);
			  if(ok == false)
     	      {
     		    //Send back a JSON string with error message
    		    //The Java Server Page should be calling the method
    		    //Editor.toJSONString() to obtain a response.
    		    //The method call FieldsValid automatically
    		    //populates the FieldErrorsOutput object with the appropriate
    		    //FieldError.
     		    return;
     	      }
     	      query = new Query(Query.Type.UPDATE,table);
     	      //Let the query object know what fields we are acting on.
     		  query.setFields(flds);
              db.setQuery(query);
          
              //Calling update here also performs updates on any Join
     	      update(id);
     	      if(loggingEnabled)
     		     logger.debug("Editor.Process - update query: " + query.toString());
        	
        	  //************* PREPARE THE OUTPUT FOR THIS ONE ROW ********************     	    
     	      String strquery = "";
     	      String[][] results = null;
     	      LinkedHashMap<String,Object> lhm = null;
     	      Field field = null;
     	      String fds = "";
   		      for(int i = 0; i < fields.size(); i++)
   		      {
   			     fds += fields.get(i).toString() + ",";
   		      }
   		      fds = fds.substring(0,fds.length() - 1);
   		    
   		    
   		      strquery = "SELECT " + fds + " FROM " + table + " WHERE " + getPrimaryKey() + " = " + String.valueOf(ids[x]);
   		      if(Editor.isLoggingEnabled())
			     logger.debug("Editor - Edit Process - query: " + strquery);
		      results = db.executeSelect(strquery);
		      
		      //PRINT RESULTS
		      if(Editor.isLoggingEnabled())
		      { 
		    	String line = "QUERY RESULTS:\n ";
		        for(int i = 0; i < results.length; i++)
		        {
		    	  for(int j = 0; j < results[0].length; j++)
			      {
			    	  line += results[i][j] + " | ";
			      }
		    	  line += "\n";
		        }
		        logger.debug(line);
		      }
		      
		      
		      field = new Field("", Constants.DT_ROWID,Field.Type.STRING);
		      field.setValue(Constants.IDPREFIX + String.valueOf(id), Field.DIRECTION.FROM_DB);
   	          lhm = new LinkedHashMap<String,Object>();
   	          lhm.put(field.getName(), field.getValue().toString());
   	          
   	          if(haveJoins())
   	          {
   	        	LinkedHashMap<String,Object> lhmap = new LinkedHashMap<String,Object>();
   	        	for(int n = 0; n < fields.size(); n++)
   	            {
   	        	  field = fields.get(n);
   	        	  if(field.getExcludeOnOutput() == false)
   	        	  {
   	        		if(field.getFieldType() == Field.Type.DATE)
   	        		{
   	        		  field.setValue(results[0][n], Field.DIRECTION.FROM_DB);
      		    	  lhmap.put(field.getName(), field.getClientDateValue()); 
   	        		}   	        		
      		    	else
      		    	  lhmap.put(field.getName(), results[0][n]);   	        	       	        	    
   	        	  }
   	            }
   	        	lhm.put(table,lhmap); 
   	        	Join join = null;
     	        for(int i = 0; i < joins.size(); i++)
     	        {
     	   	         join = joins.get(i);
     	   	         if (join.getExcludeOnOutput() == false)
                     {
                         if (join.getCanRead())
                         {
                           if (join.isUsingLinkTable())
                           {
                        	   if(this.haveUploadInstance)
                         	   {
                         		  join.runJoinSelectQueryForUploads(db);
                         	   }
                         	   else
                         	   {
                                  join.runJoinSelectQueryForStringArrays(db);
                         	   }
                               //using arrays - ONE TO MANY relationships
                               if (join.getTableAlias().equals(""))
                               {
                                    lhm.put(join.getChildTableName(), join.getArrayFieldValues(String.valueOf(id), join.getResultsOfJoinSelectQuery()));
                               }
                               else
                               {
                                   lhm.put(join.getTableAlias(), join.getArrayFieldValues(String.valueOf(id), join.getResultsOfJoinSelectQuery()));
                               }
                           }
                           else
                           {
                               //using objects only - ONE TO ONE relationships
                     	       Field[] fields = null;
                               fields = join.getFields();
                               Field field3 = null;
                               LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
                               for (int l = 0; l < fields.length; l++)
                               {
                                   field3 = fields[l];
                                   if (field3.getExcludeOnOutput() == false)
                                   {
                                       if (field3.getFieldType() == Field.Type.DATE)
                                       {
                                           //field3.setValue(results[0][l], Field.DIRECTION.FROM_DB);
                                           //j++;
                                           map.put(field3.getName(), field3.getClientDateValue());
                                       }
                                       else
                                       {
                                           map.put(field3.getName(), field3.getValue());
                                           //j++;
                                       }

                                   }
                               }
                               if (join.getTableAlias().equals(""))
                               {
                            	   lhm.put(join.getChildTableName(), map);
                               }
                               else
                               {
                                   lhm.put(join.getTableAlias(), map);
                               }
                           }                           
                         }
                     }
                } //end for loop on joins
     	        out.addDataRow(lhm);
   	          }
   	          else
   	          {
   	        	//NOT DEALING WITH JOINS  
   	            for(int n = 0; n < fields.size(); n++)
   	            {
   	        	  field = fields.get(n);
   	        	  if(field.getExcludeOnOutput() == false)
   	        	  {
   	        		if(field.getFieldType() == Field.Type.DATE)
   	        		{
   	        	      //System.out.println("Editor.edit - results[0][n]: " + results[0][n]);
   	        		  field.setValue(results[0][n], Field.DIRECTION.FROM_DB);	
    		    	  lhm.put(field.getName(), field.getClientDateValue()); 
   	        		}   	        		
    		    	else
    		    	  lhm.put(field.getName(), results[0][n]);
   	        	  }
   	            }
   	            out.addDataRow(lhm);
   	          }
   	          
  	        //}//End for on ids array
        	}//End For Loop on ids array  
        	if(haveUploadInstance)
        	{
        		CleanDatabase();
        	}
  	        output = out;
        
      }
      else
      {
    	//THE ACTION IS A STRAIGHT SELECT QUERY 
    	  
    	if(disableSelects) return;  
    	query = new Query(Query.Type.SELECT,table);    	
    	
    	//Set the query object in the Database object
    	db.setQuery(query);    	
    	
        select();
        
        if(data == null)
        	return;
        
        if(loggingEnabled)
        	logger.debug("Editor.Process - select query: " + query.toString());
        
        //PROVIDE THE OUTPUT OBJECT WITH EVERYTHING IT REQUIRES TO SEND
	    //BACK AN APPROPRIATE RESPONSE.
        if(usingSSP)
        {
          if(params.getDraw() > -1)
  	      {
        	sspout.draw = params.getDraw();
        	sspout.recordsTotal = String.valueOf(query.getITotal());
        	sspout.recordsFiltered = String.valueOf(query.getIFilteredTotal());
  	      }
          if(includeRowID && includeRowClass == false)
  	      {
  	        Field field = new Field("",Constants.DT_ROWID,Field.Type.INT);
  	        fields.add(0,field);
  	      }
  	      else if(includeRowID && includeRowClass)
  	      {
  	        Field field1 = new Field("",Constants.DT_ROWID,Field.Type.INT);
  	        fields.add(0,field1);
  	        Field field2 = new Field("",Constants.DT_ROWCLASS,Field.Type.STRING);
  	        fields.add(1,field2);	      
  	      }
  	      else if(includeRowID == false && includeRowClass)
  	      {
  	        Field field = new Field("",Constants.DT_ROWCLASS,Field.Type.STRING);
  	        fields.add(0,field);	      
  	      }
          //PUT THE DATA INTO THE OUTPUT OBJECT FOR PROCESSING
          LinkedHashMap<String,Object> row = null;
          LinkedHashMap<String,Object> map = null;
  	      int cols = fields.size();
  	      String columnData = "";
  	      int index = 0;
  	      Field field = null;
  	      Field field3 = null;
  	      int j = 0;
  	      
  	      if (haveJoins())
          {
            //Determine any of the joins are processing String Arrays
            for (Join jn : joins)
            {
                if (jn.isUsingLinkTable())
                {
                    //Make each of these type joins retain its own result set from a select query.
                    //This is to ensure there is only one trip to the database server.
                	if(this.haveUploadInstance)
              	    {
              		  jn.runJoinSelectQueryForUploads(db);
              	    }
              	    else
              	    {
                      jn.runJoinSelectQueryForStringArrays(db);
              	    }
                }
            }
          }
  	      //Iterate over the data
  	      for(int i = 0; i < data.length; i++)
  	      {
  	    	//load up on data from the parent table first
  		    row = new LinkedHashMap<String,Object>();
  		    for(; j < cols; j++)
  		    {
  		      if(j == 0 && includeRowID && includeRowClass == false)
  		      {
  		        row.put(fields.get(j).getName(),Constants.IDPREFIX + data[i][j]);
  		      }
  		      else if(j == 0 && includeRowID && includeRowClass)
  		      {
  		    	row.put(fields.get(j).getName(),Constants.IDPREFIX + data[i][j]);
  		      }
  		      else if(j == 1 && includeRowID && includeRowClass)
		      {
  		    	row.put(fields.get(j).getName(),rowclassPrefix + data[i][j]); 
		      }
  		      else if(j == 0 && includeRowID == false && includeRowClass)
  		      {
  		    	row.put(fields.get(j).getName(),rowclassPrefix + data[i][j]);  
  		      }
  		      else
  		      {
  		    	
  		    	columnData = params.getColumn(index).getData();  
  		    	field = findField(columnData); 
  		    	if(field.getExcludeOnOutput() == false)
				{
  		    	  if(field.getFieldType() == Field.Type.DATE)
  		    	  {
  		    	    field.setValue(data[i][j], Field.DIRECTION.FROM_DB);  		    		  
  		    	    row.put(columnData, field.getClientDateValue());  		    	  
  		    	  }  		    	  
  		    	  else
  		    	    row.put(columnData ,data[i][j]);  		    	  
				}  		    	
  		    	index++;
  		    		    	
  		      }
  		    }//end of inner for loop
  		    
  		    index = 0;//reset
  		    
  		    if (haveJoins())
            {
              //Now, load up on data from the joins, one row at a time
              Join join = null;
              Field[] fields = null;
              for (int k = 0; k < joins.size(); k++)
              {
                  join = joins.get(k);
                  if (join.getExcludeOnOutput() == false)
                  {
                      if (join.getCanRead())
                      {
                          if (join.isUsingLinkTable())
                          {
                              //using arrays - ONE TO MANY relationships
                              if (join.getTableAlias().equals(""))
                              {
                                  row.put(join.getChildTableName(), join.getArrayFieldValues(data[i][0], join.getResultsOfJoinSelectQuery()));
                              }
                              else
                              {
                                  row.put(join.getTableAlias(), join.getArrayFieldValues(data[i][0], join.getResultsOfJoinSelectQuery()));
                              }
                          }
                          else
                          {
                              //using objects only - ONE TO ONE relationships
                              fields = join.getFields();
                              map = new LinkedHashMap<String, Object>();
                              for (int l = 0; l < fields.length; l++)
                              {
                                  field3 = fields[l];
                                  if (field3.getExcludeOnOutput() == false)
                                  {
                                      if (field3.getFieldType() == Field.Type.DATE)
                                      {
                                          field3.setValue(data[i][j], Field.DIRECTION.FROM_DB);
                                          j++;
                                          map.put(field3.getName(), field3.getClientDateValue());
                                      }                                      
                                      else
                                      {
                                          map.put(field3.getName(), data[i][j]);
                                          j++;
                                      }

                                  }
                              }
                              if (join.getTableAlias().equals(""))
                              {
                                  row.put(join.getChildTableName(), map);
                              }
                              else
                              {
                                  row.put(join.getTableAlias(), map);
                              }
                          }
                      }
                  }
               } //end for loop on joins
            }//end of if haveJoins()  
            j = 0; //reset
            sspout.addDataRow(row);
          } //end outer for loop
  	      //Add files if we have an Upload instance anywhere
  	      if(haveUploadInstance)
  	      {
  	    	Field fld = findFieldWithUpload();
  	    	Upload upload = fld.getUpload();
  	    	sspout.files.put(upload.Table(), field.getUpload().getDBData(db));
  	      }
  	      output = sspout;
        }//end of usingSSP
        else
        {
        	//PUT THE DATA INTO THE OUTPUT OBJECT FOR PROCESSING
            LinkedHashMap<String,Object> row = null;
            LinkedHashMap<String,Object> map = null;
			LinkedHashMap<String,Object> lhmap = null;
    	    int cols = fields.size();
    	    Field field2 = null;
    	    int j = 0;
    	      
    	    if(haveJoins())
            {
              //Determine any of the joins are processing String Arrays
              for(Join jn : joins)
              {
                  if(jn.isUsingLinkTable())
                  {
                      //Make each of these type joins retain its own result set from a select query.
                      //This is to ensure there is only one trip to the database server.
                	  if(this.haveUploadInstance)
                	  {
                		  jn.runJoinSelectQueryForUploads(db);
                	  }
                	  else
                	  {
                        jn.runJoinSelectQueryForStringArrays(db);
                	  }
                  }
              }
			  
			  for(int i = 0; i < data.length; i++)
    	      {
				row = new LinkedHashMap<String,Object>();
				lhmap = new LinkedHashMap<String,Object>();
				row.put(Constants.DT_ROWID, Constants.IDPREFIX + data[i][j]);
    		    //Load up on data from parent table first
    		    for(; j < cols; j++)
    		    {    		      
    		      field2 = fields.get(j);
    		      if(field2.getExcludeOnOutput() == false)
  	    	      {
    		        if(field2.getFieldType() == Field.Type.DATE)
  			        {
    		    	  field2.setValue(data[i][j+1], Field.DIRECTION.FROM_DB);
    		    	  lhmap.put(field2.getName(),field2.getClientDateValue());
  			        }    		        
    		        else
    		        {
    		          lhmap.put(fields.get(j).getName(),data[i][j+1]);
    		        }
  	    	      }
    		    }//end of inner for loop
    		    row.put(table,lhmap);
    	    
                //Now, load up on data from the joins, one row at a time
                Join join = null;
                Field[] fields = null;
              
                for(int k = 0; k < joins.size(); k++)
                {
                    join = joins.get(k);
                    if (join.getExcludeOnOutput() == false)
                    {
                        if (join.getCanRead())
                        {
                            if (join.isUsingLinkTable())
                            {
                              //using arrays - ONE TO MANY relationships
                              if (join.getTableAlias().equals(""))
                              {
                                  row.put(join.getChildTableName(), join.getArrayFieldValues(data[i][0], join.getResultsOfJoinSelectQuery()));
                              }
                              else
                              {
                                  row.put(join.getTableAlias(), join.getArrayFieldValues(data[i][0], join.getResultsOfJoinSelectQuery()));
                              }
                            }
                            else
                            {
                                //using objects only - ONE TO ONE relationships
                                fields = join.getFields();
                                map = new LinkedHashMap<String, Object>();
                                for (int l = 0; l < fields.length; l++)
                                {
                                    field2 = fields[l];
                                    if (field2.getExcludeOnOutput() == false)
                                    {
                                      if (field2.getFieldType() == Field.Type.DATE)
                                      {
                                          field2.setValue(data[i][j+1], Field.DIRECTION.FROM_DB);
                                          j++;
                                          map.put(field2.getName(), field2.getClientDateValue());
                                      }                                      
                                      else
                                      {
                                          map.put(field2.getName(), data[i][j+1]);
                                          j++;
                                      }
                                    }
                                }
                                if (join.getTableAlias().equals(""))
                                {
                                    row.put(join.getChildTableName(), map);
                                }
                                else
                                {
                                    row.put(join.getTableAlias(), map);
                                }
                            }//End else
                        }//End if (join.getCanRead())
                    }//End if (join.getExcludeOnOutput() == false)
                }//end of for loop iterating over joins                
                j = 0; //reset              
                nsspout.addDataRow(row);
              }//end of outer for iterating over data
            }
			else
			{
			    //NOT DEALING WITH JOINS
				Field field = new Field("",Constants.DT_ROWID,Field.Type.INT);
	  	        fields.add(0,field);
			    for(int i = 0; i < data.length; i++)
    	        {
    		      row = new LinkedHashMap<String,Object>();
    		      //Load up on data from parent table first
    		      for(; j < fields.size(); j++)
    		      {
    		        field2 = fields.get(j);
    		        if(field2.getExcludeOnOutput() == false)
  	    	        {
    		          if(field2.getFieldType() == Field.Type.DATE)
  			          {
    		    	    field2.setValue(data[i][j], Field.DIRECTION.FROM_DB);
    		    	    row.put(field2.getName(),field2.getClientDateValue());
  			          }    		          
    		          else
    		          {
    		    	    if(j == 0)
    		    		  row.put(fields.get(j).getName(),Constants.IDPREFIX + data[i][j]);
    		    	    else
    		              row.put(fields.get(j).getName(),data[i][j]);
    		          }
  	    	        }
    		      }//end of inner for loop
    		      j = 0; //reset
    		      nsspout.addDataRow(row);
				}
			}
    	    
    	    //Add files if we have an Upload instance anywhere
    	    if(haveUploadInstance)
    	    {
    	    	Field field = findFieldWithUpload();
    	    	Upload upload = field.getUpload();
    	    	nsspout.files.put(upload.Table(), field.getUpload().getDBData(db));
    	    }
    	    
  	        output = nsspout;   
  	      
          }//end of using Non-SSP 
      }//End else on processing SELECT    
    }//End Try 
    catch(Exception e)
    {
      if(loggingEnabled)
  	   logger.error("Log4J error: " +  getFullStackTrace(e));      
    }//End Catch    
  }//End Process method
  
  private Field findFieldWithUpload()
  {
	  Field fld = null;
	  boolean found = false;
      for(Field field : fields)
      {
    	  if(field.hasUpload())
    	  {
    		  fld = field;
    		  found = true;
    		  break;
    	  }
      }
      if(found == false)
      {
    	  //Then maybe the field with the instance of Upload is in a Join?
    	  if(haveJoins())
    	  {
    		  START:
    		  for(Join join : joins)
    		  {
    			 if(join.haveFields()) 
    			 {
    			   for(Field field : join.getFields())
    			   {
    				 if(field.hasUpload())
    	        	  {
    	        		  fld = field;
    	        		  break START;
    	        	  } 
    			   }
    			 }
    			 if(join.isUsingLinkTable() && join.haveLinkTableFields())
    			 {    			   
    				 for(Field field : join.getLinkTableFields())
        			 {
        				 if(field.hasUpload())
        	        	  {
        	        		  fld = field;
        	        		  break START;
        	        	  } 
        			 }    			   
    			 }
    		  }
    	  }
      }
      return fld;
  }
  
  private void CleanDatabase()
  {
	  Field field = findFieldWithUpload();
	  Upload uload = field.getUpload();
	  //Knowing if we are saving files to the server or to the database
	  //is important. We don't want to perform a clean of the table if
	  //we are saving files directly to the database.  The delete functionality
	  //within editor is enough to perform this function.
	  if(uload.haveSystemPath())
	     uload.DbCleanExec(db);
  }
  
  //This method only works in conjunction with SSP when
  //assembling the appropriate field data for SSPOutput.
  private Field findField(String mDataProp)
  {
	  if(isNumeric(mDataProp))
	  {
		  if(includeRowID && includeRowClass == false)
		  {
		    int index = Integer.parseInt(mDataProp) + 1;
		    return fields.get(index);
		  }
		  else if(includeRowID && includeRowClass)
		  {
			  int index = Integer.parseInt(mDataProp) + 2;
			  return fields.get(index);  
		  }
		  else if(includeRowID == false && includeRowClass)
		  {
			  int index = Integer.parseInt(mDataProp) + 1;
			  return fields.get(index);  
		  }
		  else if(includeRowID == false && includeRowClass == false)
		  {
			  int index = Integer.parseInt(mDataProp);
			  return fields.get(index);  
		  }
	  }
	  Field field = null;
	  for(int i = 0; i < fields.size(); i++)
	  {
		  field = fields.get(i);
		  if(field.getDBFieldName().equals(mDataProp))
			  break;
	  }
	  return field;
  }
  
  private Field[] convertTo_FieldsArray(ArrayList<Field> fields)
  {
	  Field[] flds = new Field[fields.size()];
	  for(int i = 0; i < fields.size(); i++)
	  {
		  flds[i] = fields.get(i);
	  }
	  return flds;
  }
  
  private Field[] addPrimaryKeyField(ArrayList<Field> fields)
  {   
	  Field[] flds = new Field[fields.size() + 1];
	  flds[0] = new Field(table,pKey,Field.Type.INT);
	  int count = 1;
	  for(int i = 0; i < fields.size(); i++)
	  {
		  flds[count] = fields.get(i);
		  count++;
	  }
	  
	  return flds;
  }
  
  private Field[] addPrimaryKeyAndClassField(ArrayList<Field> fields) throws UnknownFieldException
  {
	  if(rowclassFieldReference.equals(""))
		  throw new UnknownFieldException("rowclassFieldReference is an empty string");
	  Field field = null;
	  //check that the rowclassFieldReference is actually referencing an existing field
	  boolean ok = false;
	  for(int i = 0; i < fields.size(); i++)
	  {
		 field = fields.get(i);
		 if(field.getDBFieldName().equalsIgnoreCase(rowclassFieldReference))
		 {
	       ok = true;
	       break;
		 }
	  }
	  if(ok == false)
	  {
		  throw new UnknownFieldException("rowclassFieldReference: " + rowclassFieldReference + " is not a field known to the existing list of fields.");  
	  }
	  
	  Field[] flds = new Field[fields.size() + 2];
	  flds[0] = new Field(table,pKey,Field.Type.INT);
	  field = null;
	  for(int i = 0; i < fields.size(); i++)
	  {
		 field = fields.get(i);
		 if(field.getDBFieldName().equalsIgnoreCase(rowclassFieldReference))
		 {
	       flds[1] = new Field("",field.getDBFieldName(),field.getFieldType());
	       break;
		 }
	  }
	  int count = 2;
	  for(int i = 0; i < fields.size(); i++)
	  {
		  flds[count] = fields.get(i);
		  count++;
	  }
	  
	  if(loggingEnabled)
	  {
		logger.debug("Editor.addPrimaryKeyAndClassField - flds.length: " + flds.length);
	    for(int i = 0; i < flds.length; i++)
	    {
		  if(flds[i] == null)
			  logger.debug("Editor.addPrimaryKeyAndClassField - flds[" + i +"] is null");
	    }
	  }
	  
	  return flds;
  }
  
  private Field[] addClassField(ArrayList<Field> fields) throws UnknownFieldException
  {
	  if(rowclassFieldReference.equals(""))
		  throw new UnknownFieldException("rowclassFieldReference is an empty string");
	  Field field = null;
	  //check that the rowclassFieldReference is actually referencing an existing field
	  boolean ok = false;
	  for(int i = 0; i < fields.size(); i++)
	  {
		 field = fields.get(i);
		 if(field.getDBFieldName().equalsIgnoreCase(rowclassFieldReference))
		 {
	       ok = true;
	       break;
		 }
	  }
	  if(ok == false)
	  {
		  throw new UnknownFieldException("rowclassFieldReference: " + rowclassFieldReference + " is not a field known to the existing list of fields.");  
	  }
	  
	  Field[] flds = new Field[fields.size() + 1];
	  
	  field = null;
	  for(int i = 0; i < fields.size(); i++)
	  {
		 field = fields.get(i);
		 if(field.getDBFieldName().equalsIgnoreCase(rowclassFieldReference))
		 {
	       flds[0] = new Field("", field.getDBFieldName(),field.getFieldType());
	       break;
		 }
	  }
	  int count = 1;
	  for(int i = 0; i < fields.size(); i++)
	  {
		  flds[count] = fields.get(i);
		  count++;
	  }
	  	  
	  if(loggingEnabled)
	  {
		logger.debug("Editor.addClassField - flds.length: " + flds.length);
	    for(int i = 0; i < flds.length; i++)
	    {
		  if(flds[i] == null)
			  logger.debug("Editor.addClassField - flds[" + i +"] is null");
	    }
	  }
	  return flds;
  }
  
    
  private void select()
  {
	  //SET ALL THE INFORMATION WITHIN THE QUERY OBJECT
	  //SO THAT A PROPER QUERY CAN BE INVOKED ON THE DATABASE.
	  //The fields provided for the query need to include the name of the 
	  //primary key field.
	  query.setPrimaryKey(pKey);
	  if(haveOrder())
	  {
		Order[] ord = new Order[orders.size()];
		for(int i = 0; i < orders.size(); i++)
		{
			ord[i] = orders.get(i);
		}
	   	query.setOrder(ord);
	  }
	  
	  if(haveJoins())
      {
		  Join[] jns = new Join[joins.size()];
		  for(int i = 0; i < joins.size(); i++)
		  {
			  jns[i] = joins.get(i);
		  }
          query.setJoins(jns);
      }
	  
	  if(params.getDraw() > -1)
	  {
		usingSSP = true;
	    ssp_sort(query);
	    ssp_filter(query);
	    ssp_limit(query);
	  }
	  if(usingSSP)
	  {
		  if(includeRowID && includeRowClass == false)
		  {
			  query.setFields(addPrimaryKeyField(fields));
		  }
		  else if(includeRowID && includeRowClass)
		  {
			  try{
			  query.setFields(addPrimaryKeyAndClassField(fields));
			  }
			  catch(UnknownFieldException ufe){ufe.printStackTrace();}
		  }
		  else if(includeRowID == false && includeRowClass)
		  {
			  try{
			  query.setFields(addClassField(fields));
			  }
			  catch(UnknownFieldException ufe){ufe.printStackTrace();}
		  }
		  else
		  {
		      query.setFields(convertTo_FieldsArray(fields));
		  }
		  
	  }
	  else
	  {
		//Primary key field is mandatory for non-ssp processing
	    query.setFields(addPrimaryKeyField(fields));	    
	  }
	  
	  //Include any WHERE conditions if the user is providing a filter directly in their query.
	  if(whereGroups != null)
	  {
		  query.setWhereConditionGroups(whereGroups);
	  }
	  else if(where.size() > 0)
  	  {
  	    WhereCondition[] wc = new WhereCondition[where.size()];
  	    for(int i = 0; i < where.size(); i++)
  	    {
  		  wc[i] = where.get(i);
  	    }
  	    query.setWhereConditions(wc);
  	  }
  	  
	  data = db.executeSelect();	  
  }

  
  private void insert()
  {
	  query.setFields(convertTo_FieldsArray(fields));
	  try{
	    boolean success = db.executeInsertUpdate();
	    //If the insert was unsuccessful, we should be
	    //sending an error response back to the client side.
	    if(success == false)
		  return;
	  
	    //Insert new record in the Joins as well.
 	    if(haveJoins())
        {
  	      Join join = null;
  	      for(int i = 0; i < joins.size(); i++)
  	      {
  	    	  join = joins.get(i);
  	    	  if(join.getCanWrite())
  	    	  {
  	    		  join.insert(db,query.getNewRecordID(), params);
  	    	  }
  	    	  if(setJoinFields)
	    	  {
	    		  setJoinFields(join, params.getDataValue(parameterKey));
	    	  }
  	      }
        }
	  }
	  catch(Exception e)
	  {
		  if(isLoggingEnabled())	    	  
		  {	    	
	    	logger.error(Editor.getFullStackTrace(e));
		  }	  
	  }
	  
  }
  //When we perform an update, it is only on one row of data!
  private void update(int id)
  {
	try{
	  //In addition, create a new Field object representing the PRIMARY KEY
	  //field and set a Where object for the Query to act upon.
	  WhereCondition[] where = new WhereCondition[1];
	  Field pkfield = new Field("",pKey,Field.Type.INT);	  
	  where[0] = new WhereCondition(pkfield, id, "=");
	  query.setWhereConditions(where);
	  
	  boolean success = db.executeInsertUpdate();
	  //If the update was unsuccessful, we should be
	  //sending an error response back to the client side.
	  if(success == false)
		  return;
	  
	  
	  //Update any changes in the Joins as well.
 	  if(haveJoins())
      {
  	      Join join = null;
  	      for(int i = 0; i < joins.size(); i++)
  	      {
  	    	  join = joins.get(i);
  	    	  if(join.getCanWrite())
  	    	  {
  	    		  join.update(db,id, params);
  	    	  }
  	    	  if(setJoinFields)
  	    	  {
  	    		  setJoinFields(join, params.getDataValue(parameterKey));
  	    	  }
  	      }
      }
	}
	catch(Exception e)
	{
	  if(isLoggingEnabled())	    	  
	  {	    	
	   	logger.error(Editor.getFullStackTrace(e));
	  }	  
	}
	  
  }
  
  //Delete one or more rows from the database
  private void remove()
  {
	try{
	  	  
	  int[] ids = params.getDistinctIdValues();  
	  	  
	  //To perform multiple deletes, we need multiple Query objects
	  //to pass to the Database.
	  //We already have one Query object floating around.  We need to
	  //create more, but with different Where objects so we can set the
	  //Primary Key values.
	
	  if(ids.length == 1)
	  { 
	    Query[] queries = new Query[1];
	    queries[0] = query;
	    WhereCondition[] where = new WhereCondition[1];
	    Field pkfield = new Field("",pKey,Field.Type.INT);
	    where[0] = new WhereCondition(pkfield, ids[0], "=");
	    query.setWhereConditions(where);
	    if(loggingEnabled)
	    	logger.debug("Editor.remove - delete query: " + query.toString());
	    db.executeDeletes(queries);	  
	  }
	  else
	  {
	    Query[] queries = new Query[ids.length];
	    queries[0] = query;
	    Query temp = null; 
	    WhereCondition[] where = null;
	    Field pkfield = null;
	    //Create additional Query objects
	    for(int i = 1; i < ids.length; i++)
	    {
           temp = new Query(Query.Type.DELETE,table);
           queries[i] = temp;		
	    }
	    //Now set the Where objects for all of them
	    for(int i = 0; i < ids.length; i++)
	    {
           where = new WhereCondition[1];
           pkfield = new Field("",pKey,Field.Type.INT);
           where[0] = new WhereCondition(pkfield,ids[i],"=");
           queries[i].setWhereConditions(where);
           if(loggingEnabled)
        	   logger.debug("Editor.remove - delete query: " + queries[i].toString());
	    }
	    db.executeDeletes(queries);
	  }
	  if(haveJoins())
      {
  	    Join join = null;
  	    for(int i = 0; i < joins.size(); i++)
  	    {
  	      join = joins.get(i);
  	      if(join.getCanWrite())
  	         join.delete(db, ids);
  	    }
      }
	
	}
	catch(Exception e)
	{
		if(loggingEnabled)
	    	 logger.error("Log4J error: " +  getFullStackTrace(e)); 
	}
  }
  
  /**
   * Call this method if you want to apply a filter on your query.  
   * @param condition
   * 
   */
  public void addWhere(WhereCondition condition)
  {
	  where.add(condition);
  }
  /**
   * Call this method if you want to apply multiple WhereConditions to your query.
   * @param groups
   */
  public void addWhereConditionGroup(WhereConditionGroups groups)
  {
	 whereGroups = groups; 
  }
  /**
   * Get all the groups of WhereConditions.
   * @return WhereConditionGroups
   */
  public WhereConditionGroups getWhereGroups()
  {
	  return whereGroups;
  }
  
  /**
   * Get all the Where conditions on your query
   * @return
   */
  public ArrayList<WhereCondition> getWhere()
  {
    return where;
  }
  
  
  private String ssp_field(int index) 
  {	  
	  return fields.get(index).getDBFieldName();	  
  }
  
  private void ssp_sort(Query query)
  {
	  ArrayList<Order> orders = new ArrayList<Order>();
	  int cols = params.getColumnOrdersSize();
	  Order temp = null;
	  for(int i = 0; i < cols; i++)
	  {
		  //if(params.getSortableColumn("bSortable_"+ i) == true)
		  if(params.getColumn(i).getOrderable())
		  {
			  //temp = new Order(ssp_field(params.getISortColumn("iSortCol_" + i)), params.getSSortDir("sSortDir_" + i));
			  temp = new Order(ssp_field(params.getColumnOrder(i).getColumn()),params.getColumnOrder(i).getDirection().toString());
			  orders.add(temp);
		  }
	  }
	  if(orders.size() > 0)
	  {
	    Order[] queryOrders = new Order[orders.size()];
	    for(int i = 0; i < orders.size(); i++)
	    {
		  queryOrders[i] = orders.get(i);
	    }
	    query.setOrder(queryOrders);
	  }
	  
  }
  
  private void ssp_filter(Query query)
  {
	  int columnsSize = params.getColumns().size();
	  
	  //FILTER ON ALL FIELDS
	  Field field = null;
	  Column col = null;
	  if(!params.getSearchValue().equals(""))
	  {  
		//This will be a global search on all fields.
		//Therefore, set the FilterLogicalOperator to OR
		query.setFilterLogicalOperator(Query.FilterLogicalOperator.OR);
		WhereCondition[] where = new WhereCondition[params.getColumns().size()];
		
		for(int i = 0; i < columnsSize; i++)
		{
			col = params.getColumn(i);
			if(col.getSearchable())
			{
			  field = fields.get(i);
			  where[i] = new WhereCondition(field, "%" + params.getSearchValue() + "%" , " LIKE ");
			}
		}
		query.setWhereConditions(where);
		return;
	  }
	  //INDIVIDUAL COLUMN FILTERING
	  query.setFilterLogicalOperator(Query.FilterLogicalOperator.AND);
	  ArrayList<WhereCondition> list = new ArrayList<WhereCondition>();
	  String searchValue = "";	  
	  for(int i = 0; i < columnsSize; i++)
	  {
		 col = params.getColumn(i);
		 searchValue = col.getSearchValue(); 
		 if(searchValue != null && !searchValue.equals("") && col.getSearchable())
		 {
			 field = fields.get(i);
			 list.add(new WhereCondition(field,"%" + searchValue + "%", " LIKE "));	
		 }		 
	  }
	  if(list.size() > 0)
	  {
	    WhereCondition[] where = convertToArray(list);
	    query.setWhereConditions(where);
	  }
  }
  
  private WhereCondition[] convertToArray(ArrayList<WhereCondition> list)
  {
	  WhereCondition[] where = new WhereCondition[list.size()];
	  for(int i = 0;  i < list.size(); i++)
	  {
		  where[i] = list.get(i);
	  }
	  return where;
  }
  
  private void ssp_limit(Query query)
  {
	  //-1 is show all
	  if(params.getLength() != -1)
	  {
		  query.setOffset(params.getStart());
		  query.setLimit(params.getLength());
	  }
  }
  /**
   * Use this method to append a JSON string to an existing one.
   * This method assumes that the first parameter json1 is a complete
   * JSON representation of some data structure and the second parameter
   * is represents some object structure. Usage:<br>
   * String json1 = &quot;{ "data1":[...] }&quot;<br>
   * String json2 = &quot;{ "data2":[...] }&quot;<br>
   * String result = appendJSONData(json1, json2);<br>
   * System.out.println(result);<br>
   * {"data1":[...],"data2"[...]}<br>
   * @param json1
   * @param json2
   * @return A complete JSON string resultant from both JSON parameters.
   */
  public String appendJSONData(String json1, String json2)
  {
	  //Remove the last character "}" from the first JSON string  
	  char lastChar = json1.charAt(json1.length() - 1);
	  //Make json1 contain everything except the last character
	  //We'll append it later.
	  json1 = json1.substring(0,json1.length() - 1);
	  //add a comma on the end of json1
	  json1 += ",";
	  //Strip away both the first and last character "{" and "}" from json2
	  json2 = json2.substring(1,json2.length() - 1);
	  //Add json2 to json1
	  json1 += json2;
	  json1 += lastChar;
	  return json1;
	  
  }
  
  /**
   * This is just a convenience method to run a simple SELECT query
   * on just two fields in a table.  
   * @param tableName
   * @param field1
   * @param field2
   * @return A 2D String array
   */
  public String[][] runSelectQuery(String tableName, String field1, String field2)
  {
	if(db == null)return new String[0][0];
	String query = "SELECT " + field1 + "," + field2 + " FROM " + tableName;
	return db.executeSelect(query);	 
  }
  /**
   * This is just a convenience method to run a simple SELECT query
   * on just three fields in a table.
   * @param tableName
   * @param field1
   * @param field2
   * @param field3
   * @return A 2D String array
   */
  public String[][] runSelectQuery(String tableName, String field1, String field2, String field3)
  {
    if(db == null)return new String[0][0];  
	String query = "SELECT " + field1 + "," + field2 + "," + field3 + " FROM " + tableName;
	return db.executeSelect(query);	 
  }
  /**
   * This is just a convenience method to run a simple SELECT query
   * on just four fields in a table.
   * @param tableName
   * @param field1
   * @param field2
   * @param field3
   * @param field4
   * @return A 2D String array
   */
  public String[][] runSelectQuery(String tableName, String field1, String field2, String field3, String field4)
  {
	if(db == null)return new String[0][0];
	String query = "SELECT " + field1 + "," + field2 + "," + field3 + "," + field4 + " FROM " + tableName;
	return db.executeSelect(query);	 
  }
  
  private boolean isNumeric(String value)
  {
	  char ch;
	  for(int i = 0; i < value.length(); i++ )
	  {
		 ch = value.charAt(i);
		 if(!Character.isDigit(ch))
		 {
			 return false; 
		 }
	  }
	  return true;  
  }
  
}



