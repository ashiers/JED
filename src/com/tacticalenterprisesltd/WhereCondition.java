package com.tacticalenterprisesltd;

/**
 * Use this class to set conditions on a WHERE clause for a query.
 * By default the operand is the equals "=" symbol.  You can change this
 * to: &lt;,&gt;,&lt;=,&gt;=,LIKE, BETWEEN, IS NULL, IS NOT NULL, etc.
 * 
 * If you have an instance where you want to apply a condition that
 * tests if a field is null or not null see the following example:
 * <code>
 * //For A Select query: SELECT * FROM basic WHERE basic.city IS NULL<br>
 * Field fld3 = new Field("basic","city", Field.Type.STRING);<br>
 * WhereCondition wc = new WhereCondition(fld3,"","IS NULL");<br>
 * </code>
 * Another scenario would be to test between two dates:
 * <code>
 * //For A Select query: SELECT * FROM basic WHERE basic.registered_date BETWEEN '2011-07-02' AND '2011-12-23'<br>
 * Field fld4 = new Field("basic","registered_date", Field.Type.DATE);<br>
 * DateFormat df = new DateFormat(fld4, "yyyy-MM-dd");<br>
 * fld4.setDatePattern("yyyy-MM-dd");<br>
 * WhereCondition wc = new WhereCondition(fld4,"'2011-07-02' AND '2011-12-23'","BETWEEN");<br>
 * </code>
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class WhereCondition
{
  private Field field = null;
  private Object value = null;
  private String operand = "="; //DEFAULT
    
  public WhereCondition(Field FIELD, Object VALUE, String OP)
  {
	  field = FIELD;
	  operand = OP;
	  value = VALUE;	  
  }
  /**
   * Set the key
   * @param value
   */
  public void setKey(Field value)
  {
	  field = value;
  }
  /**
   * Get the key
   * @return
   */
  public Field getKey()
  {
	  return field;
  }
  /**
   * Set the value
   * @param somevalue
   */
  public void setValue(Object somevalue)
  {
	  value = somevalue;
  }
  /**
   * Get the value
   * @return
   */
  public Object getValue()
  {
	  return value;
  }
  /**
   * Set the operand
   * @param value
   */
  public void setOperand(String value)
  {
	  operand = value;
  }
  /**
   * Get the operand
   * @return
   */
  public String getOperand()
  {
	  return operand;
  }
  
  public String toStringWithValues()
  {
	  String temp = "";
	  if(value instanceof String)
	  {		 
	    temp =  field.toString() + " " + operand + " '" + value.toString() + "'";		 
	  }
	  else
	  {		  
		temp = field.toString() + " " + operand + " " + value.toString();		  
	  }
	  return temp;
  }
  /**
   * This method combines all elements to produce a proper String as part
   * of a WHERE condition on an SQL statement.
   * @return String
   */
  @Override
  public String toString()
  {
	  String temp = "";
	  if(operand.equalsIgnoreCase("IS NULL"))
		  temp = field.toString() + " " + operand;
	  else if(operand.equalsIgnoreCase("IS NOT NULL"))
		  temp = field.toString() + " " + operand;
	  else if(operand.equalsIgnoreCase("BETWEEN"))
		  temp = field.toString() + " " + operand + " " + value;
	  else
	      temp = field.toString() + " " + operand + " ?";
	  return temp;
  }
}

