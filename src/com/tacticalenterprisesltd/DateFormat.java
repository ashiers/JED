package com.tacticalenterprisesltd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * <P>This class allows you to set a Field of type DATE to a specific
 * date format.  String constants are provided as possible formatting options.
 * If none are to your liking, you can create your own custom format. Refer
 * to java.text.SimpleDateFormat for more formatting options. Then use the
 * setCustomFormat(String datePattern) method to render your custom Date format.</P>
 * 
 * <P>NOTE: THIS CLASS ASSUMES THE DEFAULT DATABASE DATE FORMAT IS DATE_ISO_8601 = "yyyy-MM-dd".
 * THIS IS THE DEFAULT DATE FORMAT FOR MYSQL.  IF YOU ARE USING A DBMS (Database Management System)
 * THAT IS SOMETHING OTHER THAN MYSQL, YOU WILL NEED TO TAKE ADDITIONAL STEPS TO ENSURE THE 
 * DEFAULT DATE FORMAT IS SET FOR DATE_ISO_8601.</P>
 * <P>IN POSTGRESSQL DBMS, THIS IS POSSIBLE BY SETTING A PROPERTY NAMED <i>DateStyle(String)</i>.</p>
 * <p>IN ORACLE DBMS, YOU CAN ISSUE A COMMAND SIMILAR TO THIS:<br><br>
 * <i>alter session set nls_date_format='yyyy-mm-dd hh24:mi:ss';</i><br><br>
 * YOU CAN PLACE THIS COMMAND IN A CONNECTION FILE SO THAT IT IS RUN EVERY TIME FOR EVERY SESSION.
 * IF YOU ARE USING A DBMS OTHER THAN MYSQL, AND DON'T TAKE THE NECESSARY STEPS TO ENSURE 
 * A DEFAULT DATE FORMAT OF DATE_ISO_8601, YOU CAN EXPECT AN IllegalArgumentException AT RUN TIME.</P>
 * 
 * @see java.text.SimpleDateFormat
 * @author Alan Shiers
 * @version 1.5.0
 */
public class DateFormat
{
	/** Date format: 2012-03-09. jQuery UI equivalent format: yy-mm-dd */
	public static final String DATE_ISO_8601 = "yyyy-MM-dd";

	/** Date format: Fri, 9 Mar 12. jQuery UI equivalent format: D, d M y */
	public static final String DATE_ISO_822 = "EEE', 'dd' 'MMM' 'yy";
	
	/** Date format: Friday, 09-Mar-12.  jQuery UI equivalent format: DD, dd-M-y */
	public static final String DATE_ISO_850 = "EEEE', 'dd-MMM-yy";
	
	/** Date format: Fri, 9 Mar 2012. jQuery UI equivalent format: D, d M yy */
	public static final String DATE_ISO_1123 = "EEE', 'dd' 'MMM' 'yyyy";
	
	/** Date format: Fri, 9 March 2012. jQuery UI equivalent format: D, d MM yy */
	public static final String DATE_ISO_1123_CUSTOM1 = "EEE', 'dd' 'MMMM' 'yyyy";
	
	/** Date format: Friday, 9 March 2012. jQuery UI equivalent format: DD, d MM yy */
	public static final String DATE_ISO_1123_CUSTOM2 = "EEEE', 'dd' 'MMMM' 'yyyy";
	
	/** Date format: 13-JUN-98. jQuery UI equivalent format: dd-MMM-yy */
	public static final String DATE_ORACLE_DEFAULT = "dd-MMM-yy";
	
	/** Date format: 1331251200. jQuery UI equivalent format: @ */
	public static final String DATE_TIMESTAMP = "yyMMddHHmmss";
	
	/** Date format: 1331251200. jQuery UI equivalent format: @ */
	public static final String DATE_EPOCH = "yyMMddHHmmss";
	
	private String customFormat = "";
	
	private String defaultDBFormat = DATE_ISO_8601;
	
	private static Logger logger;
	
	private Field field = null;
	/**
	 * Constructor
	 * @param fld
	 */
	public DateFormat(Field fld)
	{
		field = fld;
		if(Editor.isLoggingEnabled())
		{
			  logger = Logger.getLogger(DateFormat.class);
		}
	}
	
