package com.tacticalenterprisesltd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import org.apache.commons.fileupload.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.apache.log4j.Logger;

/**
 * <p>Use this class to upload files.  For database cleanup you can allow Upload to perform
 * its own routine, or you can provide your own routine by setting method setDbCleanInstance(DbClean dbc).
 * See interface DbClean for more details.</p><p>There are two ways you can save the file you upload:<br>
 * <ul>
 * <li>save to a directory on the server</li>
 * <li>save directly to a field in a table in the database</li>
 * </ul></p>
 * <p>The pros and cons of either method are hotly debated on java forums, but whichever one
 * you choose, this class can handle it.  If you want to save your files to a directory
 * on the server, use the constructor that has the parameter <i>systemPath</i>.  This property
 * must be set so that JED knows where to save the files.  A sub-directory is created named
 * <i>upload</i> which is appended to the systemPath. i.e.: C:\\my\\system\\path\\tomcat\\webapps\\editor[\\upload]</p>
 * <p>If you want to save files directly to a database table field, the field should be of type
 * TINYBLOB,BLOB,MEDIUMBLOB, OR LONGBLOB.  The types may be named something else in other database systems,
 * but this is what they are named in MySQL.  As long as they hold large amounts of binary data, you're good to go.
 * Use the constructors that don't require a systemPath.  In doing so, Upload will assume you're trying to
 * save to the database itself.</p>
 * <p>When creating the table that contains the files you upload, it should consist of any combination of the following
 * field names:
 * <ul>
 * <li>content - the binary data of the file</li>
 * <li>contenttype - this would be the same as mimetype</li>
 * <li>mimetype - the mimetype of the file (same as contenttype)</li>
 * <li>extension - the files extension. i.e.: .txt, .doc, .xlsx, .pdf</li>
 * <li>filename - the name of the file (with the extension)</li>
 * <li>filesize - the size of the file in bytes</li>
 * <li>modified - a DATETIME field consisting of a timestamp. This field is automatically set by this class if used.</li>
 * <li>web_path - the relative path to the file as it sits on the server</li>
 * <li>system_path - the absolute path to your file as it sits on the server</li>
 * </ul></p>
 * <p>
 * CONFIGURATION EXAMPLE:<br>
 * <code>
 * ...<br>
 * Field field2 = new Field("mytable","somefield",Field.Type.STRING);<br>
 * //By passing field2 as a parameter to the constructor of Upload, field2 then also contains<br>
 * //a reference to the Upload instance and vise versa.<br>
 * Upload upload = new Upload(fileItem, getServletContext().getInitParameter("ContextPath"), field2);<br>
 *  <br>
 *  //We will be uploading only image files.  Set the file extensions permissible.<br>
 *  //The empty string indicates to Upload to use a default error message if a file is uploaded with the wrong extension.<br>
 *  upload.setImageExtensions("");<br>
 *  //Call the convenience method to tell Upload to check each files mime type. This is important to perform<br>
 *  //a deeper scan of the contents of the file to ensure the file is what its extension seems to indicate.<br>
 *  upload.setImageMimeTypes();<br>
 *  //Set the maximum file size for each file.<br>
 *  upload.setMaxFileSize(1024 * 50);  //50Kb<br>
 *  //Configure Upload by informing it of the database field names in the database<br>
 *  //on each file.<br>
 *  LinkedHashMap<String, Object> fds = new LinkedHashMap<String, Object>();<br>
 *  fds.put("filename",Upload.DbType.FileName);<br>
 *  fds.put("filesize",Upload.DbType.FileSize);<br>
 *  fds.put("web_path",Upload.DbType.WebPath);<br>
 *  fds.put("system_path",Upload.DbType.SystemPath);<br>
 *  upload.DbConfiguration("mytable", "id", fds);<br> 
 *  //Add field2 to editor<br>
 *  editor.addField(field2);<br>
 *  ...
 * </code>
 * </p>
 * @author Alan Shiers
 * @version 1.5.0
 */
public class Upload
{
	public static enum DbType {Content,ContentType,Extn,FileName,FileSize,MimeType,Modified,WebPath,SystemPath }
	private LinkedHashMap<String,String> mimetypes = null;
	private String sysPath = "";
	private Field fld = null;
	private String[] extns;
    private String extnError;
    private String dbTable = "";
    private String dbPKey = "id"; //default
    private LinkedHashMap<String, Object> dbFields;
    private String[] arrDbFields = null;
    private Logger logger;
    private int maxFileSize = -1;
    private FileItem fileItem = null;
    private final String STORAGEDIR = "upload";
    private DbClean dbclean = null;
    private int rowID = -1;
    private Editor editor = null;
            
    /**
     * Upload constructor - provide an instance of a Field
     * @param e An instance of Editor
     * @param field An instance of Field
     */
    public Upload(Editor e, Field field)
    {
    	editor = e;
    	fld = field;
    	fld.setUpload(this);    	
    	if(Editor.isLoggingEnabled())
  	    {
  		  logger = Logger.getLogger(Upload.class);
  	    }
    }
    
