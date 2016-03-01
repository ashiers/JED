package com.tacticalenterprisesltd;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

/**
 * The Database class performs all the necessary functions required to SELECT, UPDATE, INSERT, and DELETE records.<br>
 * This class supports the following database management systems: 
 * <ul>
 * <li>MySql (default) v5.0 or higher</li>
 * <li>Oracle v12c or higher</li>
 * <li>SQL Server v2012 or higher</li>
 * </ul>
 * <p>The JED library of classes support DataTables on the server side for full CRUD (Create,Read,Update and Delete) operations, and are expected to be used in a Java environment
 * using a servlet container such as Tomcat, or Glassfish.</p>
 * <h2>Setup:</h2>
 * This library of classes
 * are expected to be used in a JEE environment, and as such, it is 
 * assumed that you will be setting up an XML file for the Servlet
 * Container that describes the necessary parameters required to obtain
 * a database connection from a connection pool.  The XML file I am referring
 * to is generally named: <b>context.xml</b>.  This file should reside in a folder
 * named: <b>META-INF</b>.  The context.xml file would look similar to this:<br><br>
 * <blockquote>
 * <code>
 * &lt;!-- New Context for MySQL Database Driver --&gt;<br>
 * &lt;!-- The following URL will point you to the correct syntax regarding CONTEXTs:<br>
 *    http://jakarta.apache.org/tomcat/tomcat-5.5-doc/config/context.html --&gt;<br>
 * &lt;Context path="/JQuery" docBase="JQuery" debug="0"<br>
 * reloadable="true" crossContext="true"&gt;<br>
 *
 * &lt;!--Logger className="org.apache.catalina.logger.FileLogger"<br>
 *  prefix="localhost_jquery_log." suffix=".txt"<br>
 *  timestamp="true"/ --&gt;<br>
 * 
 *  &lt;Valve className="org.apache.catalina.valves.AccessLogValve"<br>
 *        prefix="localhost_jquery_log." suffix=".txt"<br>
 *        pattern="common"/&gt;<br>
 *
 *
 * &lt;Resource name="jdbc/fcs_db" auth="Container" type="javax.sql.DataSource" <br> 
 *  username="webapp" password="secret" driverClassName="com.mysql.jdbc.Driver"<br>
 *  url="jdbc:mysql://localhost:3306/fcs_db?autoReconnect=true"<br>
 *  maxActive="10" maxIdle="3" maxWait="10000"/&gt; <br>
 * 
 * &lt;/Context&gt;<br>
 * </code>
 * </blockquote>
 * This context.xml example is for reference only. It is being used to access a
 * MySQL database. This file was being used in a
 * Servlet Container named <a href="http://tomcat.apache.org/" target="main">Tomcat 7.0</a>. You will need to change the parameters
 * for your particular setup. Consult the documentation for your Servlet Container for
 * more information.  Your database drivers (contained in a jar file) for your particular system should reside
 * in the <b>WEB-INF\lib</b> folder. In our case, we were using the <i>mysql-connector-java-5.1.18.jar</i>.<br><br>
 * 
 * Your basic directory structure for your application should look like this:<br><br>
 * [home_dir]<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;index.html<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|_ media<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;|_ css<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;|_ extensions<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;|_ js<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;|_ images<br> 
 * &nbsp;&nbsp;&nbsp;&nbsp;|_ META-INF<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ context.xml<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;|_WEB-INF<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ classes<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ lib<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ jed-1.#.jar<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ gson-2.2.4.jar<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ log4j-1.2.8.jar<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_ mysql-connector-java-5.1.18.jar<br><br>
 * <b>Usage Example:</b><br><br>
 * <code>
 * String dbName = "fcs_db";<br>
 * String[] tableNames = new String[]{"employees"};<br>
 * //Set the Primary Key field<br>
 * String pKey = "ID";<br>
 * Parameters params = new Parameters();<br>
 * //Acquire all the parameters passed to the Java Server Page<br>
 * ...<br>
 * Database db = new Database(dbName);<br>
 * Editor editor = new Editor(db, tableNames, pKey, params);<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 */

