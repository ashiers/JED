package com.tacticalenterprisesltd;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import com.tacticalenterprisesltd.Database.RDBMS;


/**
 * The Query class is used internally by an instance of Editor.  It encapsulates
 * all the SQL (Structured Query Language) syntax required to interface with any
 * database that supports SQL. Limiting the number of records for Server Side Processing
 * is achievable on all versions of MySQL database, but only works with Oracle 
 * version 12c or higher.
 * @author Alan Shiers
 * @version 1.5.0
 */

public class Query
{
	public static enum Type {SELECT{
		public String toString()
		{
			return "SELECT ";
		}
	},INSERT{
		public String toString()
		{
		  return "INSERT ";	
		}
	},UPDATE{
	    public String toString()
	    {
	      return "UPDATE ";	
	    }
	},DELETE{
	    public String toString()
	    {
	      return "DELETE ";	
	    }
	}
	};
	
	public static enum FilterLogicalOperator {AND{
		 public String toString()
		 {
			 return " AND ";
		 }
	},OR{
		public String toString()
		{
			return " OR ";
		}
	}};
	
	protected final static String FROM = " FROM ";
	protected final static String WHERE = " WHERE ";
	protected final static String LIMIT = " LIMIT ";
	protected final static String ORDER = " ORDER BY ";
	protected Type type = Type.SELECT; //Default
	protected Database dbconn = null;
	protected String table = null;
	protected Field[] fields = null;
    protected WhereCondition[] where = null;
    protected WhereConditionGroups whereGroups = null;
	protected Order[] order = null;
	protected int limit = -1;
	protected int offset = -1;	
	private FilterLogicalOperator flo = FilterLogicalOperator.AND;//Default
	private int NewRecordID = -1;
	private String pKey = "id"; //Default
	private int iFilteredTotal = -1;
	private int iTotal = -1;
	private String parentTableAlias = "";
	private boolean isLinkTable = false;
	private Join[] joins = null;
	private Logger logger;
	
	
	public Query(Type t)
	{
		if(t != Type.SELECT) //SELECT IS THE DEFAULT
		   type = t;
		logger = Logger.getLogger(Query.class);
	}
	
	public Query(Type t, String tbl)
	{
		if(t != Type.SELECT) //SELECT IS THE DEFAULT
		   type = t;
		table = tbl;
		logger = Logger.getLogger(Query.class);
	}
		
	/**
	 * Inquire if the table is a Link Table
	 * @return
	 */
	public boolean isLinkTable()
	{
		return isLinkTable;
	}
	/**
	 * Set this property if the table being dealt with is a Link Table.
	 * @param value
	 */
	public void setAsLinkTable(boolean value)
	{
		isLinkTable = value;
	}
	
	/**
	 * Set an alias name for the Parent table.
	 * @param value
	 */
	public void setAliasParentTableName(String value)
	{
	  parentTableAlias = value;
	}
	/**
	 * Get the alias name of the Parent table  
	 * @return
	 */
	public String getAliasParentTableName()
	{
	  return parentTableAlias;
	}
	/**
	 * Set the name of the Primary Key field of the Parent table.
	 * @param value
	 */
	public void setPrimaryKey(String value)
	{
		pKey = value;
	}
	/**
	 * Get the name of the Primary Key field of the Parent table.
	 * @return A String
	 */
	public String getPrimaryKey()
	{
		return pKey;
	}
	/**
	 * Set the value for iFilteredTotal.
	 * @param value
	 */
	public void setIFilteredTotal(int value)
	{
		iFilteredTotal = value;
	}
	/**
	 * Get the value of iFilteredTotal.
	 * @return An int
	 */
	public int getIFilteredTotal()
	{
		return iFilteredTotal;
	}
	/**
	 * Set the value for iTotal.
	 * @param value
	 */
	public void setITotal(int value)
	{
		iTotal = value;
	}
	/**
	 * Get the value of iTotal
	 * @return An int
	 */
	public int getITotal()
	{
		return iTotal;
	}
	/**
	 * Set an array of Joins
	 * @param joinarray
	 */
	public void setJoins(Join[] joinarray)
    {
        joins = joinarray;
    }
    /**
     * Get the array of Joins
     * @return
     */
    public Join[] getJoins()
    {
        return joins;
    }
	
