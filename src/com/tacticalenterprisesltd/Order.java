package com.tacticalenterprisesltd;
/**
 * This class is used as part of the query whereby you need to indicate
 * for an ORDER BY clause the Field name and the direction of the order: "asc" or "desc"
 * @author Alan Shiers
 * @version 1.5.0
 */
public class Order
{
   
  private String fieldName = "";
  private String direction = "";
  
  public Order(String field, String dir)
  {
	  fieldName = field;
	  direction = dir;
  }
  /**
   * Set the name of the Field
   * @param name
   */
  public void setField(String name)
  {
	  fieldName = name;
  }
  /**
   * Get the name of the Field
   * @return
   */
  public String getField()
  {
	  return fieldName;
  }
  /**
   * Set the direction of the order. It should be either "asc" or "desc".
   * @param dir
   */
  public void setDirection(String dir)
  {
	  direction = dir;
  }
  /**
   * Get the direction of the order.
   * @return
   */
  public String getDirection()
  {
	  return direction;
  }
  /**
   * This method combines all the information required for the ORDER BY clause.
   */
  @Override
  public String toString()
  {
	  if(direction.equals(""))
		  return fieldName;
	  return fieldName + " " + direction;
  }
}