    /**
     * Upload constructor - provide an instance of FileItem and an instance of a Field
     * @param e
     * @param item A FileItem instance
     * @param field An instance of Field
     */
    public Upload(Editor e, FileItem item, Field field)
    {
    	editor = e;
    	fileItem = item;
    	fld = field;
    	fld.setUpload(this);
    	if(Editor.isLoggingEnabled())
  	    {
  		  logger = Logger.getLogger(Upload.class);
  	    }
    }
    /**
     * Upload constructor - provide an absolute path to the web-app root directory and an instance of a Field.
     * A directory will be appended to the web-app root named "upload".  This directory will be where the actual files will be stored.
     * If you're NOT going to store the files on the server but directly into the database, use one of the other constructors that don't require
     * a systemPath as a parameter.
     * @param e An instance of Editor
     * @param systemPath A String path
     * @param field An instance of Field
     */
    public Upload(Editor e, String systemPath, Field field)
    {
    	editor = e;
    	String platform = getPlatform();
    	char lastchar;
    	if(platform.equals("Windows"))
    	{
    	   lastchar = systemPath.charAt(systemPath.length() - 1);
    	   if(lastchar == File.separatorChar)
    		   sysPath = systemPath + STORAGEDIR;
    	   else
    	       sysPath = systemPath + File.separator + File.separator + STORAGEDIR;
    	}
    	else
    	{
    	   lastchar = systemPath.charAt(systemPath.length() - 1);
     	   if(lastchar == File.separatorChar)
     		   sysPath = systemPath + STORAGEDIR;
     	   else
     	       sysPath = systemPath + File.separator + STORAGEDIR;
    	}
    	checkDirExists(sysPath);
    	fld = field;
    	fld.setUpload(this);
    	if(Editor.isLoggingEnabled())
  	    {
  		  logger = Logger.getLogger(Upload.class);
  	    }
    }
    
    /**
     * Upload constructor - provide an instance of FileItem and the absolute path to the web-app root directory, and an instance of a Field.
     * A directory will be appended to the web-app root named "upload".  This directory will be where the actual files will be stored.
     * If you're NOT going to store the files on the server but directly into the database, use one of the other constructors that don't require
     * a systemPath as a parameter.
     * @param e An instance of Editor
     * @param item A FileItem instance
     * @param systemPath A String path
     * @param field An instance of Field
     */
    public Upload(Editor e, FileItem item, String systemPath, Field field)
    {
    	editor = e;
    	fileItem = item;
    	String platform = getPlatform();
    	char lastchar;
    	if(platform.equals("Windows"))
    	{
    	   lastchar = systemPath.charAt(systemPath.length() - 1);
    	   if(lastchar == File.separatorChar)
    		   sysPath = systemPath + STORAGEDIR;
    	   else
    	       sysPath = systemPath + File.separator + File.separator + STORAGEDIR;
    	}
    	else
    	{
    	   lastchar = systemPath.charAt(systemPath.length() - 1);
     	   if(lastchar == File.separatorChar)
     		   sysPath = systemPath + STORAGEDIR;
     	   else
     	       sysPath = systemPath + File.separator + STORAGEDIR;
    	}
    	checkDirExists(sysPath);
    	fld = field;
    	fld.setUpload(this);
    	if(Editor.isLoggingEnabled())
  	    {
  		  logger = Logger.getLogger(Upload.class);
  	    }
    }
    
    /**
     * Set the name of the tables primary key field.
     * If not set, the default is &quot;id&quot;
     * @param pkey
     */
    public void setPKeyFieldName(String pkey)
    {
    	dbPKey = pkey;
    }
    /**
     * Get the name of the tables primary key field
     * @return
     */
    public String getPKeyFieldName()
    {
    	return dbPKey;
    }
    
    /**
     * Set the row id value of a record that needs updating
     * @param id
     */
    public void setRowID(int id)
    {
    	rowID = id;
    }
    /**
     * Get the row id value of the record that needs updating
     * @return
     */
    public int getRowID()
    {
    	return rowID;
    }
    
    /**
     * This method is very important in that you can set your own class that implements
     * interface DbClean.  The interface has one method clean(int[] ids).  Your concrete 
     * class will return a boolean on the method signifying a completed action on the database
     * cleanup and any other actions you deem necessary.  An array of ids will be provided
     * by Uploads internal method of for database cleanup.  The ids are those of records
     * in your database table that are no longer being referenced and should be deleted.
     * @param dbc
     */
    public void setDbCleanInstance(DbClean dbc)
    {
    	dbclean = dbc;
    }
    
