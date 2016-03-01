package com.tacticalenterprisesltd;

import java.util.ArrayList;
/**
 * Use this class to group two or more WhereCondition(s) with logical operators AND, OR.
 * When multiple WhereCondition(s) are grouped together they are wrapped by parenthesis.
 * Example:<br><br>
 * <code>
 * //SELECT city,name,ranking<br>
 * //FROM suppliers<br>
 * //WHERE (city = 'New York' AND name = 'IBM')<br>
 * //OR (ranking >= 10);<br>
 * Database dbase = new Database("editor");<br>
 * Query query = new Query(Query.Type.SELECT, "suppliers");<br>
 * dbase.setQuery(query);<br>
 * Field fld1 = new Field("suppliers","city", Field.Type.STRING);<br>
 * Field fld2 = new Field("suppliers","name", Field.Type.STRING);<br>
 * Field fld3 = new Field("suppliers","ranking", Field.Type.INT);<br>
 * 
 * Field[] flds = new Field[3];<br>
 * flds[0] = fld1;<br>
 * flds[1] = fld2;<br>
 * flds[2] = fld3;<br>
 * query.setFields(flds);<br>
 * 
 * WhereCondition wc1 = new WhereCondition(fld1,"New York","=");<br>
 * WhereCondition wc2 = new WhereCondition(fld2,"IBM","=");<br>
 * WhereCondition wc3 = new WhereCondition(fld3,10,">=");<br>
 * 
 * WhereConditionGroups wgs = new WhereConditionGroups();<br>
 * wgs.addWhereConditionGroup(wc1, WhereConditionGroups.LogicOperator.AND, wc2);<br>
 * wgs.addLogicOperatorBetweenGroups(WhereConditionGroups.LogicOperator.OR);<br>
 * wgs.addWhereConditionGroup(wc3);<br>
 * 
 * query.setWhereConditionGroups(wgs);<br>
 * System.out.println("Query: " + query.toString());<br>
 * //Output: Query: SELECT suppliers.city,suppliers.name,suppliers.ranking FROM suppliers WHERE (suppliers.city = ? AND suppliers.name = ?) OR (suppliers.ranking >= ?)<br> 
 * String[][] result = dbase.executeSelect();<br>
 * ...
 * <code>
 * @author Alan Shiers
 * @version 1.5.0
 */
public class WhereConditionGroups
{
  public enum LogicOperator{AND,OR}
  private ArrayList<Group> conditions = new ArrayList<Group>();
  private ArrayList<String> operators = new ArrayList<String>();
  /**
   * Get all the WhereCondition(s) as an array.
   * @return WhereCondition[]
   */
  public WhereCondition[] getAllWhereConditions()
  {
	  WhereCondition[] conds = null;
	  int count = 0;
	  if(conditions.size() > 0)
	  {
		  for(Group gp : conditions)
		  {
			count += gp.getWhereConditionCount();
		  }
		  //Dimension the array based on the count.
		  conds = new WhereCondition[count];
		  WhereCondition[] temp = null;
		  Group gp = null;
		  int index = 0;
		  //Acquire all the WhereCondition objects and load into conds.
		  for(int i = 0; i < conditions.size(); i++)
		  {
			 gp = conditions.get(i);
			 temp = gp.getWhereConditions();
			 for(int j = 0; j < temp.length; j++)
			 {
				conds[index] = temp[j];  
			 }
		  }
	  }
	  else
	  {
		  conds = new WhereCondition[0];
	  }
	  return conds;
  }
  /**
   * Add two WhereCondition(s) with a LogicOperator between them.
   * @param cond1
   * @param operator
   * @param cond2
   */
  public void addWhereConditionGroup(WhereCondition cond1, LogicOperator operator, WhereCondition cond2)
  {
	 conditions.add(new Group(cond1,operator,cond2));
  }
  /**
   * Add one WhereCondition to stand as its own group.
   * @param cond1
   */
  public void addWhereConditionGroup(WhereCondition cond1)
  {
	  conditions.add(new Group(cond1));
  }
  /**
   * Add a LogicOperator between WhereConditionGroup(s).
   * @param operator
   */
  public void addLogicOperatorBetweenGroups(LogicOperator operator)
  {
	  if(operator == LogicOperator.AND)
	  {
		  operators.add(" AND ");
	  }
	  else
	  {
		  operators.add(" OR ");
	  }
  }
  @Override
  public String toString()
  {
	  String temp = "";
	  if(conditions.size() == 1)
	  {
		  temp += conditions.get(0).toString();
	  }
	  else
	  {
		  for(int i = 0; i < conditions.size(); i++)
		  {
			  temp += conditions.get(i).toString();
			  if(operators.size() > 0)
			  {
				  try{
					  temp += operators.get(i);
				  }
				  catch(IndexOutOfBoundsException ie){}
			  }
		  }
	  }
	  return temp;
  }
  
  /**
   * This class will allow you to group one or two WhereConditions together so that their
   * output results in a grouping between parenthesis: (...).
   * 
   */
  class Group
  {
	  private WhereCondition cond1 = null;
	  private WhereCondition cond2 = null;
	  private String operator = "";
	  /**
	   * Constructor
	   * @param condition1
	   * @param lo
	   * @param condition2
	   */
	  public Group(WhereCondition condition1, LogicOperator lo, WhereCondition condition2)
	  {
		  cond1 = condition1;
		  cond2 = condition2;
		  if(lo == LogicOperator.AND)
		  	  operator = " AND ";			  
		  else
			  operator = " OR ";
	  }
	  /**
	   * Constructor 
	   * @param condition1
	   */
	  public Group(WhereCondition condition1)
	  {
		  cond1 = condition1;
	  }
	  /**
	   * Get all WhereCondition(s) in this Group.
	   * @return WhereCondition[]
	   */
	  public WhereCondition[] getWhereConditions()
	  {
		  WhereCondition[] conditions = null;
		  
		  if(cond2 != null)
		  {
			  conditions = new WhereCondition[2];
			  conditions[0] = cond1;
			  conditions[1] = cond2;
		  }
		  else
		  {
			  conditions = new WhereCondition[1];
			  conditions[0] = cond1;
		  }
		  return conditions;
	  }
	  /**
	   * Get the number of WhereCondition(s) in this group.
	   * @return
	   */
	  public int getWhereConditionCount()
	  {
		  int count = 0;
		  if(cond1 != null)
			  count++;
		  if(cond2 != null)
			  count++;
		  return count;
	  }
	  @Override
	  public String toString()
	  {
		  if(cond2 == null)
			  return "(" + cond1.toString() + ")";
		  return "(" + cond1.toString() + operator + cond2.toString() + ")";
	  }
  }
}
