package com.tacticalenterprisesltd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
/**
 * This interface is being implemented by SSPOutput and NonSSPOutput because the
 * &quot;<i>data</i>&quot; class member resides in both classes and I wanted Polymorphic capabilities
 * through this interface which I use in the Join class.
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public interface BasicOutput
{
  public void addDataRow(LinkedHashMap<String,Object> row);
  public ArrayList<LinkedHashMap<String,Object>> getData();
}