    //Helper method to ensure the directory indicated actually exists.
    //If it doesn't, it will be created.
    private void checkDirExists(String syspath)
    {
    	File dir = new File(syspath);
    	if(dir.exists() == false)
    	{
    	  try{	
    		dir.mkdir();    		
    	  }
    	  catch(SecurityException se)
    	  {
    		  if(Editor.isLoggingEnabled())
    	  	  {
    			  logger.error(Editor.getFullStackTrace(se));
    	  	  }  
    	  }
    	}    	
    }
    
    /**
     * Convenience setter for mime types of type image.
     * These include: "image/jpeg", "image/png", "image/gif"
     */
    public void setImageMimeTypes()
    {
    	mimetypes = new LinkedHashMap<String,String>();
    	mimetypes.put("jpg","image/jpeg");
    	mimetypes.put("png","image/png");
    	mimetypes.put("gif","image/gif");
    	mimetypes.put("tif","image/tiff");
    }
    
    /**
     * Set mime types as they correspond with known file name extensions.
     * 
     * Example:
     * <blockquote> 
     * <pre><br>
     * LinkedHashMap<String,String> mimetypes = new LinkedHashMap<String,String>();<br>
     * mimetypes.put("tar","application/x-tar");<br>
     * mimetypes.put("zip","application/zip");<br>
     * mimetypes.put("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");<br>
     * mimetypes.put("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");<br>
     * </pre>
     * </blockquote>
     * @param types
     * @see http://www.freeformatter.com/mime-types-list.html for list of all possible mimetypes
     */
    public void setAllowableMimeTypes(LinkedHashMap<String,String> types)
    {
    	mimetypes = types;
    }
    /**
     * Set the maximum file size
     * @param size
     */
    public void setMaxFileSize(int size)
    {
    	maxFileSize = size;
    }
    /**
     * Get the maximum file size
     * @return
     */
    public int getMaxFileSize()
    {
    	return maxFileSize;
    }
    /**
     * Set the system path that leads to the root of the web-app. A default directory named "upload"
     * will be appended to the path.
     * @param path
     */
    public void setSystemPath(String path)
    {
    	String platform = getPlatform();
    	char lastchar;
    	if(platform.equals("Windows"))
    	{
    	   lastchar = path.charAt(path.length() - 1);
    	   if(lastchar == File.separatorChar)
    		   sysPath = path + STORAGEDIR;
    	   else
    	       sysPath = path + File.separator + File.separator + STORAGEDIR;
    	}
    	else
    	{
    	   lastchar = path.charAt(path.length() - 1);
     	   if(lastchar == File.separatorChar)
     		   sysPath = path + STORAGEDIR;
     	   else
     	       sysPath = path + File.separator + STORAGEDIR;
    	}
    	checkDirExists(sysPath);
    }
    /**
     * Get the system path that leads to the storage directory for the uploaded files. 
     * @return
     */
    public String getSystemPath()
    {
    	return sysPath;
    }
    /**
     * Determine if we have a System Path. Knowing this will signify whether or not
     * we are saving files to the server or directly to the database.
     * @return a boolean value
     */
    public boolean haveSystemPath()
    {
    	if(sysPath.isEmpty())
    		return false;
    	return true;
    }
    /**
     * Set the FileItem which has been tested to contain the contents of the file uploaded.
     * The test process is done usually in a java server page or servlet which queries
     * for MultiPartContent then separates form field content from file content.     
     * @param item
     */
    public void setFileItem(FileItem item)
    {
    	fileItem = item;
    }
    /**
     * Get the FileItem that contains the actual content of the file uploaded.
     * @return a FileItem
     */
    public FileItem getFileItem()
    {
    	return fileItem;
    }
    /**
     * Set the field this upload instance is associated with.
     * @param field
     */
    public void setField(Field field)
    {
    	fld = field;
    	fld.setUpload(this);
    }
    /**
     * Get the field this upload instance is associated with.
     * @return
     */
    public Field getField()
    {
    	return fld;  
    }
    
        
    private void SaveFileAs(FileItem fi, File file)
    {
    	try{
    	  fi.write(file);
    	}
    	catch(Exception e)
    	{
    		if(Editor.isLoggingEnabled())
    		{    		  
    	      logger.error(Editor.getFullStackTrace(e));
    		}
    	}
    }
    
        
    /**
     * Provide a list of valid file extensions. This is for simple
     * validation that the file is of the expected type. Extensions must be provided
     * to ensure minimally validation is performed on the extension of the file name.  To ensure deeper
     * validation on the content of each file uploaded, be sure to call methods
     * setImageMimeTypes() or setAllowableMimeTypes(). Provide an error message in the event
     * that the file uploaded doesn't pass the validation phase. If you pass an empty
     * String, a default message will be set.
     * @param extensions
     * @param errorMessage
     * 
     */
    public void setAllowableExtensions(String[] extensions, String errorMessage)
    {
        extns = extensions;
        if(errorMessage.equals(""))
        {
        	//Default error message
        	extnError = "This file type cannot be uploaded";
        }
        else
            extnError = errorMessage;        
    }
    
