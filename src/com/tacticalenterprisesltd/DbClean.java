package com.tacticalenterprisesltd;
/**
 * This interface is to be used in conjunction with the Upload class.  Create a concrete class
 * that implements this interface and pass it to method Upload.setDbCleanInstance(DbClean dbc).
 * If you do not set this instance, Upload will perform its own cleanup routine.  That routine may
 * or may not be appropriate for your circumstances.  If the latter is true, create a class using this interface that performs
 * the appropriate actions to take to clean up your database and any thing else you deem appropriate.
 * The cleanup routine in Upload will first determine what records in your database table are no longer being 
 * referenced and will pass to your clean(...) method the ids of those records.
 * @author Alan
 * @version 1.5.0
 */
public interface DbClean
{
   public boolean clean(int[] ids);
}
