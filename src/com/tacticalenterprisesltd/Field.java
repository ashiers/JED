package com.tacticalenterprisesltd;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.log4j.Logger;

/**
 * To create an instance of this class you can add instances of class
 * Validate and DateFormat like so:<br><br>
 * <code>
 * Field field = new Field("TableName","ColumnName","JSonName");<br>
 * String[] arguments = new String[]{DateFormat.DATE_ISO_822};<br>
 * field.setValidator(new Validate(Validate.Type.DATE_FORMAT, arguments));<br>
 * field.setDateFormat(new DateFormat(field, DateFormat.DATE_ISO_822));<br>
 * </code>
 * <br>
 * Minimally, you need to provide both the Table name and the Column name of the field for the database when invoking any of the constructors.
 * Two constructors for Field allows you to provide a different name for the client side through the JSON string if so desired. 
 * <br><br>
 * <b>SPECIAL USE CASES:</b> There are times when you want to replace a field with a function call to the database. Or perhaps you wish to upload
 * a file to the database. The following use cases demonstrate how these are done.  If you want a function call in your
 * SQL statement, you can use the Field class to accomplish this task.  There are basically two scenarios: the default is to display a function
 * call for a SELECT query in the fields list.  The other is to display a function call for an INSERT or UPDATE statement. For the latter, you
 * would make sure to call the method assignFunctionForInsertOrUpdate(). Following are two examples.<br><br>
 * <b>Example 1:</b>&nbsp;<i>SELECT COUNT(*) FROM users;</i><br><br>
 * To ready the Field class for this example, you declare empty Strings to the constructor for both the table name and column name, then set
 * the value for the field as a String representation for the function. For output purposes to the JSON string you may in some cases need to
 * provide a column name.<br><br>
 * <code>
 * Field field1 = new Field("","",Field.Type.DBFUNCTION);<br>
 * field1.setValue("COUNT(*)", Field.DIRECTION.FROM_CLIENT);
 * </code><br><br>
 * <b>Example 2:</b>&nbsp;<i>UPDATE users SET users.my_timestamp=NOW() WHERE id=123;</i><br><br>
 * To ready the Field class for this example, we do want the names for the table and column since we are wanting to assign a value to the field
 * by making a function call.<br><br>
 * <code>
 * Field field1 = new Field("users","my_timestamp",Field.Type.DBFUNCTION);<br>
 * field1.setValue("NOW()", Field.DIRECTION.FROM_CLIENT);<br>
 * field1.assignFunctionForInsertOrUpdate();<br>
 * </code>
 * <br><b>Example 3:</b><br>
 * <br>Another scenario involves the uploading of Files to the database.  It is recommended you save files to the server's own file system, but
 * the option is here to allow you to save your files to the database itself. You set the Field to type FILE and set its InputStream member.
 * DO NOT USE the setValue(...) and getValue() methods! Use the setter and getter methods: setFileInputStream(InputStream is), setFileSizeInBytes(long size),
 * getFileInputStream(), getFileSizeInBytes().<br>
 * The Query object will detect a field of type FILE and pass to the database on inserts and updates the InputStream through the PreparedStatement
 * method setBinaryStream(int parameterIndex, InputStream x, long length).  Your database field should be set to one of these types: TINYBLOB, BLOB, MEDIUMBLOB, and LONGBLOB
 * and should allow a NULL value.
 * <br>
 * <code>
 * <br>org.apache.commons.fileupload.FileItem item = ...;<br>
 * Field field1 = new Field("files","content",Field.Type.FILE);<br>
 * field1.setFileInputStream(item.getInputStream());<br>
 * field1.setFileSizeInBytes(item.getSize());<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 *
 */

public class Field 
{
  public static enum DIRECTION {FROM_DB,FROM_CLIENT};
  public static enum Type {STRING,INT,FLOAT,DOUBLE,LONG,BIGDECIMAL,BOOLEAN,DATE,DBFUNCTION,FILE};
  private String tableName = "";
  private String parentTableAlias = "";
  private String dbField = "";
  private String name = "";
  private String stringValue = "";
  private Integer intValue = null;
  private Float floatValue = null;
  private Double doubleValue = null;
  private Long longValue = null;
  private Boolean boolValue = true;
  private String dbDateValue = "";
  private String clientDateValue = "";
  private BigDecimal bigdecimalValue = null;
  private String functionValue = "";
  private boolean write = true;  
  private boolean read = true;  
  private Validate validator = null;
  private DateFormat dateformat = null;
  private String chosenDatePattern = null;
  private Type fieldType = null;
  private boolean excludeOnOutput = false;
  private boolean hasSubstituteField = false;
  private Field substituteField = null;
  private Logger logger;
  private boolean displayFunctionForSelect = true;
  private Upload upload = null;
  private boolean hasUpload = false;
  private long fileSize = 0;
  private InputStream stream = null;
  
