package com.tacticalenterprisesltd;
/**
 * This class contains a message to be returned to the client side if
 * there is an error during the validation phase.
 * 
 * @author Alan Shiers
 * @version 1.5.0
 *
 */
public class ValidationMessage 
{
  private boolean result = true;
  private String errorMessage = "";
  
  
  public ValidationMessage(boolean vresult)
  {
	  result = vresult;
  }
  
  public ValidationMessage(boolean vresult, String errMessage)
  {
	 result = vresult;
	 errorMessage = errMessage;
  }
  /**
   * Inquire if the validation failed or passed.
   * @return
   */
  public boolean isValid()
  {
	  return result;
  }
  /**
   * Get the message if the validation failed.
   * @return
   */
  public String getMessage()
  {
	  return errorMessage;
  }
}