	/**
	 * Set the ID value obtained after a new record
	 * has been inserted into a table.
	 * @param value An int value
	 */
	public void setNewRecordID(int value)
	{
		NewRecordID = value;
	}
	/**
	 * Get the ID value obtained after a new record
	 * has been inserted into a table.
	 * @return String
	 */
	public int getNewRecordID()
	{
		return NewRecordID;
	}
	/**
	 * Set a reference to an instance of the Database class.
	 * @param db
	 */
	public void setDatabase(Database db)
	{
		dbconn = db;
	}
	/**
	 * Get a reference to an instance of the Database class.
	 * @return Database
	 */
	public Database getDatabase()
	{
		return dbconn;
	}
	/**
	 * Set the Fields to be used in this query.	
	 * @param flds
	 */
	public void setFields(Field[] flds)
	{
		fields = flds;
	}
	/**
	 * Get the Fields being used in this query. This will include any fields with any joins whose excludeFromSelect flag is not set to true.
	 * @return Field[] array
	 */
	public Field[] getFields()
	{
		if (joins == null) return fields;

        ArrayList<Field> list = new ArrayList<Field>();
        
        if(joins != null)
        {
            for(int i = 0; i < fields.length; i++)
            {
                list.add(fields[i]);
            }

            Join jn = null;
            Field[] flds = null;
            
            //combine the fields inside the joins with the parent table fields
            for(int k = 0; k < joins.length; k++)
            {
                jn = joins[k];
                if (jn.getExcludeOnSelect() == false)
                {
                    flds = jn.getFields();
                    for (int l = 0; l < flds.length; l++)
                    {
                        list.add(flds[l]);
                    }
                }
            }

        }
        Field[] flds = new Field[list.size()];
        return list.toArray(flds);
	}
	
	protected String[] getFieldsAsStringArray()
	{
		String[] temp = new String[fields.length + 1];
		temp[0] = pKey;
		int count = 1;
		for(int i = 0; i < fields.length; i++)
		{
			temp[count] = fields[i].getDBFieldName();
			count++;
		}
		return temp;
	}
	
	protected String[] getFieldsAsStringArrayWithoutID()
	{
		String[] temp = new String[fields.length];		
		
		for(int i = 0; i < fields.length; i++)
		{
			temp[i] = fields[i].getDBFieldName();			
		}
		return temp;
	}
	
	/**
	 * Set the Order - example: ORDER BY <i>fieldName</i> asc
	 * @param value An Order[] array
	 */
	public void setOrder(Order[] value)
	{
		order = value;
	}
	/**
	 * Set the logical operator as part of the WHERE filter when you have 
	 * multiple conditions.<br>
	 * Example: WHERE <i>field1Name</i> = <i>someValue</i> AND <i>field2Name</i> = <i>someOtherValue</i>
	 * The default is the AND operator. You can change it to OR.
	 * Please use the enum provided in this class.
	 * 
	 * @param operator
	 */
	public void setFilterLogicalOperator(FilterLogicalOperator operator)
	{
		if(flo != operator)
		   flo = operator;  
	}
	/**
	 * Get the logical operator being used as part of the WHERE filter.
	 * @return A FilterLogicalOperator
	 */
	public FilterLogicalOperator getFilterLogicalOperator()
	{
		return flo;
	}
	/**
	 * Set the Query Type.
	 * @param t
	 */
	public void setQueryType(Type t)
	{
		type = t;
	}
	/**
	 * Get the Query Type.
	 * @param tbl
	 */
	public void setTable(String tbl)
	{
		table = tbl;
	}
	/**
	 * Get the tables being used in this query
	 * @return
	 */
	public String getTable()
	{
		return table;
	}
		
	public String getParentTable()
	{
		return table;
	}
	