	/**
	 * Constructor
	 * @param fld
	 * @param datePattern
	 */
	public DateFormat(Field fld, String datePattern)
	{
		field = fld;
		field.setDatePattern(datePattern);
		if(Editor.isLoggingEnabled())
		{
			  logger = Logger.getLogger(DateFormat.class);
		}
	}
	/**
	 * If you wish to enter your own custom date format,
	 * use the single parameter constructor to create a new 
	 * instance of this class, then call this method to set
	 * your custom Date pattern. As a guide, use java.text.SimpleDateFormat
	 * for more formatting options.
	 * @param datePattern
	 * @see SimpleDateFormat
	 */
	public void setCustomFormat(String datePattern)
	{
		customFormat = datePattern;
		if(field != null)
		{
			field.setDatePattern(customFormat);
		}
	}
	/**
	 * Get the custom Date format pattern
	 * @return A String
	 */
	public String getCustomFormat()
	{
		return customFormat;
	}
	
	
	/**
	 * Convert an SQL date string from the database to a date string
	 * formatted as indicated by any of the constants provided. If the
	 * date should contain a time signature, this method tests for a space
	 * character between the date and time in order to separate the individual
	 * parts: day, month, year, hours, minutes, seconds.<br>
	 * Example: 2016-01-11 08:00:00.0
	 * @param value
	 * @return A String
	 */
	public String date_sql_to_format(String value) throws IllegalArgumentException
	{
		if(isDateFormatISO8601(value) == false)
		{
			throw new IllegalArgumentException("Date Format From Database is not ISO_8601 format!");			
		}
		//Example: 2016-01-11 08:00:00.0
		String strDate = "";
		String strTime = "";
		String[] arr = value.split(" "); //<<<This may prove to be problematic later! As long as db date format is consistently the same, then no problem.
		if(arr.length == 1)
		{
			strDate = arr[0];
		}
		else if(arr.length == 2)
		{
			strDate = arr[0];
			strTime = arr[1];
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(field.getDatePattern());
		StringTokenizer tok = new StringTokenizer(strDate, " -/|\\");
		String result = "";
		int year = -1;
		int month = -1;
		int day = -1;
		int hours = -1;
		int minutes = -1;
		int seconds = -1;
		
		try{
		  if(strTime.equals("")) //There is no time signature
		  {
		    int count = 0;
		    //Process the just the DATE
		    while(tok.hasMoreTokens())
		    {
		      if(count == 0)
		      {
			    year = Integer.parseInt(tok.nextToken());			    
		      }
		      else if(count == 1)
		      {
			    month = Integer.parseInt(tok.nextToken()) - 1;			    
		      }
		      else if(count == 2)
		      {
		    	day = Integer.parseInt(tok.nextToken());
			    break;
		      }
		      count++;
		    }
		    GregorianCalendar gc = new GregorianCalendar(year,month,day);
		    Date date = gc.getTime();
		    result = sdf.format(date);
		  }
		  else
		  {
			  //Process both the DATE and TIME
			  StringTokenizer tok2 = new StringTokenizer(strTime, ":");
			  int count = 0;
			  while(tok.hasMoreTokens())
			  {
			    if(count == 0)
			    {
				  year = Integer.parseInt(tok.nextToken());				  
			    }
			    else if(count == 1)
			    {
				  month = Integer.parseInt(tok.nextToken()) - 1;				  
			    }
			    else if(count == 2)
			    {
			    	day = Integer.parseInt(tok.nextToken());				    			  
				    break;
			    }
			    count++;
			  }
			  count = 0; //reset
			  while(tok2.hasMoreTokens())
			  {
			    if(count == 0)
			    {
				  hours = Integer.parseInt(tok2.nextToken());				  
			    }
			    else if(count == 1)
			    {
				  minutes = Integer.parseInt(tok2.nextToken());				  
			    }
			    else if(count == 2)
			    {
			      String token = tok2.nextToken();
			      if(token.contains("."))
			      {
			    	int index = token.indexOf(".");
			    	seconds = Integer.parseInt(token.substring(0,index));
			      }
			      else
			      {
			    	seconds = Integer.parseInt(token);
			      }				  
				  break;
			    }
			    count++;
			  }
			  GregorianCalendar gc = new GregorianCalendar(year,month,day,hours,minutes,seconds);
			  Date date = gc.getTime();
			  result = sdf.format(date);			  
		  }
		}
		catch(Exception e){
			if(Editor.isLoggingEnabled())
			{
		      logger.error(Editor.getFullStackTrace(e));
			}
			return "";
		}
		//System.out.println("DateFormat.date_sql_to_format: value: " + value + " db result: " + result);
		return result;
		
	}
	/**
	 * Convert a formatted date String back to an SQL date string
	 * the database will recognize.  For the time being, this conversion
	 * favors the date formatting recognized by the MySQL database DATE_ISO_8601. 
	 * If your date should contain a time signature, this method tests for a space
	 * character between the date and time.
	 * @param value	The formatted date 
	 * @return A String
	 */
	public String date_format_to_sql(String value)
	{
		String result = "";
		String pattern = field.getDatePattern();
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		SimpleDateFormat sdf2 = null;
		if(value.contains(":"))
		  sdf2 = new SimpleDateFormat(defaultDBFormat + " HH:mm");
		else
		  sdf2 = new SimpleDateFormat(defaultDBFormat);
		try{
		  Date date = sdf.parse(value);
		  result = sdf2.format(date);
		  //System.out.println("DateFormat.date_format_to_sql: value: " + value + " field pattern: " + pattern + " db result: " + result);
		}
		catch(ParseException pe)
		{
			//pe.printStackTrace();
			return "";
		}
		return result;
	}

	/**
	 * This convenience method expects a database Date value that adheres to this format: yyyy-MM-dd HH:mm:ss 
	 * The clientValue parameter can be any format you wish to provide as recognized by java.text.SimpleDateFormat.
	 * @param dbvalue
	 * @param clientvalue
	 * @return
	 * @throws IllegalArgumentException
	 */
    public static String db_to_client_format(String dbvalue, String clientvalue) throws IllegalArgumentException
    {
       if(dbvalue.indexOf("-") == -1)
       {
           throw new IllegalArgumentException("Date Format From Database is not ISO_8601 format!");
       }
       String result = "";
       try
       {
           String pattern = clientvalue;
           String[] parts = dbvalue.split(" ");
           String[] dateparts = parts[0].split("-");
           String[] timeparts = parts[1].split(":");
           int year = Integer.parseInt(dateparts[0]);
           int month = Integer.parseInt(dateparts[1]);
           int day = Integer.parseInt(dateparts[2]);
           int hours = Integer.parseInt(timeparts[0]);
   		   int minutes = Integer.parseInt(timeparts[1]);
   		   int seconds = Integer.parseInt(timeparts[2]);
           GregorianCalendar cal = new GregorianCalendar(year, month, day, hours, minutes, seconds);
           SimpleDateFormat sdf = new SimpleDateFormat(pattern);
           result = sdf.format(cal.getTime());
       }
       catch (Exception e)
       {
    	   if(Editor.isLoggingEnabled())
			{
		      logger.error(Editor.getFullStackTrace(e));
			}
           return "";
       }

       return result;

  }
    
    protected static java.sql.Date convertDBDateStringToDate(String strDate)
    {
    	java.sql.Date date = null; 
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //Default DATE_ISO_8601
    	try{
    	   	java.util.Date udate = sdf.parse(strDate);
    	    date = new java.sql.Date(udate.getTime());
    	}
    	catch(ParseException pe)
    	{
    		if(Editor.isLoggingEnabled())
			{
		      logger.error(Editor.getFullStackTrace(pe));
			}
    	}
    	
    	return date;
    }

  	private static boolean isDateFormatISO8601(String value)
	{
		boolean ok = true;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_ISO_8601);
		try{
			  sdf.parse(value);			  
		}
		catch(ParseException pe)
		{
			//pe.printStackTrace();
			ok = false;
		}
		return ok;
	}
}



