package com.tacticalenterprisesltd;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * <P>An instance of this class is used by a Field to ensure the user on the client side
 * is providing valid data on an Edit or Create form.</p>
 * 
 * <p>Some Validate.Type(s) require additional arguments to test against. Use the appropriate constructor to supply arguments. All arguments
 * are expected to be of type String contained in a String[] array, but in most cases those arguments have numeric content. See the table
 * below to determine which types require arguments.</p>
 * 
 * <p>NOTE: to combat against illegal SQL Insertion attacks, this class checks for invalid character sequences.  If you have a field in your
 * database table that accepts optional values, you can use the NOTREQUIRED validation type.  This type will still perform checks
 * on invalid character sequences if anything is provided as input. As this class attempts to safeguard against malicious attacks, it is by no means
 * a solid cure.  If the user wants to pursue further protection, he/she can consult the following website for guidance: http://www.owasp.org/index.php/Main_Page</p><br>
 * <table border="1">
 * <thead><tr><th>Type</th><th>Requires Arguments</th><th>How Many</th><th>Argument Type</th></tr></thead>
 * <tbody>
 * <tr><td>REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>NOTREQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>DATE_FORMAT</td><td><center>true</center></td><td><center>1</td><td>use DateFormat constant</td></tr>
 * <tr><td>EMAIL_REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>BOOLEAN_REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>NUMERIC_REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>MINNUM_REQUIRED</td><td><center>true</center></td><td><center>1</center></td><td>An integer value representing the smallest numeric value this field is permitted.</td></tr>
 * <tr><td>MAXNUM_REQUIRED</td><td><center>true</center></td><td><center>1</center></td><td>An integer value representing the largest numeric value this field is permitted.</td></tr>
 * <tr><td>MINMAXNUM_REQUIRED</td><td><center>true</center></td><td><center>2</center></td><td>Use two integer values: the first representing the smallest numeric value this field is permitted,<br>the second representing the largest numeric value this field is permitted.</td></tr>
 * <tr><td>MINLEN_REQUIRED</td><td><center>true</center></td><td><center>1</center></td><td>An integer value representing the minimum number of characters permitted in this field.</td></tr>
 * <tr><td>MAXLEN_REQUIRED</td><td><center>true</center></td><td><center>1</center></td><td>An integer value representing the maximum number of characters permitted in this field.</td></tr>
 * <tr><td>MINMAXLEN_REQUIRED</td><td><center>true</center></td><td><center>2</center></td><td>Use two integer values: the first representing the minimum number of characters permitted in this field,<br>the second representing the maximum number of characters permitted in this field.</td></tr>
 * <tr><td>IP_REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * <tr><td>URI_REQUIRED</td><td><center>false</center></td><td></td><td></td></tr>
 * </tbody>
 * </table>
 * <br>
 * <b>Usage Example:</b><br><br>
 * <code>
 * String[] arguments = new String[]{DateFormat.DATE_ISO_822};<br>
 * field1.setValidator(new Validate(Validate.Type.DATE_FORMAT, arguments));<br>
 * </code>
 * or<br>
 * <code>
 * String[] arguments = new String[]{&quot;5&quot;,&quot;25&quot;};<br>
 * field1.setValidator(new Validate(Validate.Type.MINMAXLEN_REQUIRED, arguments));<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class Validate
{
  public static enum Type {REQUIRED{
	  public String toString()
	  {
		  return "required";
	  }
  },NOTREQUIRED{
	  public String toString()
	  {
		  return "notrequired";
	  }
  },DATE_FORMAT{
	  public String toString()
	  {
		  return "date_format";
	  }
  },EMAIL_REQUIRED{
	  public String toString()
	  {
		  return "email_required";
	  }
  },BOOLEAN_REQUIRED{
	  public String toString()
	  {
		  return "boolean_required";
	  }
  },NUMERIC_REQUIRED{
	  public String toString()
	  {
		  return "numeric_required";
	  }
  },MINNUM_REQUIRED{
	  public String toString()
	  {
		  return "minNum_required";
	  }
  },MAXNUM_REQUIRED{
	  public String toString()
	  {
		  return "maxNum_required";
	  }
  },MINMAXNUM_REQUIRED{
	  public String toString()
	  {
		  return "minMaxNum_required";
	  }
  },MINLEN_REQUIRED{
	  public String toString()
	  {
		  return "minLen_required";
	  }
  },MAXLEN_REQUIRED{
	  public String toString()
	  {
		  return "maxLen_required";
	  }
  },MINMAXLEN_REQUIRED{
	  public String toString()
	  {
		  return "minMaxLen_required";
	  }
  },IP_REQUIRED{
	  public String toString()
	  {
		  return "ip_required";
	  }
  },URI_REQUIRED{
	  public String toString()
	  {
		  return "url_required";
	  }
  }
  };
  
  private Validate.Type valType = null;
  private String[] arguments = null;
  private Logger logger;
  private SQLInsertionAttackObservable obs = null;
  
  /**
   * Use this constructor when you don't need to provide additional arguments for validation purposes.
   * @param type
   */
  public Validate(Validate.Type type)
  {
	  valType = type;
	  if(Editor.isLoggingEnabled())
	  {
		  logger = Logger.getLogger(Validate.class);
	  }
	  obs = SQLInsertionAttackObservable.getInstance();		 
  }
  /**
   * Use this constructor when you do need to provide additional arguments.
   * For instance, when validating a DATE you should provide one argument which is the format you wish
   * to ensure is being used.<br>
   * Example:<br>
   * String[] arguments = new String[]{DateFormat.DATE_ISO_822};<br>
   * field1.setValidator(new Validate(Validate.Type.DATE_FORMAT, arguments));<br>
   * @param type
   * @param args
   */
  public Validate(Validate.Type type, String[] args)
  {
	  valType = type;
	  arguments = args;
	  if(Editor.isLoggingEnabled())
	  {
		  logger = Logger.getLogger(Validate.class);
	  }
	  obs = SQLInsertionAttackObservable.getInstance();
  }
  /**
   * Set the validation type.
   * @param type
   */
  public void setValidatorType(Validate.Type type)
  {
	  valType = type;
  }
  /**
   * Get the validation type.
   * @return Validate.Type
   */
  public Validate.Type getValidatorType()
  {
	  return valType;
  }
  /**
   * Inquire as to whether or not a value is valid.
   * Results are based on the Validate.Type provided for the Field.
   * @param obj An Object 
   * @return a ValidationMessage
   */
  public ValidationMessage isValid(Object obj)
  {
	  String value = "";
	  ValidationMessage vm = null;
	  if(obj instanceof String)
	  {
		value = (String)obj;	  
	  
	    switch (valType) {
	    case BOOLEAN_REQUIRED:
		  vm = checkBoolean(value);
		  break;
	    case DATE_FORMAT:
		  vm = checkDateFormat(value);
		  break;
	    case EMAIL_REQUIRED:
		  vm = checkEmail(value);
		  break;
	    case IP_REQUIRED:
		  vm = checkIP(value);
		  break;
	    case MAXLEN_REQUIRED:
		  vm = checkMaxLen(value);
		  break;
	    case MAXNUM_REQUIRED:
		  vm = checkMaxNum(value);
		  break;
	    case MINLEN_REQUIRED:
		  vm = checkMinLen(value);
		  break;
	    case MINMAXLEN_REQUIRED:
		  vm = checkMinMaxLen(value);
		  break;
	    case MINMAXNUM_REQUIRED:
		  vm = checkMinMaxNum(value);
		  break;
	    case MINNUM_REQUIRED:
		  vm = checkMinNum(value);
		  break;
	    case NUMERIC_REQUIRED:
		  vm = checkNumeric(value);
		  break;
	    case REQUIRED:
		  vm = checkRequired(value);
		  break;
	    case NOTREQUIRED:
			  vm = checkNotRequired(value);
			  break;
	    case URI_REQUIRED:
		  vm = checkUriRequired(value);
		  break;	  	  
	    }
	  }
	  return vm;
  }
  
  private ValidationMessage checkBoolean(String val)
  {
	 ValidationMessage vm = null;
	 if(val.equals(""))
	 {
		 vm = new ValidationMessage(false,"Please enter true or false");
		 return vm;
	 }
	 vm = checkForInvalidInjectionCharacters(val);
	 if(vm.isValid() == false)
	 {
		return vm;
	 }
	 if(val.equalsIgnoreCase("true") && !val.equalsIgnoreCase("false"))
	 {
		 vm = new ValidationMessage(false,"Please enter true or false");
		 return vm;
	 }
	 vm = new ValidationMessage(true); 
	 return vm;
  }
  
  private ValidationMessage checkDateFormat(String val)
  {
	 ValidationMessage vm = null;
	 if(val.equals(""))
	 {
		 vm = new ValidationMessage(false,"Date is not in the expected format");
		 return vm;
	 }
	 vm = checkForInvalidInjectionCharacters(val);
	 if(vm.isValid() == false)
	 {
		return vm;
	 }
	 try{
	   String format = arguments[0];
	   SimpleDateFormat sdf = new SimpleDateFormat(format);
	   sdf.parse(val);
	 }
	 catch(ParseException pe)
	 {
		vm = new ValidationMessage(false,"Date is not in the expected format.");
		return vm;
	 }
	 catch(NullPointerException npe)
	 {
		  if(Editor.isLoggingEnabled())
		  {
			  logger.error("Validate.checkDateFormat: You need to provide an argument.\nUse one of the constants in class DateFormat.");
			  logger.error(Editor.getFullStackTrace(npe));
		  }
	 }
	 vm = new ValidationMessage(true); 
	 return vm;
  }
  
  private ValidationMessage checkEmail(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"Please enter a valid e-mail address");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  Pattern pattern = null;
	  Matcher matcher = null;
	 
	  String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	  
	  pattern = Pattern.compile(EMAIL_PATTERN);
	  matcher = pattern.matcher(val);
	  
	  if(!matcher.matches())
	  {
		  vm = new ValidationMessage(false,"Please enter a valid e-mail address");
		  return vm;
	  }
	  vm = new ValidationMessage(true); 
	  return vm;

  }
  
  private ValidationMessage checkIP(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"Please enter a valid ip address");
		 return vm;
	  }
	  Pattern pattern;
	  Matcher matcher;
	 
	  String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	  pattern = Pattern.compile(IPADDRESS_PATTERN);
	  matcher = pattern.matcher(val);
	  if(!matcher.matches())
	  {
		  vm = new ValidationMessage(false,"Please enter a valid IP address"); 
		  return vm;
	  }
	  vm = new ValidationMessage(true); 
	  return vm;

  }
  
  private ValidationMessage checkMaxLen(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  try{
	    int maxLength = Integer.parseInt(arguments[0]);
	    if(val.length() > maxLength)
	    {
	    	int diff = val.length() - maxLength;
	    	vm = new ValidationMessage(false,"The input is " + diff + " characters too long");
	    	return vm;
	    }
	  }
	  catch(NumberFormatException nfe)
	  {
		  vm = new ValidationMessage(false,"Arguement provided " + arguments[0] + " to validate maximum string length is not valid.");
		  return vm;
	  }
	  catch(NullPointerException npe)
	  {
		  if(Editor.isLoggingEnabled())
		  {
			  logger.error("Validate.checkMaxLen: You need to provide a numeric argument for this method.");
			  logger.error(Editor.getFullStackTrace(npe));
		  }
	  }
	  vm = new ValidationMessage(true); 
	  return vm;
  }
  
  private ValidationMessage checkMaxNum(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  if(!isNumeric(val))
	  {
		 vm = new ValidationMessage(false,"This field must be a numeric value");
		 return vm;
	  }
	  try{
		int value = Integer.parseInt(val);  
	    int max = Integer.parseInt(arguments[0]);
	    if(value > max)
	    {
	      vm = new ValidationMessage(false,"Number is too large, must be " + max + " or smaller");
		  return vm;
	    }
	  }
	  catch(NumberFormatException nfe)
	  {
		  vm = new ValidationMessage(false,"Arguement provided " + arguments[0] + " to validate maximum number is not valid.");
		  return vm;
	  }
	  catch(NullPointerException npe)
	  {
		  if(Editor.isLoggingEnabled())
		  {
			  logger.error("Validate.checkMaxNum: You need to provide a numeric argument for this method.");
			  logger.error(Editor.getFullStackTrace(npe));
		  }
	  }
	  vm = new ValidationMessage(true); 
	  return vm;
  }
  
  private ValidationMessage checkMinLen(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  try{
		int minLength = Integer.parseInt(arguments[0]);
		if(val.length() < minLength)
		{
		  	int diff = minLength - val.length();
		   	vm = new ValidationMessage(false,"The input is too short. " + minLength + " characters required. (" + diff + " more to go)");
		   	return vm;
		}
	  }
	  catch(NumberFormatException nfe)
	  {
		vm = new ValidationMessage(false,"Arguement provided " + arguments[0] + " to validate minimum string length is not valid.");
		return vm;
	  }
	  catch(NullPointerException npe)
	  {
		  if(Editor.isLoggingEnabled())
		  {
			  logger.error("Validate.checkMinLen: You need to provide a numeric argument for this method.");
			  logger.error(Editor.getFullStackTrace(npe));
		  }
	  }
	  vm = new ValidationMessage(true); 
	  return vm;
  }
  
  private ValidationMessage checkMinMaxLen(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  try{
		    int minLength = Integer.parseInt(arguments[0]);
		    int maxLength = Integer.parseInt(arguments[1]);
		    if(val.length() < minLength)
		    {
		    	int diff = minLength - val.length();
		    	vm = new ValidationMessage(false,"The input is too short. " + minLength + " characters required. (" + diff + " more to go)");
		    	return vm;
		    }
		    if(val.length() > maxLength)
		    {
		    	int diff = val.length() - maxLength;
		    	vm = new ValidationMessage(false,"The input is " + diff + " characters too long");
		    	return vm;
		    }
		  }
		  catch(NumberFormatException nfe)
		  {
			  vm = new ValidationMessage(false,"One or both arguements provided: " + arguments[0] + " and " + arguments[1] + " to validate minimum and maximum string length is not valid.");
			  return vm;
		  }
		  catch(NullPointerException npe)
		  {
			  if(Editor.isLoggingEnabled())
			  {
				  logger.error("Validate.checkMinMaxLen: You need to provide a 2 numeric arguments\nfor this method; a min value and a max value.");
				  logger.error(Editor.getFullStackTrace(npe));
			  }
		  }
		  vm = new ValidationMessage(true); 
		  return vm;
  }
  
  private ValidationMessage checkMinMaxNum(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  if(!isNumeric(val))
	  {
		 vm = new ValidationMessage(false,"This field must be a numeric value");
		 return vm;
	  }
	  try{
			int value = Integer.parseInt(val); 
			int min = Integer.parseInt(arguments[0]);
		    int max = Integer.parseInt(arguments[1]);
		    if(value < min)
		    {
		      vm = new ValidationMessage(false,"Number is too small, must be " + min + " or larger");
			  return vm;
		    }
		    if(value > max)
		    {
		      vm = new ValidationMessage(false,"Number is too large, must be " + max + " or smaller");
			  return vm;
		    }
		  }
		  catch(NumberFormatException nfe)
		  {
			  vm = new ValidationMessage(false,"One or both arguements provided: " + arguments[0] + " and " + arguments[1] + " to validate minimum and maximum numeric values is not valid.");
			  return vm;
		  }
		  catch(NullPointerException npe)
		  {
			  if(Editor.isLoggingEnabled())
			  {
				  logger.error("Validate.checkMaxNum: You need to provide 2 numeric arguments\nfor this method; a min value and a max value.");
				  logger.error(Editor.getFullStackTrace(npe));
			  }
		  }
		  vm = new ValidationMessage(true); 
		  return vm;
  }
  
  private ValidationMessage checkMinNum(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"This field is required");
		 return vm;
	  }
	  vm = checkForInvalidInjectionCharacters(val);
	  if(vm.isValid() == false)
	  {
		return vm;
	  }
	  if(!isNumeric(val))
	  {
		 vm = new ValidationMessage(false,"This field must be a numeric value");
		 return vm;
	  }
	  try{
			int value = Integer.parseInt(val); 
			int min = Integer.parseInt(arguments[0]);
		    
		    if(value < min)
		    {
		      vm = new ValidationMessage(false,"Number is too small, must be " + min + " or larger");
			  return vm;
		    }		    
		  }
		  catch(NumberFormatException nfe)
		  {
			  vm = new ValidationMessage(false,"Arguements provided: " + arguments[0] + " to validate a minimum numeric value is not valid.");
			  return vm;
		  }
		  catch(NullPointerException npe)
		  {
			  if(Editor.isLoggingEnabled())
			  {
				  logger.error("Validate.checkMinNum: You need to provide a numeric argument for this method.");
				  logger.error(Editor.getFullStackTrace(npe));
			  }
		  }
		  vm = new ValidationMessage(true); 
		  return vm;
  }
  
  private ValidationMessage checkNumeric(String val)
  {
	 ValidationMessage vm = null;
	 if(val.equals(""))
	 {
	   vm = new ValidationMessage(false, "This input must be given as a number");
	   return vm;
	 }
	 vm = checkForInvalidInjectionCharacters(val);
	 if(vm.isValid() == false)
	 {
		return vm;
	 }
	 String validChars = new String("ebx.");
	 char ch;
	 for(int i = 0; i < val.length(); i++ )
	 {
		 ch = val.charAt(i);
		 if(!Character.isDigit(ch) && validChars.indexOf(ch) == -1)
		 {
			 vm = new ValidationMessage(false, "This input must be given as a number");
			 return vm; 
		 }
	 }
	 vm = new ValidationMessage(true); 
	 return vm;
  }

 
  private ValidationMessage checkForInvalidInjectionCharacters(String val)
  {
	ValidationMessage vm = null;
	String stockReply = "Invalid input. Try again.";
	String invalidChars = ";'*%#()";
	char ch;
	for(int i = 0; i < val.length(); i++ )
	{
	  ch = val.charAt(i);
	  if(invalidChars.indexOf(ch) > -1)
	  {
		 vm = new ValidationMessage(false, stockReply);
		 if(obs.countObservers() > 0)
		 {
		   // alert Observers to an SQL Insertion Attack
		   obs.alert();
		 }
		 return vm; 
	  }
	}	
	
	if (val.contains("--"))
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (val.contains("/*"))
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (val.contains("*/"))
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("drop"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("alter"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("create"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("select"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("insert"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("update"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("delete"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("where not in"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("where not exist"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("waitfor"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("shutdown"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	if (Pattern.compile(Pattern.quote("exec"), Pattern.CASE_INSENSITIVE).matcher(val).find())
	{
	    vm = new ValidationMessage(false, stockReply);
	    if(obs.countObservers() > 0)
	    {
	      // alert Observers to an SQL Insertion Attack
	      obs.alert();
	    }
	    return vm;
	}
	
	vm = new ValidationMessage(true);
	return vm;
  }
  
  private ValidationMessage checkRequired(String val)
  {
	ValidationMessage vm = null;
	if(val.equals(""))
	{
	   vm = new ValidationMessage(false, "This field is required");
	   return vm;
	}
	vm = checkForInvalidInjectionCharacters(val);
	if(vm.isValid() == false)
	{
		return vm;
	}
	vm = new ValidationMessage(true); 
	return vm;
  }
  
  private ValidationMessage checkNotRequired(String val)
  {
	ValidationMessage vm = null;
	if(!val.equals(""))
	{
		vm = checkForInvalidInjectionCharacters(val);
		if(vm.isValid() == false)
		{
			return vm;
		}
	}
	
	vm = new ValidationMessage(true); 
	return vm;
  }
  
  private ValidationMessage checkUriRequired(String val)
  {
	  ValidationMessage vm = null;
	  if(val.equals(""))
	  {
		 vm = new ValidationMessage(false,"Please enter a valid URI");
		 return vm;
	  } 
	  if(val.indexOf("://") == -1)
	  {
		vm = new ValidationMessage(false,"Please enter a valid URI");
		return vm;
	  }
	  try{
	    URL url = new URL(val);
	    url.toURI();
	  }
	  catch(Exception e)
	  {
		  vm = new ValidationMessage(false,"Please enter a valid URI"); 
		  return vm;
	  }
	  vm = new ValidationMessage(true); 
	  return vm;
  }
  
  private boolean isNumeric(String value)
  {
	  String validChars = new String("ebx.");
	  char ch;
	  for(int i = 0; i < value.length(); i++ )
	  {
		 ch = value.charAt(i);
		 if(!Character.isDigit(ch) && validChars.indexOf(ch) == -1)
		 {
			 return false; 
		 }
	  }
	  return true;  
  }
}