	/**
	 * Get the Query Type.
	 * @return Type
	 */
	public Type getQueryType()
	{
		return type;
	}
	/**
	 * Set all the WhereConditions for this query.
	 * @param whereclauses
	 */
	public void setWhereConditions(WhereCondition[] whereclauses)
	{
		where = whereclauses;
	}
	/**
	 * Set groups of WhereConditions for this query.
	 * @param groups
	 */
	public void setWhereConditionGroups(WhereConditionGroups groups)
	{
		whereGroups = groups;
	}
	/**
	 * Get the array of WhereCondition(s)
	 * @return
	 */
	public WhereCondition[] getWhereConditions()
	{
		return where;
	}
	/**
	 * Inquire if there is any Where conditions
	 * @return true or false
	 */
	public boolean haveWhereConditions()
	{
		if(where == null)
			return false;
		return true;
	}
	//This just delimits any elements in an array with commas
	//such as the list of fields and tables
	private String prepare(Object[] array)
	{
		if (array == null) return "";
        String temp = "";
        for (int i = 0; i < array.length; i++)
        {
            temp += array[i].toString() + ",";
        }
        if (joins != null)
        {
            Field fld = null;
            Field[] fields = null;
            Join jn = null;
            for (int i = 0; i < joins.length; i++)
            {
                jn = joins[i];
                if (jn.getExcludeOnSelect() == false)
                {
                    fields = jn.getFields();
                    for (int j = 0; j < fields.length; j++)
                    {
                        fld = fields[j];
                        temp += fld.toString() + ",";
                    }
                }
            }
        }
        temp = temp.substring(0, temp.length() - 1);
        return temp;
	}
	
	private String prepareFields(Object[] array)
	{
		if(array == null)return "";
		String temp = "";		
		for(int i = 0; i < array.length; i++)
		{
			temp += array[i].toString() + ",";
		}
		temp = temp.substring(0, temp.length() - 1);
		return temp;
	}
	
	private String prepareFieldsWithID(Object[] array)
	{
		if(array == null)return "";
		String temp = "";
		String idField = getParentTable() + "." + pKey;
		temp = idField + ",";
		for(int i = 0; i < array.length; i++)
		{
			temp += array[i].toString() + ",";
		}
		temp = temp.substring(0, temp.length() - 1);
		return temp;
	}
	
	protected String prepareTable()
	{
		if(table == null)return "";		
		
		return table;
	}
	
	protected String prepareJoins(Join[] array)
    {
        if (array == null) return "";

        String temp = "";
        Join jn = null;
        for(int i = 0; i < array.length; i++)
        {
            jn = array[i];
            if(jn.getExcludeOnSelect() == false)
               temp += jn.toString();
        }

        return temp;
    }
	
		
	protected String prepareValues()
	{
		if(fields == null)return "";
		String temp = "";
		//The id field value needs to be inserted automatically by RDBMS, so we just provide a dummy value here.
		if(dbconn == null)
		{
			logger.error("You didn't provide a reference to the Database by calling Query.setDatabase(Database db).");
		}
		try
		{
		  if(dbconn.dbtype == Database.RDBMS.MYSQL)
			     temp = "0,";		         
		  else if(dbconn.dbtype == Database.RDBMS.ORACLE)
			     temp = getParentTable().toUpperCase() + "_SEQ.NEXTVAL,";
		  for(int i = 0; i < fields.length; i++)
		  {
			 temp += "?,"; 
		  }
		  temp = temp.substring(0, temp.length() - 1);		  
		}
        catch (Exception e) { logger.error(e); }
		return temp;
	}
	
		
	protected String prepareLinkTableValues()
	{
		if(fields == null)return "";
		String temp = "";
		for(int i = 0; i < fields.length; i++)
		{
			temp += "?,";
		}
		temp = temp.substring(0, temp.length() - 1);
		
		return temp;
	}
	
	//This prepares the WHERE clause 
	protected String prepareWhere()
	{
		if(where == null && whereGroups == null)return "";
		String temp = WHERE;
		if(whereGroups != null)
		{
		   where = whereGroups.getAllWhereConditions();		
		   temp += whereGroups.toString();
		}
		else
		{
		  //Process only the where array
		  for(int i = 0; i < where.length; i++)
		  {
			temp += where[i].toString() + flo.toString();
		  }
		  temp = temp.substring(0, temp.length() - flo.toString().length());
		}
		return temp;
	}
	