    /**
     * A convenience method to set allowable file extensions on image type files.
     * These would include: jpg,gif,png, and tif
     * Provide an error message in the event
     * that the file uploaded doesn't pass the validation phase.  If you pass an empty
     * String, a default message will be set.
     */
    public void setImageExtensions(String errorMessage)
    {
    	String[] extensions = new String[4];
    	extensions[0] = "jpg";
    	extensions[1] = "gif";
    	extensions[2] = "png";
    	extensions[3] = "tif";
    	extns = extensions;
    	if(errorMessage.equals(""))
        {
        	//Default error message
        	extnError = "This file type cannot be uploaded";
        }
        else
            extnError = errorMessage;
    }
    
    /**
     * Database configuration method. When used, this method will tell Editor
     * what information you want to be written to a database on file upload, should
     * you wish to store relational information about your files on the database
     * (this is generally recommended).
     * @param table - Name of the table where the file information should be stored
     * @param pkey - Primary key column name. This is required so each row can be uniquely identified.
     * @param fields - A list of the fields to be written to on upload. The
     * LinkedHashMap keys are used as the database column names and the values can be
     * defined by the 'DbType' enum of this class. The value can also be a string,
     * which will be written directly to the database, or a function which will be
     * executed and the returned value written to the database.
     * 
     */
    public void DbConfiguration(String table, String pkey, LinkedHashMap<String, Object> fields)
    {
        dbTable = table;
        dbPKey = pkey;
        dbFields = fields;
        arrDbFields = new String[dbFields.size()];
        int i = 0;
        for ( String key : dbFields.keySet())
        {
            arrDbFields[i] = key;
            i++;
        }
    }   
    
    
    /**
     * Check file uploads. Each FileItem object passed will be tested. If mime types are provided
     * by calling methods setImageMimeTypes() or setAllowableMimeTypes(), extended validation will
     * be performed on each FileItem object to ensure it is what it's file name extension seems to imply.
     * @param item
     * @return ValidationMessage
     */
    private ValidationMessage isFileValid(FileItem item) throws InsufficientDataException
    {
        if(maxFileSize == -1)
        	throw new InsufficientDataException("You need to set the MaxFileSize property.");
        if(extns == null)
        	throw new InsufficientDataException("You need to set the file Extensions property.");
        ValidationMessage vm = null;
        if(item.getSize() > maxFileSize)
        {        	
        	vm = new ValidationMessage(false, "The file " + item.getName() + " exceeds the maximum file size: " + maxFileSize + " bytes.");
        	return vm;  	
        }
        //Perform validation on just the file name extension
        boolean ok = false;
        String extension = FilenameUtils.getExtension(item.getName());
        for(String str : extns)
        {
        	if(extension.equalsIgnoreCase(str))
        	{
        		ok = true;   
        		break;
        	}
        }
        if(ok == false)
        {
        	vm = new ValidationMessage(false, extnError);
        	return vm;
        }
        
        if(mimetypes != null)
        {
        	//Perform validation on the contents of the FileItem
        	String mimetype = getFileMimeType(item);
        	String mtype = mimetypes.get(extension);
        	if(!mimetype.equals(mtype))
        	{
        		vm = new ValidationMessage(false, "File: " + item.getName() + " - is NOT the type it claims to be.");
            	return vm;
        	}
        }
        
        vm = new ValidationMessage(true);
        
        return vm;
    }
    
    /**
     * Use this method to validate the contents of a FileItem to ensure it is what it's name implies.
     * @param item
     * @return The FileItem's true mime type.
     */
    private String getFileMimeType(FileItem item)
    {
    	String type = "";
    	InputStream is = null;
    	try
    	{ 
    	  is = item.getInputStream();
		  ContentHandler contenthandler = new BodyContentHandler();
	      Metadata metadata = new Metadata();
	      metadata.set(Metadata.RESOURCE_NAME_KEY, item.getName());
	      Parser parser = new AutoDetectParser();
	      parser.parse(is, contenthandler, metadata, new ParseContext());
	      type = metadata.get(Metadata.CONTENT_TYPE);	      	      
    	}
    	catch(Exception e)
    	{
    		if(Editor.isLoggingEnabled())
      		{    		  
      	      logger.error(Editor.getFullStackTrace(e));
      		}
    	}
    	finally
    	{
    		try{
    		is.close();
    		}
    		catch(IOException ioe)
    		{
    			if(Editor.isLoggingEnabled())
          		{    		  
          	      logger.error(Editor.getFullStackTrace(ioe));
          		}
    		}
    	}
    	return type;
    }
    
    /**
     * Get database information from the table on all files currently sitting on the server.
     * @param db - Database instance
     * @return LinkedHashMap<String, LinkedHashMap<String, Object>> Database information
     */
    