public class Database
{
	public String name = "";
	public Query query = null;
	private Editor reference = null;
	private Logger logger;
	private Context ctx = null;    
	private DataSource ds = null;
	public static enum RDBMS {MYSQL,ORACLE,SQLSERVER};	
	protected RDBMS dbtype = RDBMS.MYSQL;
	private boolean datasourceException = false;
	private boolean connectionException = false;
	
	/**
	 * The default RDBMS type is MYSQL. If you want to specify some other RDBMS,
	 * use the other constructor.
	 * @param dbName
	 */
	public Database(String dbName)
	{
		try{
		name = dbName;
		logger = Logger.getLogger(Database.class);
		ctx = new InitialContext();
		setDataSource(ctx);
		}
		catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
	      {			
	        logger.error("Database constructor: " + Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	  System.out.println(Editor.getFullStackTrace(e));
	      }
	    }
	}
	/**
	 * Use this constructor if you want to specify an RDBMS other than MYSQL.
	 * @param dbName
	 * @param type
	 */
	public Database(String dbName, RDBMS type)
	{
		try{
		name = dbName;
		dbtype = type;
		logger = Logger.getLogger(Database.class);
		ctx = new InitialContext();
		setDataSource(ctx);
		}
		catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
	      {			
	        logger.error("Database constructor: " + Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	  System.out.println(Editor.getFullStackTrace(e));
	      }
	    }
	}
	