	//This prepares the WHERE clause with their known values
	protected String prepareWhereWithValues(WhereCondition[] array)
	{
		if(array == null)return "";
		String temp = WHERE;
		for(int i = 0; i < array.length; i++)
		{
			temp += array[i].toStringWithValues() + flo.toString();
		}
		temp = temp.substring(0, temp.length() - flo.toString().length());
		return temp;
	}
	
	//This prepares the ORDER BY clause specifically.
	protected String prepareOrder()
	{
		if(order == null)return "";
		String temp = ORDER;		
		for(int i = 0; i < order.length; i++)
		{
			temp += order[i].toString() + ",";
		}
		temp = temp.substring(0, temp.length() - 1);
		
		return temp;	
	}
	//This prepares the LIMIT clause specifically.
	//This method only works with Oracle version 12c or higher
	protected String prepareLimit(int start, int recordsToReturn)
	{
		if(recordsToReturn == -1 && start == -1)return "";
		String temp = "";
		if(dbconn.dbtype == Database.RDBMS.MYSQL)
		{
		  if(start == 0 && recordsToReturn > -1)
		     temp = LIMIT + recordsToReturn;
		  else if(start > -1 && recordsToReturn > -1)
			 temp = LIMIT + start + ", " + recordsToReturn;
		}
		else if(dbconn.dbtype == Database.RDBMS.ORACLE)
		{
			if(start == 0 && recordsToReturn > -1)
			     temp = " FETCH FIRST " + recordsToReturn + " ROWS ONLY";
			  else if(start > 0 && recordsToReturn > -1)
				 temp = " OFFSET " + start + " ROW FETCH NEXT " + recordsToReturn + " ROWS ONLY";	
		}
		else if(dbconn.dbtype == Database.RDBMS.SQLSERVER)
        {
            temp = " OFFSET " + start + " ROW FETCH NEXT " + recordsToReturn + " ROWS ONLY";
        }
		return temp;
	}
	
	
	//The way this is supposed to work is that before calling this method,
	//at some point the fields should have been iterated over and the field
	//values changed.
	protected String prepareSet(Field[] flds)
	{
		if(flds.length == 0) return "";
		String set = "";
		Field field = null;
		for(int i = 0; i < flds.length; i++)
		{
			field = flds[i];
			set += field.getDBFieldName() + "=?,";
		}
		set = set.substring(0, set.length() - 1);
		
		return set;
	}
	
	private void setTableAliasForFields(Field[] fields, String alias)
    {
        Field fld = null;
        for(int i = 0; i < fields.length; i++)
        {
            fld = fields[i];
            fld.setAliasParentTableName(alias);
        }
    }
	