  /**
   * Constructor - the JSON name automatically gets the same name as that
   * given for the database column name. Ensure you use one of the enum
   * types provided with this class.
   * @param DBColumnName
   * @param ftype
   */
  public Field(String table, String DBColumnName, Field.Type ftype)
  {
	  if(table != null && !table.equals(""))
	     tableName = table;
	  if(DBColumnName != null && !DBColumnName.equals(""))
	  {
	     dbField = DBColumnName;
	     name = DBColumnName;
	  }
	  fieldType = ftype;
	  if(Editor.isLoggingEnabled())
	  {
		  logger = Logger.getLogger(Field.class);
	  }
  }
  
  /**
   * Constructor - you can assign a different name for the field when Output
   * sends out the JSON string.  Ensure you use one of the enum
   * types provided with this class.
   * @param DBColumnName
   * @param jsonname
   * @param ftype
   */
  public Field(String table, String DBColumnName, String jsonname, Field.Type ftype)
  {
	  if(table != null && !table.equals(""))
		     tableName = table;
	  if(DBColumnName != null && !DBColumnName.equals("") && jsonname.equals(""))
	  {
		 dbField = DBColumnName;
		 name = DBColumnName;
	  }
	  else
	  {
		  dbField = DBColumnName;
		  name = jsonname;
	  }
	  fieldType = ftype;
	  if(Editor.isLoggingEnabled())
	  {
		  logger = Logger.getLogger(Field.class);
	  }
  }
  
  /**
   * Set an Upload instance
   * @param upl
   */
  public void setUpload(Upload upl)
  {
	  upload = upl;
	  hasUpload = true;
  }
  
  /**
   * Get the Upload instance
   * @return Upload
   */
  public Upload getUpload()
  {
	  return upload;
  }
  /**
   * Determine if this instance of Field has an instance of Upload attached to it.
   * @return boolean
   */
  public boolean hasUpload()
  {
	  return hasUpload;
  }
  
  /**
   * This method is set in a special use case scenario where you have a field that references
   * a lookup table.  Typically this field is of type INT and would reference an <i>id</i> field
   * from a lookup table.  During an Insert or Update transaction the client side
   * would return an <i>id</i> value, but it is under the name of the lookup table and its field name.
   * Since a lookup table is READ ONLY, you can't perform an Insert or Update on it.  The lookup table
   * would be set on a Join whose property canWrite would be set to false, preventing any writing
   * to the table. By providing the name of that lookup table and field here, a redirection will be made and the value
   * returned from the client side will be stored in this field instead.  All you are doing is telling JED
   * to search for the parameter sent from the client side under the name of the lookup table and its field, and
   * place its value in this field.
   * @param field
   */
  public void setSubstituteField(Field field)
  {
	  substituteField = field;
	  hasSubstituteField = true;
  }
  /**
   * Get the Substitute Field
   * @return
   */
  public Field getSubstituteField()
  {
	  return substituteField;
  }
  /**
   * Inquire if this Field has a substitute Field
   * @return true or false
   */
  public boolean hasSubstituteField()
  {
	  return hasSubstituteField;
  }
  
  /**
   * At times you may not wish to include a field when processing Output.
   * In which case, set this to true;
   * @param value
   */
  public void setExcludeOnOutput(boolean value)
  {
	  excludeOnOutput = value;
  }
  /**
   * Get the value for ExcludeOnOutput
   * @return
   */
  public boolean getExcludeOnOutput()
  {
	  return excludeOnOutput;
  }
  
  /**
   * Set an alias for the Table
   * @param value a String value
   */
  public void setAliasParentTableName(String value)
  {
	  parentTableAlias = value;
  }
  /**
   * get the alias for the table
   * @return String
   */
  public String getAliasParentTableName()
  {
	  return parentTableAlias;
  }
  /**
   * Set the name of the table this field belongs to.
   * @param value
   */
  public void setTableName(String value)
  {
	  tableName = value;
  }
  /**
   * Get the name of the table this field belongs to.
   * @return
   */
  public String getTableName()
  {
	  return tableName;
  }
  /**
   * Get the field type
   * @return Field.Type
   */
  public Field.Type getFieldType()
  {
	  return fieldType;
  }
  /**
   * Set the field type.
   * @param ftype
   */
  public void setFieldType(Field.Type ftype)
  {
	  fieldType = ftype;
  }
  /**
   * Set a format generally having to do with Date type fields.
   * @param df
   */
  public void setDateFormat(DateFormat df)
  {
	  dateformat = df;
  }
  /**
   * Get the format generally having to do with Date type fields.
   * @return String
   */
  public DateFormat getDateFormat()
  {
	  return dateformat;
  }
  /**
   * Inquire if this field has a date format.
   * @return boolean
   */
  public boolean hasDateFormat()
  {
	  if(dateformat == null)
		  return false;
	  return true;
  }
  