	@Resource
	private void setDataSource(Context context)
	{
		try{
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/" + name); 
		}
		catch(Exception e)
	    {
		  datasourceException = true;
	      if(Editor.isLoggingEnabled())
	      {			
	        logger.error("Database.createDataSource(): " + Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	  System.out.println(Editor.getFullStackTrace(e));
	      }
	    }
	}
	
	/**
	 * Set a reference to an instance of the Editor class
	 * @param ref
	 */
	public void setEditorReference(Editor ref)
	{
		reference = ref;			
	}
	/**
	 * Set a reference to an instance of the Query class
	 * @param q
	 */
	public void setQuery(Query q)
	{
		query = q;
		query.setDatabase(this);
	}
	/**
	 * This form of the getConnection method is made available for convenience and does <b>NOT</b> attempt to derive a Connection from a connection pool.
	 * Instead, it obtains a Connection by accessing a JDBC Driver directly so that you may connect to a different database other than the one that the
	 * servlet container has pooled.<br><br>
	 * Example for connection to MySQL:<br><br>
	 * <pre>getConnection(Database.RDBMS.MYSQL,"org.gjt.mm.mysql.Driver","localhost","3306","scott", "tiger")</pre><br>
	 * Example for connection to Oracle:<br><br>
	 * <pre>getConnection(Database.RDBMS.ORACLE,"oracle.jdbc.OracleDriver","localhost","1521","scott","tiger")</pre><br><br>
	 * Example for connection to SQL SERVER:<br><br>
	 * <pre>getConnection(Database.RDBMS.SQLSERVER,"com.microsoft.sqlserver.jdbc.SQLServerDriver","localhost","1433","scott","tiger")</pre><br><br>
	 * Don't forget to close the connection when you are done with it. 
	 * @param type
	 * @param classForName
	 * @param URL
	 * @param PORT
	 * @param UserName
	 * @param Password
	 * @return Connection
	 */
	public Connection getConnection(RDBMS type,String classForName,String URL,String PORT,String UserName, String Password)
	{
		Connection connection = null;
		String connectionString = "jdbc:";
		
		try
	    {
	      if(type == RDBMS.MYSQL)
	      {	    	  
	    	  connectionString += "mysql://" + URL + ":" + PORT + "/" + name;
	      }
	      else if(type == RDBMS.ORACLE)
	      {
	    	  connectionString += "oracle:thin:@" + URL + ":" + PORT + ":" + name;
	      }
	      else if(type == RDBMS.SQLSERVER)
	      {
	    	  connectionString += "sqlserver://" + URL + ":" + PORT + "/" + name;
	      }
	      Class.forName(classForName).newInstance();
		  connection = DriverManager.getConnection(connectionString, UserName, Password);
	    }
		catch(SQLException sqle)
	    {
		  String extra = "SQL Problem: " + sqle.getMessage() + "\n";
		  extra += "SQL State: " + sqle.getSQLState() + "\n";
		  extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
		  if(Editor.isLoggingEnabled())
		  {			  	      
		      logger.error(extra + Editor.getFullStackTrace(sqle));
		  }
		  else
	      {
	    	  System.out.println(extra + Editor.getFullStackTrace(sqle));
	      }
	    }
	    catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
	      {			
	        logger.error("Database.getConnection(): " + Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	  System.out.println(Editor.getFullStackTrace(e));
	      }
	    }
	    
	    return connection;
	}
	/**
	 * Obtain a Connection to perform a query. This method first
	 * creates a Context and performs a lookup on the system to obtain a DataSource object.
	 * From the DataSource, we return a Connection.  To use this method, it is assumed
	 * you have created a context.xml file in the META-INF directory. 
	 * <br><br>It is this method that is called directly by the methods:<br><br>
	 * executeSelect(), executeSelect(String strQuery),<br>
	 * executeInsertUpdate(),executeDeletes(Query[] queries)
	 * @return Connection
	 */
	public Connection getConnection()
	{
		Connection connection = null;
		try
	    {
	      if (datasourceException == false)
	      {
	         connection = ds.getConnection();
	      }
	    }
		catch(SQLException sqle)
	    {
			connectionException = true;
			String extra = "SQL Problem: " + sqle.getMessage() + "\n";
			extra += "SQL State: " + sqle.getSQLState() + "\n";
			extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
			if(Editor.isLoggingEnabled())
			{			  	      
			    logger.error(extra + Editor.getFullStackTrace(sqle));
			}
			else
		    {
		    	System.out.println(extra + Editor.getFullStackTrace(sqle));
		    }
	    }
	    catch(Exception e)
	    {
	      connectionException = true;
	      if(Editor.isLoggingEnabled())
	      {			
	        logger.error("Database.getConnection(): " + Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	  System.out.println(Editor.getFullStackTrace(e));
	      }
	    }
	    
	    return connection;
	}
	
	/**
	 * Use this method specifically for getting the binary data of a file contained in a database.
	 * The database field should be created of type TINYBLOB,BLOB,CLOB,NCLOB,MEDIUMBLOB,OR LONGBLOB. The type names
	 * may differ on other database systems, but as long as the field can hold large binary data, then you're good to go.
	 * The pKey and id values are used in a WHERE condition to obtain the specific record.
	 * @param pKey The name of the primary key field for the table in question
	 * @param id  The actual id value of the primary key field
	 * @param field  A Field object which should contain the names of the table and the field containing the binary file data
	 * @return An InputStream object
	 * @throws InsufficientDataException
	 */
	public InputStream executeFileSelect(String pKey, int id, Field field) throws InsufficientDataException
	{
		String tableName = field.getTableName();
		String fieldName = field.getDBFieldName();
		
		if(tableName.isEmpty())
		{
			throw new InsufficientDataException("You must provide a table name in the Field object.");
		}
		if(fieldName.isEmpty())
		{
			throw new InsufficientDataException("You must provide a field name in the Field object.");
		}
		if(pKey.isEmpty())
		{
			throw new InsufficientDataException("You must provide a primary key name.");
		}
		if(id < 1)
		{
			throw new InsufficientDataException("You must provide an id value greater than 0.");
		}
		
		Connection connection = null;
	    Statement statement = null;
	    ResultSet resultSet = null;
	    InputStream data = null;
	    String strquery = "SELECT " + field.toString() + " FROM " + field.getTableName() + " WHERE " + pKey + " = " + String.valueOf(id);
	    	    
	    try{
	      connection = getConnection();
	      if(connectionException == false)
	      {
	            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                resultSet = statement.executeQuery(strquery);
                 //move the cursor to the last record to obtain the record number
                resultSet.last();
                int recordCount = resultSet.getRow();
                //return the cursor to the first record
                resultSet.first();
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int type = -1;
                
                for(int i = 0; i < recordCount; i++)
                {
              	  for(int j = 0; j < 1; j++)
                  {            		
            		  type = rsmd.getColumnType(j + 1);
            		  switch (type)
            		  {
            		    case Types.BINARY:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;
            		    case Types.VARBINARY:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;            		    	
            		    case Types.LONGVARBINARY:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;
            		    case Types.BLOB:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;
            		    case Types.CLOB:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;
            		    case Types.NCLOB:
            		    	data = resultSet.getBinaryStream(j + 1);
            		    	break;
            		    default:
            		    	if(Editor.isLoggingEnabled())
                  	  	    {
                  			  logger.debug("Database.executeFileSelect - The database field indicated is not conducive to containing large binary data");
                  	  	    }            			  
            		  }            		  
                  }
            	  resultSet.next();
                }            
                
                resultSet.close();
                resultSet = null;
                statement.close();
                statement = null;  
        	    connection.close();
        	    connection = null;
             
	      }
	    }
	    catch(SQLException sqle)
	    {
	    	String extra = "SQL Problem: " + sqle.getMessage() + "\n";
	  		extra += "SQL State: " + sqle.getSQLState() + "\n";
	  		extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
	  		if(Editor.isLoggingEnabled())
	  		{	
	  			logger.error("Database.executeFileSelect - query: " + strquery);
	  		    logger.error(extra + Editor.getFullStackTrace(sqle));	  		    
	  		}
	  		else
	  	    {
	  		   System.out.println(extra + Editor.getFullStackTrace(sqle));	  	       
	  	    }
	    }
	    catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
		  {
	    	logger.error("Database.executeFileSelect - query: " + strquery);	        
	        logger.error(Editor.getFullStackTrace(e));	        
		  }
	      else
	  	  {
	    	 System.out.println(Editor.getFullStackTrace(e));	  	     
	  	  }
	    }
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
	      if (resultSet != null)
	      {
	        try { resultSet.close(); } catch (SQLException e) { ; }
	        resultSet = null;
	      }
	      if (statement != null)
	      {
	        try { statement.close(); } catch (SQLException e) { ; }
	        statement = null;
	      }	      
	      if (connection != null)
	      {
	        try { connection.close(); } catch (SQLException e) { ; }
	        connection = null;
	      }
	    }	    
	    
		return data;
	}
	