	/**
	 * Create the SQL query string to select one or more
	 * rows of data from the database.
	 * @return
	 */
	private String AssembleSelect()
	{	
		if(fields == null)
		{
			logger.error("You didn't set the array of Fields for the Query object.");
		}
		if(table == null)
		{
			logger.error("You didn't set the array of Tables for the Query object.");
		}
		if(joins != null)
        {
            Join jn = null;
            //Check to see if the table alias name was set
            for(int i = 0; i < joins.length; i++)
            {
                jn = joins[i];
                //If the table alias was set, then set the same alias for all the Fields in the Join
                if(!jn.getTableAlias().equals(""))
                {
                    setTableAliasForFields(jn.getFields(), jn.getTableAlias());
                }
            }
            
        }
		return type.toString() + prepare(fields) + FROM + prepareTable() + parentTableAlias + prepareJoins(joins) + prepareWhere() + prepareOrder() + prepareLimit(offset,limit);
	}
	/**
	 * Create the SQL query string to insert a new row
	 * of data into the database.
	 * @return String
	 */
	private String AssembleInsert()
	{ 
		String query = "";
		if(fields == null)
		{
			logger.error("You didn't set the array of Fields for the Query object.");
		}
		if(table == null)
		{
			logger.error("You didn't set the parent Table for the Query object.");
		}
		
		if(isLinkTable == false)
		{
			if (dbconn.dbtype == Database.RDBMS.SQLSERVER)
            {
                query = type.toString() + "INTO " + prepareTable() + " (" + prepareFields(fields) + ") VALUES (" + prepareValues() + "); SELECT SCOPE_IDENTITY() AS ID;";
            }
            else
            {              	
                query = type.toString() + "INTO " + prepareTable() + " (" + prepareFieldsWithID(fields) + ") VALUES (" + prepareValues() + ")";                
            }
        }
		else
			query = type.toString() + "INTO " + prepareTable() + " (" + prepareFields(fields) + ") VALUES (" + prepareLinkTableValues() + ")";
		return query;
	}
	/**
	 * Create the SQL query string to update an existing
	 * row of data in the database.
	 * @return String
	 */
	private String AssembleUpdate()
	{
		if(fields == null)
		{
			logger.error("You didn't set the array of Fields for the Query object.");
		}
		if(table == null)
		{
			logger.error("You didn't set the array of Tables for the Query object.");
		}
		return type.toString() + prepareTable() + " SET " + prepareSet(fields) + prepareWhere();
	}
	/**
	 * Create the SQL query string to delete a row of data
	 * from the database.
	 * @return String
	 */
	private String AssembleDelete()
	{
		if(table == null)
		{
			logger.error("You didn't set the parent Table for the Query object.");
		}
		return type.toString() + FROM + prepareTable() + prepareWhere();
	}
	/**
	 * Set the Offset on a SELECT query when limiting the number of records
	 * to be returned.
	 * @param value
	 */
	public void setOffset(int value)
	{
		offset = value;
	}
	/**
	 * Get the Offset on a SELECT query when limiting the number of records
	 * to be returned.
	 * @return
	 */
	public int getOffset()
	{
		return offset;
	}
	/**
	 * Set the Limit on a SELECT query when limiting the number of records
	 * to be returned.
	 * @param value
	 */
	public void setLimit(int value)
	{
		limit = value;
	}
	/**
	 * Get the Offset on a SELECT query when limiting the number of records
	 * to be returned.
	 * @return
	 */
	public int getLimit()
	{
		return limit;
	}
	