  /**
   * Set the date pattern which should come from any of the constants defined
   * in the DateFormat class.
   * @param pattern A String pattern that will be used by SimpleDatePattern class
   */
  public void setDatePattern(String pattern)
  {
	  chosenDatePattern = pattern;
  }
  /**
   * Get the date pattern which would have come from any of the constants defined
   * in the DateFormat class.
   * @return A String
   */
  public String getDatePattern()
  {
	  return chosenDatePattern;
  }
  /**
   * Set the validator
   * @param val A Validate object
   */
  public void setValidator(Validate val)
  {
	  validator = val;
  }
  /**
   * Get the validator
   * @return A Validate object
   */
  public Validate getValidator()
  {
	  return validator;
  }
  public boolean hasValidator()
  {
	  if(validator == null)
		  return false;
	  return true;
  }
  /**
   * Set the name of the field as it is known on the database.
   * @param value A String
   */
  public void setDBFieldName(String value)
  {
	  dbField = value;
  }
  /**
   * Get the name of the field as it is known on the database.
   * @return A String
   */
  public String getDBFieldName()
  {
	  return dbField;
  }
  
  /**
   * Set the JSON name of this field.
   * @param value A String.
   */
  public void setName(String value)
  {
	  name = value;
  }
  /**
   * Get the JSON name of this field.
   * @return A String
   */
  public String getName()
  {
	  return name;
  }
  /**
   * Determine if we can write data from this field to the database or not.
   * @param data
   * @return A boolean value.
   */
  public boolean canWriteDataToDatabase()
  {
	 return write;
  }
  /**
   * Determine if we can read the value of this field from the database or not. 
   * @return
   */
  public boolean canReadDataFromDatabase()
  {
	  return read;
  }
  /**
   * This is a special method to handle the uploading of files.
   * @param is
   */
  public void setFileInputStream(InputStream is)
  {
	  stream = is;
  }
  /**
   * Get the input stream of the file that was uploaded.
   * @return
   */
  public InputStream getFileInputStream()
  {
	  return stream;
  }
  /**
   * Determine if this field has an InputStream from a file that was supposedly uploaded.
   * @return
   */
  public boolean hasInputStream()
  {
	  if(stream == null)
		  return false;
	  return true;
  }
  /**
   * Set the size of the file
   * @param size
   */
  public void setFileSizeInBytes(long size)
  {
	 fileSize = size; 
  }
  /**
   * Get the size of the file.
   * @return
   */
  public long getFileSizeInBytes()
  {
	  return fileSize;
  }
  
  /**
   * 9 times out of 10, when dealing with Date type fields, the DIRECTION of a query
   * is coming from the client side, and if the field is not a Date type, then 100%
   * of the time the DIRECTION is from the client side. Therefore, this is an overloaded method and is for
   * convenience calling the setValue(String val, Direction dir) method using a default of
   * DIRECTION.FROM_CLIENT as a parameter.
   * @param val
   */
  public void setValue(String val)
  {
	 setValue(val, DIRECTION.FROM_CLIENT);
  }
  
  /**
   * This Field needs to handle three cases: a value that is <b>not</b> a date,
   * a value that is a date coming from the database, and a date value coming
   * from the client side.
   * No matter from which direction the date value comes from, both the variables
   * dbDateValue and clientDateValue will be set simultaneously. If this is
   * not a Date type field, then we just set the value variable.
   * 
   * If this is a Date type field, there are appropriate GET methods for both
   * the database and the client side.
   *  
   * @param val A String
   * @param dir From which direction is the value coming from?
   */
  