	/**
	 * All data returned by this method will be obtained as a String
	 * regardless of each field type. If a field type that holds large
	 * amounts of binary data such as a BLOB, CLOB, NCLOB, LONGVARBINARY, etc.
	 * the return value will always be &quot;***Binary Data Field***&quot; regardless of 
	 * whether the field is null or not.
	 * @return a 2D String[][] array
	 * @throws IllegalArgumentException
	 */
	public String[][] executeSelect()throws IllegalArgumentException
	{
		if(query.getQueryType() != Query.Type.SELECT)
		{
			throw new IllegalArgumentException("You are trying to execute a query of type " + query.getQueryType().toString() + " using method executeSelect.");
		}
		
		Connection connection = null;
	    PreparedStatement statement = null;
	    ResultSet resultSet = null;
	    String[][] data = null;
	    SimpleDateFormat sdf = null;	    
	    
	    try{
	      connection = getConnection();
	      if(connectionException == false)
	      {
	            connection.setAutoCommit(false);
            
            	//statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            	statement = query.getPreparedStatement(connection);
                resultSet = statement.executeQuery();
                Field[] flds = query.getFields();
                int columns = flds.length;
                //move the cursor to the last record to obtain the record number
                resultSet.last();
                int recordCount = resultSet.getRow();
                //Dimension the String[][] array
                data = new String[recordCount][columns];
                //return the cursor to the first record
                resultSet.first();
                String temp = "";
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int type = -1;
                java.sql.Date dte = null;
                java.sql.Timestamp tstamp = null;
                for(int i = 0; i < recordCount; i++)
                {
              	  for(int j = 0; j < columns; j++)
                  {            		
            		  type = rsmd.getColumnType(j + 1);
            		  //System.out.println("select() - Column Type: " + type);
            		  switch (type)
            		  {
            		    case Types.DATE:
            		    	sdf = new SimpleDateFormat("yyyy-MM-dd");//Adheres to DATE_ISO_8601	
                  		    dte = resultSet.getDate(j + 1);             		  
                  		    if(resultSet.wasNull())
                  		       data[i][j] = "";
                  		    else
                  		       data[i][j] = sdf.format(dte);
                  		    break;
            		    case Types.TIMESTAMP:
            		    	sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//Adheres to DATE_ISO_8601 + TIME	
                  		    tstamp = resultSet.getTimestamp(j + 1);             		  
                  		    if(resultSet.wasNull())
                  		       data[i][j] = "";
                  		    else
                  		       data[i][j] = sdf.format(tstamp);
                  		    break;
            		    case Types.BINARY:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    case Types.VARBINARY:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    case Types.LONGVARBINARY:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    case Types.BLOB:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    case Types.CLOB:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    case Types.NCLOB:
            		    	data[i][j] = Constants.BINARYDATA;
            		    	break;
            		    default:
            		    	temp = resultSet.getString(j + 1);
                  		    if(resultSet.wasNull())
                  		       data[i][j] = "";
                  		    else
                  		       data[i][j] = temp;
            		  }            		  
                  }
            	  resultSet.next();
                }
            
                //If reference to the class Editor is null then this Database class isn't being used
                //in conjunction with the Editor class.  Therefore, we don't need to calculate the number
                //of records returned from the query.
                if(reference != null && reference.usingSSP)
                {
                  //GET THE NUMBER OF RECORDS RETURNED BASED ON THE ORIGINAL QUERY.
                  boolean flag = false;
                  int foundrows = 0;
         
                  Statement stment = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                  String queryStr = "";
                  if(query.haveWhereConditions())
                  {
            	      queryStr = "SELECT COUNT(*) COUNT FROM " + query.getParentTable() + query.prepareWhereWithValues(query.getWhereConditions());            	      
                  }
                  else
                  {
            	      queryStr = "SELECT COUNT(*) COUNT FROM " + query.getParentTable();
                      flag = true;
                  }
     	   
                  resultSet = stment.executeQuery(queryStr);
                  resultSet.first();
                  foundrows = resultSet.getInt("COUNT");
                  query.setIFilteredTotal(foundrows);
              
     	   
     	          //GET THE TOTAL NUMBER OF RECORDS IN THE TABLE.
                  if(flag == false)
                  {
                	stment = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                	queryStr = "SELECT COUNT(*) COUNT FROM " + query.getParentTable();
     	            resultSet = stment.executeQuery(queryStr);
                    resultSet.first();
                    int ttl = resultSet.getInt("COUNT");
                    query.setITotal(ttl);
                  }
                  else
                  {
                    query.setITotal(foundrows);
                  }              
                }
                connection.commit();            
        	    resultSet.close();
                resultSet = null;
                statement.close();
                statement = null;  
        	    connection.close();
        	    connection = null;
             
	      }
	    }
	    catch(SQLException sqle)
	    {
	    	String extra = "SQL Problem: " + sqle.getMessage() + "\n";
	  		extra += "SQL State: " + sqle.getSQLState() + "\n";
	  		extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
	  		if(Editor.isLoggingEnabled())
	  		{	
	  			logger.error("Database.executeSelect - query: " + query.toString());
	  		    logger.error(extra + Editor.getFullStackTrace(sqle));
	  		    try
			    {
		          connection.rollback();
		        } catch(SQLException excep) {
		          logger.error(Editor.getFullStackTrace(excep));
		        }
	  		}
	  		else
	  	    {
	  		   System.out.println(extra + Editor.getFullStackTrace(sqle));
	  	       try
			    {
		          connection.rollback();
		        } catch(SQLException excep) {
		          System.out.println(Editor.getFullStackTrace(excep));
		        }
	  	    }
	    }
	    catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
		  {
	    	logger.error("Database.executeSelect - query: " + query.toString());	        
	        logger.error(Editor.getFullStackTrace(e));
	        try
		    {
	          connection.rollback();
	        } catch(SQLException excep) {
	          logger.error(Editor.getFullStackTrace(excep));
	        }
		  }
	      else
	  	  {
	    	 System.out.println(Editor.getFullStackTrace(e));
	  	     try
		     {
	           connection.rollback();
	         } catch(SQLException excep) {
	           System.out.println(Editor.getFullStackTrace(excep));
	         }
	  	  }
	    }
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
	      if (resultSet != null)
	      {
	        try { resultSet.close(); } catch (SQLException e) { ; }
	        resultSet = null;
	      }
	      if (statement != null)
	      {
	        try { statement.close(); } catch (SQLException e) { ; }
	        statement = null;
	      }	      
	      if (connection != null)
	      {
	        try { connection.close(); } catch (SQLException e) { ; }
	        connection = null;
	      }
	    }	    
	    if(data == null)
	    {
	    	data = new String[0][0];
	    }
		return data;
	}
	/**
	 * This is a convenience method when a select query string is being supplied
	 * directly instead of using the Query object. All data returned by this method will be obtained as a String
	 * regardless of each field type. If a field type that holds large
	 * amounts of binary data such as a BLOB, CLOB, NCLOB, LONGVARBINARY, etc.
	 * the return value will always be &quot;***Binary Data Field***&quot; regardless of whether the field is null or not.
	 * @param strQuery	 
	 * @return A 2D String[][] array
	 * @throws IllegalArgumentException
	 */
	public String[][] executeSelect(String strQuery)throws IllegalArgumentException
	{
		String copy = new String(strQuery);
		copy = copy.toLowerCase();
		//Perform simple test to ensure this is a proper SELECT query
		//The database itself will complain if there is something else wrong with the syntax.
		if(!copy.contains("select"))
		{
			throw new IllegalArgumentException("The query you provided is syntactically incorrect.");
		}
		else
		{
			if(!copy.contains("from"))
				throw new IllegalArgumentException("The query you provided is syntactically incorrect.");			
		}
		
		Connection connection = null;
	    Statement statement = null;
	    ResultSet resultSet = null;
	    String[][] data = null;
	    SimpleDateFormat sdf = null;
	    int columns = determineColumnCount(strQuery);
	    
	    try{
	      connection = getConnection();
          if(connectionException == false)
          {
        	statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        	resultSet = statement.executeQuery(strQuery);
            
            //move the cursor to the last record to obtain the record number
            resultSet.last();
            int recordCount = resultSet.getRow();
            //Dimension the String[][] array
            data = new String[recordCount][columns];
            //return the cursor to the first record
            resultSet.first();
            String temp = "";
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int type = -1;
            java.sql.Date dte = null;
            java.sql.Timestamp tstamp = null;
            for(int i = 0; i < recordCount; i++)
            {
            	for(int j = 0; j < columns; j++)
                {
            	  type = rsmd.getColumnType(j + 1);
            	  //System.out.println("select(String) - Column Type: " + type);
            	  switch (type)
          		  {
          		    case Types.DATE:
          		    	sdf = new SimpleDateFormat("yyyy-MM-dd");//Adheres to DATE_ISO_8601	
                		dte = resultSet.getDate(j + 1);             		  
                		if(resultSet.wasNull())
                		   data[i][j] = "";
                		else
                		   data[i][j] = sdf.format(dte);
                		break;
          		    case Types.TIMESTAMP:
          		    	sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//Adheres to DATE_ISO_8601 + TIME	
                		tstamp = resultSet.getTimestamp(j + 1);             		  
                		if(resultSet.wasNull())
                		   data[i][j] = "";
                		else
                		   data[i][j] = sdf.format(tstamp);
                		break;
          		    case Types.BINARY:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    case Types.VARBINARY:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    case Types.LONGVARBINARY:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    case Types.BLOB:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    case Types.CLOB:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    case Types.NCLOB:
          		    	data[i][j] = Constants.BINARYDATA;
          		    	break;
          		    default:
          		    	temp = resultSet.getString(j + 1);
                		if(resultSet.wasNull())
                		   data[i][j] = "";
                		else
                		   data[i][j] = temp;
          		  }        	  
                }
            	if(i < recordCount - 1)
           	       resultSet.next();
            }
            
                                    
        	resultSet.close();
            resultSet = null;
            statement.close();
            statement = null;  
        	connection.close(); 
        	connection = null;
          }
	    }
	    catch(SQLException sqle)
	    {
	    	String extra = "SQL Problem: " + sqle.getMessage() + "\n";
	  		extra += "SQL State: " + sqle.getSQLState() + "\n";
	  		extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
	  		if(Editor.isLoggingEnabled())
	  		{
	  			logger.error("Database.executeSelect - query: " + strQuery);
	  		    logger.error(extra + Editor.getFullStackTrace(sqle));
	  		}
	  		else
	  	    {
	  		   System.out.println("Database.executeSelect - query: " + strQuery); 
	  	       System.out.println(extra + Editor.getFullStackTrace(sqle));
	  	    }
	    }
	    catch(Exception e)
	    {
	      if(Editor.isLoggingEnabled())
		  {
	    	logger.error("Database.executeSelect - query: " + strQuery);
	        logger.error(Editor.getFullStackTrace(e));
		  }
	      else
	  	  {
	    	 System.out.println("Database.executeSelect - query: " + strQuery); 
	  	     System.out.println(Editor.getFullStackTrace(e));
	  	  }
	    }
	    finally
	    {
	      // Always make sure result sets and statements are closed,
	      // and the connection is returned to the pool
	      if (resultSet != null)
	      {
	        try { resultSet.close(); } catch (SQLException e) { ; }
	        resultSet = null;
	      }
	      if (statement != null)
	      {
	        try { statement.close(); } catch (SQLException e) { ; }
	        statement = null;
	      }	      
	      if (connection != null)
	      {
	        try { connection.close(); } catch (SQLException e) { ; }
	        connection = null;
	      }
	    }	    
	    if(data == null)
	    {
	    	data = new String[0][0];
	    }
		return data;
	}
	
	//Helper method 
	private int determineColumnCount(String query)
	{
		String copy = new String(query);
		copy = copy.toLowerCase();
		String slc = "select";
		int index = copy.indexOf("from");
		String sub = copy.substring(slc.length(),index);
		StringTokenizer tok = new StringTokenizer(sub,",");
		return tok.countTokens();
	}
	
		
	/**
	 * Execute an Insert or Update on a Query supplied to this class.
	 * @return A boolean informing success or not.
	 */
	public boolean executeInsertUpdate()throws IllegalArgumentException, SQLException
	{
		if(query.getQueryType() != Query.Type.INSERT && query.getQueryType() != Query.Type.UPDATE)
		{
			throw new IllegalArgumentException("You are trying to execute a query of type " + query.type.toString() + " using method executeInsertUpdate.");
		}
		
		Connection connection = null;
	    PreparedStatement statement = null;
	    ResultSet resultSet = null;
	    boolean success = true;
	    	    
	    try{
	      connection = getConnection();
	      
          if(connectionException == false)
          {
        	connection.setAutoCommit(false);
        	statement = query.getPreparedStatement(connection);        	
            statement.executeUpdate();
            if(query.getQueryType() == Query.Type.INSERT)
            {
            	//Get the id assigned to the new record
            	//If we are dealing with a Link Table then we don't need to retrieve any generated ids.
            	if(query.isLinkTable() == false)
            	{
            	  resultSet = statement.getGeneratedKeys();            	  
            	  if(resultSet != null)
                  {
            		if(resultSet.next())
                	{
                	  query.setNewRecordID(resultSet.getInt(1));
                      //System.out.println(query.getNewRecordID());               		  
                	}
                	else
                	{
                	  if(Editor.isLoggingEnabled())	    	  
              		  {                		 
                		throw new SQLException("Database.executeInsertUpdate - on INSERT - Unable to retrieve id value");
              		  }
                	}
                 }
                 resultSet.close();
                 resultSet = null;                 
               }
            }
        	connection.commit();
            statement.close();
            statement = null;               
          }
	    }
	    catch(SQLException sqle)
	    {
	      success = false;
	      if(Editor.isLoggingEnabled())
		  {
	    	try
		    {
	          connection.rollback();
	        } catch(SQLException excep) {
	          logger.error(Editor.getFullStackTrace(excep));
	        }  
	    	String extra = "Database.executeInsertUpdate - query: " + query.toString() + "\n";  
		    extra += "SQL Problem: " + sqle.getMessage() + "\n";
			extra += "SQL State: " + sqle.getSQLState() + "\n";
			extra += "Vendor Error: " + sqle.getErrorCode() + "\n";
	    	if(sqle.getSQLState().indexOf("S1000") == -1)
	    	{	    	  	      
			  logger.error(extra + Editor.getFullStackTrace(sqle));
	    	}
	    	else
	    	{
	    	  System.out.println(extra + Editor.getFullStackTrace(sqle));
	    	}
		  }
	    }
	    catch(Exception e)
	    {
	      success = false;
	      if(Editor.isLoggingEnabled())	    	  
		  {
	    	try
			{
		      connection.rollback();
		    } catch(SQLException excep) {
		      logger.error(Editor.getFullStackTrace(excep));
		    }
	    	logger.error("Database.executeInsertUpdate - query: " + query.toString());
	    	logger.error(Editor.getFullStackTrace(e));
		  }
	      else
	      {
	    	System.out.println("Database.executeInsertUpdate - query: " + query.toString());
	    	System.out.println(Editor.getFullStackTrace(e));
	    	try
		    {
	          connection.rollback();
	        } catch(SQLException excep) {
	        	System.out.println(Editor.getFullStackTrace(excep));
	        }
	      }
	    }
	    finally
	    {
	      // Always make sure statements are closed,
	      // and the connection is returned to the pool
	      if (resultSet != null)
		  {
		     try { resultSet.close(); } catch (SQLException e) { ; }
		     resultSet = null;
		  }
	      if (statement != null)
	      {
	        try { statement.close(); } catch (SQLException e) { ; }
	        statement = null;
	      }
	      if (connection != null)
	      {
	        try { connection.close(); } catch (SQLException e) { ; }
	        connection = null;
	      }
	    }
	    return success;
	}	
	/**
	 * Execute a delete based on an array of Query objects.
	 * @param queries
	 */
	public void executeDeletes(Query[] queries)throws IllegalArgumentException
	{
		
		for(int i =0; i < queries.length; i++)
		{
			if(queries[i].getQueryType() != Query.Type.DELETE )
			{
				throw new IllegalArgumentException("You are trying to execute a query of type " + queries[i].getQueryType().toString() + " using method executeDeletes.");
			}
		}
		
		Connection connection = null;
	    PreparedStatement statement = null;
	    int i = 0;
	    	    	    
	    try{
	      connection = getConnection();	      
          if(connectionException == false)
          {
        	connection.setAutoCommit(false);
        	for(; i < queries.length; i++)
        	{          	  
        	  //statement = connection.prepareStatement(queries[i].toString());
        	  statement = queries[i].getPreparedStatement(connection);	
              statement.executeUpdate();
        	}
        	connection.commit();
            statement.close();
            statement = null;              
          }
	    }
	    catch(SQLException sqle)
	    {
	      String extra = "Database.executeDeletes - query: " + queries[i].toString() + "\n";  
		  extra += "SQL Problem: " + sqle.getMessage() + "\n";
		  extra += "SQL State: " + sqle.getSQLState() + "\n";
		  extra += "Vendor Error: " + sqle.getErrorCode() + "\n";	
	      if(Editor.isLoggingEnabled())
		  {	 
	    	try
			{
			   connection.rollback();
			} catch(SQLException excep) {
			  logger.error(Editor.getFullStackTrace(excep));
			}
			logger.error("Database.executeDeletes: query: " + queries[i].toString()); 
			logger.error(extra + Editor.getFullStackTrace(sqle));
		  }
	      else
	      {
	    	  try
			  {
			      connection.rollback();
			  } catch(SQLException excep) {
				  System.out.println(Editor.getFullStackTrace(excep));
			  }
			  System.out.println("Database.executeDeletes: query: " + queries[i].toString());
	    	  System.out.println(extra + Editor.getFullStackTrace(sqle));
	      }
	    }
	    catch(Exception e)
	    {	    	
	    	if(Editor.isLoggingEnabled())
			{
		      logger.error("Database.executeDeletes: query: " + queries[i].toString());  
		      logger.error(Editor.getFullStackTrace(e));
		      try
			  {
			      connection.rollback();
			  } catch(SQLException excep) {
			      logger.error(Editor.getFullStackTrace(excep));
			  }
			}
	    	else
		    {
	    	  try
			  {
				      connection.rollback();
			  } catch(SQLException excep) {
				  System.out.println(Editor.getFullStackTrace(excep));
			  }	
	    	  System.out.println("Database.executeDeletes: query: " + queries[i].toString());
		      System.out.println(Editor.getFullStackTrace(e));
		    }
	    }
	    finally
	    {
	      // Always make sure statements are closed,
	      // and the connection is returned to the pool
	      if (statement != null)
	      {
	        try { statement.close(); } catch (SQLException e) { ; }
	        statement = null;
	      }
	      if (connection != null)
	      {
	        try { connection.close(); } catch (SQLException e) { ; }
	        connection = null;
	      }
	    }	    
	}	
	
}