    protected LinkedHashMap<String, LinkedHashMap<String, Object>> getDBData(Database db) throws InsufficientDataException
    {
    	if(dbTable.equals("") && dbPKey.equals(""))
        {
      	  throw new InsufficientDataException("You need to call method DbConfiguration first to set the name of the table and its field names.");    	  
        }
    	
        if(db == null)
        {
        	throw new InsufficientDataException("An instance of Database has not been created.");
        }
        String fields = dbPKey + ",";
        for ( String key : arrDbFields ) {
        	//exclude any content types
        	DbType type = (DbType)dbFields.get(key);
        	if(type != DbType.Content)
               fields = fields + key + ",";
        }
        fields = fields.substring(0,fields.length() - 1);

        // Select the configured db columns
        String query = "SELECT " + fields + " FROM " + dbTable;
                
        String[][] result = db.executeSelect(query);

        LinkedHashMap<String, LinkedHashMap<String, Object>> outData = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
        
        if(result.length == 0)
        	return outData;
        
        LinkedHashMap<String, Object> row;

        String id = "";
        for(int i = 0; i < result.length; i++)
        {
        	row = new LinkedHashMap<String,Object>();
        	for(int j = 0; j < result[0].length; j++)
        	{
        		if(j == 0)
        		{
        		   id = result[i][j];
        		   row.put(dbPKey, result[i][j]);
        		}
        		else
        		   row.put(arrDbFields[j-1], result[i][j]);
        	}
        	outData.put(id, row);
        }
        
        return outData;
    }
    
    
    /**
     * Execute a file clean up
     * @param editor - Calling Editor instance
     * @param field - Host field
     */
    protected void DbCleanExec(Database db)
    {
        DbClean(db);
    }