  public void setValue(String val, DIRECTION dir)
  {	 
	 if(val == null || val.equals(""))return;
	 try{		 
	 
	   if(fieldType == Field.Type.DATE && dir == DIRECTION.FROM_DB)
	   {
		 dbDateValue = val;
	     clientDateValue = dateformat.date_sql_to_format(dbDateValue);
	   }
	   else if(fieldType == Field.Type.DATE && dir == DIRECTION.FROM_CLIENT)
	   {	 
	     clientDateValue = val;
	     dbDateValue = dateformat.date_format_to_sql(clientDateValue);
	   }
	   else if(fieldType == Field.Type.STRING)
	   {
		  stringValue = new String(val);
	   }
	   else if(fieldType == Field.Type.INT)
	   {
		  if(val != null)
		     intValue = new Integer(Integer.parseInt(val));
	   }
	   else if(fieldType == Field.Type.FLOAT)
	   {
		  if(val != null) 
		    floatValue = new Float(Float.parseFloat(val));
	   }
	   else if(fieldType == Field.Type.DOUBLE)
	   {
		  if(val != null)
		    doubleValue = new Double(Double.parseDouble(val));
	   }
	   else if(fieldType == Field.Type.LONG)
	   {
		  if(val != null)
		    longValue = new Long(Long.parseLong(val));
	   }
	   else if(fieldType == Field.Type.BIGDECIMAL)
	   {
		  if(val != null) 
		    bigdecimalValue = new BigDecimal(val);
	   }
	   else if(fieldType == Field.Type.BOOLEAN)
	   {
		  if(val != null)
		    boolValue = new Boolean(Boolean.parseBoolean(val));
	   }
	   else if(fieldType == Field.Type.DBFUNCTION)
	   {
		  if(val != null)  
		    functionValue = new String(val);
	   }
	   
	 }
	 catch(Exception e)
	 {
		if(Editor.isLoggingEnabled())
		{
		  if(fieldType == Field.Type.DATE && dateformat == null)
		  {
			  logger.error("\nYou set this Field: " + getName() + " to type DATE.\nYou must therefore set the DateFormat.\nFailure to do so will result in a\nNullPointerException at runtime.");
		  }
	      logger.error(Editor.getFullStackTrace(e));
		}
	 }
  }
  
  /**
   * Outgoing date is destined for the client-side.  NOTE: if retrieving a field of type FILE
   * you will be receiving an InputStream object.  DON'T FORGET TO CLOSE THE STREAM WHEN YOU'RE DONE WITH IT!!
   * @return
   */
  public Object getValue()
  {	
	  Object value = null;
	  if(fieldType == Field.Type.STRING)
	  {
		value = stringValue;
	  }
	  else if(fieldType == Field.Type.INT)
	  {
		  value = intValue;
	  }
	  else if(fieldType == Field.Type.FLOAT)
	  {
		  value = floatValue;
	  }
	  else if(fieldType == Field.Type.DOUBLE)
	  {
		  value = doubleValue;
	  }
	  else if(fieldType == Field.Type.LONG)
	  {
		  value = longValue;
	  }
	  else if(fieldType == Field.Type.BOOLEAN)
	  {
		  value = boolValue;
	  }
	  else if(fieldType == Field.Type.BIGDECIMAL)
	  {
		  value = bigdecimalValue;
	  }
	  else if(fieldType == Field.Type.DBFUNCTION)
	  {
		  value = functionValue;
	  }	  
	  else if(fieldType == Field.Type.DATE)
	  {
		  value = getClientDateValue();
	  }
	  
	  return value;
  }
  
  /**
   * Convenience wrapper for the BigDecimal.setScale(...) method.
   * @param newScale
   * @see BigDecimal.setScale(int newScale) in javadocs
   */
  public BigDecimal setBigDecimalScale(int newScale)
  {
	  return bigdecimalValue.setScale(newScale);
  }
  /**
   * Convenience wrapper for the BigDecimal.setScale(...) method.
   * @param newScale
   * @param roundingMode
   * @see BigDecimal.setScale(int newScale, int roundingMode) in javadocs
   */
  public BigDecimal setBigDecimalScale(int newScale, int roundingMode)
  {
	  return bigdecimalValue.setScale(newScale,roundingMode);
  }
  /**
   * Convenience wrapper for the BigDecimal.setScale(...) method.
   * @param newScale
   * @param roundingMode
   * @see BigDecimal.setScale(int newScale, RoundingMode roundingMode) in javadocs
   */
  public BigDecimal setBigDecimalScale(int newScale, RoundingMode roundingMode)
  {
	  return bigdecimalValue.setScale(newScale,roundingMode);  
  }
  
  public Object getDBDateValue()
  {	
	if(dbDateValue.equals(""))
		return new Integer(java.sql.Types.NULL);
	return dbDateValue;
  }
  
  public Object getClientDateValue()
  {	
	if(clientDateValue.equals(""))
	   return new Integer(java.sql.Types.NULL);
	return clientDateValue;
  }
  
  public void setWrite(boolean value)
  {
	  write = value;
  }
  
  public boolean getWrite()
  {
	  return write;
  }
  
  public void setRead(boolean value)
  {
	  read = value;
  }
  
  public boolean getRead()
  {
	  return read;
  }
  
  public void assignFunctionForInsertOrUpdate()
  {
	  displayFunctionForSelect = false;
  }
  @Override
  public String toString()
  {
	  if(fieldType == Field.Type.DBFUNCTION && displayFunctionForSelect)
	  {
		 return functionValue;
	  }
	  if(parentTableAlias.equals(""))
	  {		 
		 if(tableName.equals(""))
		 {			 
			 return dbField;
		 }
		 else
		 {			 
	         return tableName + "." + dbField;
		 }
	  }
	  return parentTableAlias + "." + dbField;
  }
}