	protected PreparedStatement getPreparedStatement(Connection connection)throws SQLException
	{
		PreparedStatement statement = null;
		Type qtype = getQueryType();
		if(qtype == Query.Type.INSERT)
        {  
    	  if(dbconn.dbtype == RDBMS.MYSQL)
          {   
    		if(isLinkTable() == false) 
    		{
    	      statement = connection.prepareStatement(toString(), PreparedStatement.RETURN_GENERATED_KEYS);
    	      setPreparedStatementParams(statement);    	      
    		}
    		else
    		{
    		  statement = connection.prepareStatement(toString());
    		  setPreparedStatementParams(statement);    		  
    		}
          }
    	  else if(dbconn.dbtype == RDBMS.ORACLE)
          {
    		  if(isLinkTable() == false)
    		  {
    		     //statement = connection.prepareStatement(toString(), getFieldsAsStringArray());
    			 statement = connection.prepareStatement(toString(), new String[]{this.getPrimaryKey()});
    		     setPreparedStatementParams(statement);    		     
    		  }
    		  else
    		  {
    			 statement = connection.prepareStatement(toString(), getFieldsAsStringArrayWithoutID());
    			 setPreparedStatementParams(statement);    			 
    		  }
          }
    	  else if(dbconn.dbtype == RDBMS.SQLSERVER)
          {
    		  if(isLinkTable() == false)
    		  {
    			  statement = connection.prepareStatement(toString(), PreparedStatement.RETURN_GENERATED_KEYS);
        	      setPreparedStatementParams(statement);        	      
    		  }
        	  else
        	  {
        		  statement = connection.prepareStatement(toString());
        		  setPreparedStatementParams(statement);        		  
        	  }
          }    	  
        }
    	else if(qtype == Query.Type.UPDATE)
    	{
    	  //This is just for UPDATES
    	  statement = connection.prepareStatement(toString());
    	  setPreparedStatementParams(statement);    	  
    	}
    	else if(qtype == Query.Type.DELETE)
    	{
    	  //This is just for DELETES
    	  statement = connection.prepareStatement(toString());
    	  setPreparedStatementParams(statement);    	  
    	}
    	else if(qtype == Query.Type.SELECT)
    	{
    	  //This is just for SELECTS
    	  statement = connection.prepareStatement(toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    	  setPreparedStatementParams(statement);    	  
    	}
		
		if(Editor.isLoggingEnabled())
			 logger.debug("\nQuery.getPreparedStatement: " + statement.toString() + "\n");
		
		return statement;
	}
	
	private void setPreparedStatementParams(PreparedStatement statement)throws SQLException
	{
		
		 int index = 1;
		 Type qtype = getQueryType();
		 String outValues = "";
		 Field fld = null;
		 if(qtype == Query.Type.INSERT || qtype == Query.Type.UPDATE)
		 {
		   outValues += "PreparedStatement SQL Parameters:\n";
		   if(fields.length > 0)
		   {
			 try
			 {
		       for(int i = 0; i < fields.length; i++)
	           {
	    	     //Field.Types: {STRING,INT,FLOAT,DOUBLE,LONG,BIGDECIMAL,BOOLEAN,DATE,DBFUNCTION,IMAGE}
		    	 fld = fields[i];
	    	     Field.Type value = fld.getFieldType();
	    	     Object obj = fld.getValue();
	    	     switch (value)
	    	     {
	    	        case STRING:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	     statement.setString(index, ((String)obj));
	    	    	     outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	     index++;
	    	    	    }
	    	    	    break;
	    	        case INT:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	      statement.setInt(index, ((Integer)obj));
	    	    	      outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	      index++;
	    	    	    }
	    	    	    break;
	    	        case FLOAT:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	      statement.setFloat(index, ((Float)obj));
	    	    	      outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	      index++;
	    	    	    }
	    	    	    break;
	    	        case DOUBLE:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	      statement.setDouble(index, ((Double)obj));
	    	    	      outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	      index++;
	    	    	    }
	    	    	    break;
	    	        case LONG:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	      statement.setLong(index, ((Long)obj));
	    	    	      outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	      index++;
	    	    	    }
	    	    	    break;
	    	        case BIGDECIMAL:
	    	    	    if(obj == null)
	    	    	    {
	    	    		  statement.setNull(index, java.sql.Types.NULL);
	    	    		  outValues += "\tnull\n";
	    	    	    }
	    	    	    else
	    	    	    {
	    	    	      statement.setBigDecimal(index, ((BigDecimal)obj));
	    	    	      outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	      index++;
	    	    	    }
	    	    	    break;
	    	        case BOOLEAN:
	    	    	    statement.setBoolean(index, ((Boolean)obj));
	    	    	    outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	    index++;
	    	    	    break;
	    	        case DATE:
	    	    	    //We don't want to use the setDate(...) method as it expects
	    	    	    //a java.sql.Date returned which doesn't allow for any time stamp.
	    	    	    //Let the database perform the conversion from String to Date type.
	    	    	    if(fld.getDBDateValue() instanceof Integer)
	 				    {
	 				      Integer val = (Integer)fld.getDBDateValue();
	 				      if(val == java.sql.Types.NULL)
	 				      {
	 					     statement.setString(index, "null");
	 					     outValues += "\t" + "null\n";
	 				      }
	 				    }
	 				    else
	 				    {
	 				   	   statement.setString(index, ((String)fld.getDBDateValue()));
	 					   outValues += "\t" + String.valueOf(fld.getDBDateValue()) + "\n";
	 				    }
	    	    	    index++;
	    	    	    break;
	    	        case DBFUNCTION:
	    	    	    statement.setString(index, ((String)obj));
	    	    	    outValues += "\t" + String.valueOf(obj) + "\n";
	    	    	    index++;
	    	    	    break;	
	    	        case FILE:
	    	    	    if(fld.hasInputStream())
	    	    	       statement.setBinaryStream(index, fld.getFileInputStream(),fld.getFileSizeInBytes());
	    	    	    else
	    	    		   statement.setNull(index, java.sql.Types.NULL);
	    	    	    index++;
	    	    	    break;
	    	       }
	            }
			 }
			 catch(ClassCastException cce)
			 {
				 if(Editor.isLoggingEnabled())
				 {
					 if(fld != null)
						 logger.error("Field: " + fld.getName());
		    		 logger.error(Editor.getFullStackTrace(cce));
				 }
			 }
		   }
		}
		if(Editor.isLoggingEnabled())
	  	{
			if(!outValues.isEmpty())
			   logger.debug(outValues);
	  	} 
	    if(haveWhereConditions())
   	    {
	      try
	      {
	        for(WhereCondition wc : where)
   		    {
	    	  if(wc.toString().contains("?") == false)
	    		  break;
	    	  //Getting the actual field becomes important is we have a ClassCastException
	    	  fld = wc.getKey();
	    	  Field.Type value = wc.getKey().getFieldType();
	    	  Object obj = wc.getValue();
	    	  switch (value)
	    	  {
	    	     case STRING:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	   statement.setString(index, ((String)obj));
	    	    	   index++;
	    	    	 }
	    	    	 break;
	    	     case INT:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	     statement.setInt(index, ((Integer)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case FLOAT:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	   statement.setFloat(index, ((Float)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case DOUBLE:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	     statement.setDouble(index, ((Double)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case LONG:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	     statement.setLong(index, ((Long)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case BIGDECIMAL:
	    	    	 statement.setBigDecimal(index, ((BigDecimal)obj));
	    	    	 index++;
	    	    	 break;
	    	     case BOOLEAN:
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	     statement.setBoolean(index, ((Boolean)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case DATE:
	    	    	 //We don't want to use the setDate(...) method as it expects
	    	    	 //a java.sql.Date returned which doesn't allow for any time stamp.
	    	    	 //Let the database perform the conversion from String to Date type.
	    	    	 if(obj == null)
	    	    	 {
	    	    		 statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 }
	    	    	 else
	    	    	 {
	    	    	     statement.setString(index, ((String)obj));
	    	    	 }
	    	    	 index++;
	    	    	 break;
	    	     case DBFUNCTION:
	    	    	 if(obj == null)
	    	    	     statement.setNull(index, java.sql.Types.NULL);	    	    		 
	    	    	 else if(obj instanceof Integer)
	    	    		 statement.setInt(index, ((Integer)obj));
	    	    	 else if(obj instanceof Float)
	    	    		 statement.setFloat(index, ((Float)obj));
	    	    	 else if(obj instanceof Double)
	    	    		 statement.setDouble(index, ((Double)obj));
	    	    	 else if(obj instanceof Long)
	    	    		 statement.setLong(index, ((Long)obj));
	    	    	 else if(obj instanceof BigDecimal)
	    	    		 statement.setBigDecimal(index, ((BigDecimal)obj));
	    	    	 else if(obj instanceof Boolean)
	    	    		 statement.setBoolean(index, ((Boolean)obj));
	    	    	 else if(obj instanceof Date)
	    	    	     statement.setString(index, ((String)obj));
	    	    	 else if(obj instanceof String)
	    	    		 statement.setString(index, ((String)obj));
	    	    	 index++;
	    	    	 break;
	    	     
	    	    }   			
   		      }
	      }
	      catch(ClassCastException cce)
	      {
	    	  if(Editor.isLoggingEnabled())
			  {
	    		  if(fld != null)
					 logger.error("Field: " + fld.getName());	    		  
				  logger.error(Editor.getFullStackTrace(cce));
			  }
	      }
   	    }	  
	}
	
	/**
	 * Get the actual query produced by this class. It will produce either
	 * an INSERT,UPDATE,DELETE, or SELECT query statement.
	 * @return An SQL String
	 */
	@Override
	public String toString()
	{
		String statement = "";
		if(type == Type.SELECT)
		{
			return AssembleSelect();
		}
		else if(type == Type.INSERT)
		{
			return AssembleInsert();
		}
		else if(type == Type.UPDATE)
		{
			return AssembleUpdate();
		}
		else if(type == Type.DELETE)
		{
			return AssembleDelete();
		}
		return statement;
	}
}