    /**
     * Process the uploading of files
     * 
     * 
     */
    protected void Execute(Database db)
    {
      try
      {
    	if(fileItem == null)
    	{
    		FieldErrorsOutput err = new FieldErrorsOutput();
    		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
    		if(fld != null)
			{
				map.put("name", fld.getName());
			}
			else
			{
			  map.put("name", "image"); //Default
			}
			map.put("status", "No file uploaded");
			err.fieldErrors.add(map);
			editor.output = err;
			return;
    	}    	
        
        //ArrayList<Integer> ids = new ArrayList<Integer>();
        
    	ValidationMessage vm = isFileValid(fileItem);
    	if(vm.isValid() == false)
    	{
    	  //Set the fieldsError for the output
		  FieldErrorsOutput err = new FieldErrorsOutput();
		  LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		  if(fld != null)
		  {
			map.put("name", fld.getName());
		  }
		  else
		  {
			map.put("name", "image"); //Default
		  }
		  map.put("status", vm.getMessage());
		  err.fieldErrors.add(map);
		  editor.output = err;
		  return;
    	}
    	//Process the FileItem.  Save to directory if indicated and
    	//create a new or edit an existing record in the images table of the database.    	  
    	if(sysPath.equals("") == false)
    	{
          //The files are stored on the server
          //1) First we want to create a record in the database and get
          //the resultant id value to use in renaming the file.
          //2) Save the file to the server.
          //3) Generate the appropriate output
          int id = dbExec(db, fileItem);
          //ids.add(new Integer(id));
          String extension = FilenameUtils.getExtension(fileItem.getName());
          File file = new File(sysPath + File.separator + String.valueOf(id) + "." + extension);
          SaveFileAs(fileItem, file);
          PrepareOutput(db, id);          
        }
    	else
    	{
    		//Save the file directly to the database
    		int id = dbExec(db,fileItem);
    		//ids.add(new Integer(id));
    		PrepareOutput(db,id);
    	}
      }
      catch(InsufficientDataException ide)
      {
    	if(Editor.isLoggingEnabled())
  		{    		  
  	      logger.error(Editor.getFullStackTrace(ide));
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
    
    private void PrepareOutput(Database db, int identifier)
    {
    	UploadOutput out = new UploadOutput();
    	LinkedHashMap<String,Object> fielddata = null;
    	LinkedHashMap<String,LinkedHashMap<String,Object>> rows = new LinkedHashMap<String,LinkedHashMap<String,Object>>(); ;
    	String fields = dbPKey + ",";
        for ( String key : arrDbFields ) {
        	//exclude any content types
        	DbType type = (DbType)dbFields.get(key);
        	if(type != DbType.Content)
               fields = fields + key + ",";
        }
        fields = fields.substring(0,fields.length() - 1);
        
        //Get all the records.
    	String query = "SELECT " + fields + " FROM " + dbTable;
    	
    	String[][] result = db.executeSelect(query);
    	
    	String id = "";
    	if(result != null && result.length > 0)
    	{    	  
    	  for(int i = 0; i < result.length; i++)
    	  {
    		fielddata = new LinkedHashMap<String,Object>();    		  	  
    		for(int j = 0; j < result[0].length; j++)
    	    {
    			if(j == 0)
    	    	{
    	    	  id = result[i][j];
    	    	  fielddata.put(dbPKey,result[i][j]);	
    	    	}
    	    	else
    	    	{    	    	  
    		      fielddata.put(arrDbFields[j - 1], result[i][j]);
    	    	}
    	    }
    		rows.put(id, fielddata);
    	  }
    	  out.files.put(dbTable, rows);
    	  //Indicate the particular item that was uploaded.
    	  out.upload.id = String.valueOf(identifier);
          editor.output = out;	
    	}
    }
    
    
    /**
     * Get the table name for the files table
     * @return Table name
     */
    protected String Table()
    {
        return dbTable;
    }
    
       
    /**
     * This method is used to remove files in the upload directory which no longer have
     * a reference in the database table.
     * @param ids String[] array of ids 
     */
    private void FilesClean(int[] ids) throws InsufficientDataException
    {
      if(dbTable.equals("") && dbPKey.equals(""))
      {
    	  throw new InsufficientDataException("You need to call method DbConfiguration first to set the name of the table and its field names.");    	  
      }
      
      if(sysPath.equals("") == false)
      {    	        
    	//The files are stored on the server
    	try
    	{
          File storageArea = new File(sysPath);
          File[] files = storageArea.listFiles();
          String name = "";
          //Iterate over all the files and determine if the id/name exists in the database table.
          //If not, then remove the file from the directory.
          for(File file : files)
          { 
        	name = FilenameUtils.removeExtension(file.getName());
        	for(int i = 0; i < ids.length; i++)
        	{
        		if(name.equals(String.valueOf(ids[i])))
        		{
        			if(file.isFile())
        			{
        				file.delete();
        			}
        		}
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
      
    }
    
    private void DbClean(Database db)
    {
    	
        if (dbTable.equals(""))
            return;        

        if(fld == null)
        	return;
        
        if(db == null)
        	return;
        
        try{
        	
          String tablename = fld.getTableName();
          String fieldname = fld.getDBFieldName();
        

          // Get the information from the database about the orphaned children
          String query = "SELECT " + dbPKey + " FROM " + dbTable +
                         " WHERE " + dbPKey + " NOT IN (SELECT " + fieldname + " FROM " + tablename + " WHERE " + fieldname + " IS NOT NULL)";
          String[][] result = db.executeSelect(query);
          
          if(result.length == 0)
        	  return;
        
          //Remove the actual files from the storage directory
          int[] ids = new int[result.length];
          for(int i = 0; i < result.length; i++)
          {
        	ids[i] = Integer.parseInt(result[i][0]);        	
          }
          
          if(dbclean != null)
          {
        	  boolean ok = dbclean.clean(ids);
        	  if(ok)
        	  {
        		  return;
        	  }
          }
          
        
          FilesClean(ids);
          
          //Now remove the records from the table containing info on the files
          Query[] qs = new Query[1];
          Query q = new Query(Query.Type.DELETE, dbTable);
          qs[0] = q;
          Field field = new Field(dbTable, dbPKey, Field.Type.INT);
          WhereCondition[] conditions = new WhereCondition[ids.length];
          WhereCondition wc = null;
          for(int i = 0; i < ids.length; i++)
          {
        	  wc = new WhereCondition(field, ids[i], "=");
        	  conditions[i] = wc;
          }
          q.setWhereConditions(conditions);
          q.setFilterLogicalOperator(Query.FilterLogicalOperator.OR);
          db.setQuery(q);
          db.executeDeletes(qs);
        
        }
        catch(Exception e)
        {
        	if(Editor.isLoggingEnabled())
    		{    		  
    	      logger.error(Editor.getFullStackTrace(e));
    		}
        }     
    }
    
    /**
     * Add a record to the database for a newly uploaded file
     * @param editor Host editor
     * @param upload Uploaded file
     * @return Primary key value for the newly uploaded file
     */
    private int dbExec(Database db, FileItem item)throws Exception
    {    	
    	if(db == null)
    		throw new Exception("An instance of Database has not been set in class Editor properly.");
    	//Divert for an update on existing record if necessary
    	if(rowID > -1)
    	{
    		update(db,item);
    		return rowID;
    	}
    	
    	int id = -1;
    	Query query = new Query(Query.Type.INSERT, dbTable);
    	LinkedHashMap<String,DbType> pathFields = new LinkedHashMap<String, DbType>();
    	ArrayList<Field> fields = new ArrayList<Field>();
    	Field field = null;
    	boolean usingPaths = false;
    	boolean usingFileType = false;
    	for (String key : dbFields.keySet())
    	{
    	   Upload.DbType value = (DbType)dbFields.get(key);
    	   switch (value)
    	   {
    	     case Content:
    	    	 field = new Field(dbTable, key, Field.Type.FILE);
       	         field.setFileInputStream(item.getInputStream());
       	         field.setFileSizeInBytes(item.getSize());
       	         usingFileType = true;
       	         break;
    	     case ContentType:	 
    	     case MimeType:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(item.getContentType());
       	         break;
    	     case Extn:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(FilenameUtils.getExtension(item.getName()));
       	         break;
    	     case FileName:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(item.getName());
       	         break;
    	     case FileSize:
    	    	 field = new Field(dbTable, key, Field.Type.LONG);
       	         field.setValue(String.valueOf(item.getSize())); 
       	         break;
    	     case SystemPath:
    	    	 pathFields.put(key, DbType.SystemPath);
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue("-");
       	         usingPaths = true;
       	         break;
    	     case WebPath:
    	    	 pathFields.put(key, DbType.WebPath);
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue("-");
       	         usingPaths = true;
       	         break;
    	     case Modified:
    	    	 Date thisMinute = new Date();
    	    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //Default
    	    	 field = new Field(dbTable, key, Field.Type.DATE);
    	    	 DateFormat df = new DateFormat(field);
    	    	 df.setCustomFormat("yyyy-MM-dd HH:mm:ss");
    	    	 field.setDateFormat(df);
    	    	 field.setValue(sdf.format(thisMinute), Field.DIRECTION.FROM_CLIENT);    	   	     
       	         break;
    	     default:
                 throw new Exception("Unknown database type: " + value);	 
    	   }
    	   
    	   fields.add(field);
    	}
    	//Convert to an array of Field(s)
    	Field[] flds = new Field[fields.size()];
    	for(int i = 0; i < fields.size(); i++)
    	{
    		flds[i] = fields.get(i);
    	}
    	query.setFields(flds);    	
    	db.setQuery(query);
    	db.executeInsertUpdate();
        id = query.getNewRecordID();
        //CLOSE THE INPUTSTREAM!!!
        if(usingFileType)
            item.getInputStream().close();
    		
        // Update the newly inserted row with the path information replacing the dashes (-). We have to
        // use a second statement here as we don't know in advance what the
        // database schema is and don't want to prescribe that certain triggers
        // etc be created. It makes it a bit less efficient but much more
        // compatible.
        if(usingPaths)
        {
          if(pathFields.size() > 0)
          {
            String sysPathWFileName = "";	
            String webPathWFileName = "";
            String platform = getPlatform();
            if(platform.equals("Windows"))
    	      sysPathWFileName = sysPath + File.separator + File.separator + String.valueOf(id) + "." + FilenameUtils.getExtension(item.getName());
            else
        	  sysPathWFileName = sysPath + File.separator + String.valueOf(id) + "." + FilenameUtils.getExtension(item.getName());  
            webPathWFileName = "/" + STORAGEDIR + "/" + String.valueOf(id) + "." + FilenameUtils.getExtension(item.getName());
    	    fields.clear();
    	    query = new Query(Query.Type.UPDATE, dbTable);
    	    for(String key : pathFields.keySet())
    	    {
    		  field = new Field(dbTable, key, Field.Type.STRING);
    		  DbType type = pathFields.get(key);
    		  if(type == DbType.WebPath)
    		  {
    			 field.setValue(webPathWFileName);
    		  }
    		  else if(type == DbType.SystemPath)
    		  {
    			 field.setValue(sysPathWFileName);
    		  }
    		  fields.add(field);
    	    }
    	    //Convert to an array of type Field
    	    Field[] fds = new Field[fields.size()];
    	    for(int i = 0; i < fields.size(); i++)
      	    {
      		  fds[i] = fields.get(i);
      	    }
    	    query.setFields(fds);
    	    Field idf = new Field(dbTable, this.dbPKey, Field.Type.INT);
    	    WhereCondition wc = new WhereCondition(idf, id, "=");
    	    WhereCondition[] conditions = new WhereCondition[1];
    	    conditions[0] = wc;
    	    query.setWhereConditions(conditions);
      	    db.setQuery(query);
            db.executeInsertUpdate();
          }
        }
    	
        return id;
    }
    
    private void update(Database db, FileItem item)throws Exception
    {
    	Query query = new Query(Query.Type.UPDATE, dbTable);
    	LinkedHashMap<String,DbType> pathFields = new LinkedHashMap<String, DbType>();
    	ArrayList<Field> fields = new ArrayList<Field>();
    	Field field = null;
    	boolean usingPaths = false;
    	boolean usingFileType = false;
    	for (String key : dbFields.keySet())
    	{
    	   Upload.DbType value = (DbType)dbFields.get(key);
    	   switch (value)
    	   {
    	     case Content:
    	    	 field = new Field(dbTable, key, Field.Type.FILE);
       	         field.setFileInputStream(item.getInputStream());
       	         field.setFileSizeInBytes(item.getSize());
       	         usingFileType = true;
       	         break;
    	     case ContentType:	 
    	     case MimeType:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(item.getContentType());
       	         break;
    	     case Extn:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(FilenameUtils.getExtension(item.getName()));
       	         break;
    	     case FileName:
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue(item.getName());
       	         break;
    	     case FileSize:
    	    	 field = new Field(dbTable, key, Field.Type.LONG);
       	         field.setValue(String.valueOf(item.getSize())); 
       	         break;
    	     case SystemPath:
    	    	 pathFields.put(key, DbType.SystemPath);
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue("-");
       	         usingPaths = true;
       	         break;
    	     case WebPath:
    	    	 pathFields.put(key, DbType.WebPath);
    	    	 field = new Field(dbTable, key, Field.Type.STRING);
       	         field.setValue("-");
       	         usingPaths = true;
       	         break;
    	     case Modified:
    	    	 Date thisMinute = new Date();
    	    	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //Default
    	    	 field = new Field(dbTable, key, Field.Type.DATE);
    	    	 DateFormat df = new DateFormat(field);
    	    	 df.setCustomFormat("yyyy-MM-dd HH:mm:ss");
    	    	 field.setDateFormat(df);
    	    	 field.setValue(sdf.format(thisMinute), Field.DIRECTION.FROM_CLIENT);    	   	     
       	         break;
    	     default:
                 throw new Exception("Unknown database type: " + value);	 
    	   }
    	   
    	   fields.add(field);
    	}
    	//Convert to an array of Field(s)
    	Field[] flds = new Field[fields.size()];
    	for(int i = 0; i < fields.size(); i++)
    	{
    		flds[i] = fields.get(i);
    	}
    	query.setFields(flds);
    	WhereCondition[] where = new WhereCondition[1];
  	    Field pkfield = new Field("",dbPKey,Field.Type.INT);	  
  	    where[0] = new WhereCondition(pkfield, rowID, "=");
  	    query.setWhereConditions(where);
    	db.setQuery(query);
    	db.executeInsertUpdate();
    	//CLOSE THE INPUTSTREAM!!!
    	if(usingFileType)
           item.getInputStream().close();
            		
        // Update the record with the path information replacing the dashes (-) if required. We have to
        // use a second statement here as we don't know in advance what the
        // database schema is and don't want to prescribe that certain triggers
        // etc be created. It makes it a bit less efficient but much more
        // compatible.
    	if(usingPaths)
    	{
          if(pathFields.size() > 0)
          {
            String sysPathWFileName = "";	
            String webPathWFileName = "";
            String platform = getPlatform();
            if(platform.equals("Windows"))
    	      sysPathWFileName = sysPath + File.separator + File.separator + String.valueOf(rowID) + "." + FilenameUtils.getExtension(item.getName());
            else
        	  sysPathWFileName = sysPath + File.separator + String.valueOf(rowID) + "." + FilenameUtils.getExtension(item.getName());  
            webPathWFileName = "/" + STORAGEDIR + "/" + String.valueOf(rowID) + "." + FilenameUtils.getExtension(item.getName());
    	    fields.clear();
    	    query = new Query(Query.Type.UPDATE, dbTable);
    	    for(String key : pathFields.keySet())
    	    {
    		  field = new Field(dbTable, key, Field.Type.STRING);
    		  DbType type = pathFields.get(key);
    		  if(type == DbType.WebPath)
    		  {
    			 field.setValue(webPathWFileName);
    		  }
    		  else if(type == DbType.SystemPath)
    		  {
    			 field.setValue(sysPathWFileName);
    		  }
    		  fields.add(field);
    	    }
    	    //Convert to an array of type Field
    	    Field[] fds = new Field[fields.size()];
    	    for(int i = 0; i < fields.size(); i++)
      	    {
      		  fds[i] = fields.get(i);
      	    }
    	    query.setFields(fds);
    	    Field idf = new Field(dbTable, this.dbPKey, Field.Type.INT);
    	    WhereCondition wc = new WhereCondition(idf, rowID, "=");
    	    WhereCondition[] conditions = new WhereCondition[1];
    	    conditions[0] = wc;
    	    query.setWhereConditions(conditions);
      	    db.setQuery(query);
            db.executeInsertUpdate();
          }
    	}
    }
    
            
    protected static String getPlatform()
    {
      String platform = System.getProperty("os.name").toLowerCase();
      String temp = "";
      
      if(platform.indexOf("nix") >= 0 || platform.indexOf("nux") >= 0)
      {
      	temp = "Unix";
      }
      else if(platform.indexOf("mac") >= 0 )
      {
      	temp = "Mac";
      }
      else if(platform.indexOf("win") >= 0)
      {
      	temp = "Windows";
      }
      else if(platform.indexOf("sunos") >= 0)
      {
      	temp = "Solaris";	
      }
      return temp;
    }
}
