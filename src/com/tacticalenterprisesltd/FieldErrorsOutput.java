package com.tacticalenterprisesltd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
/**
 * This class contains the members required to return a response to the client
 * side whenever there is a FieldError during the validation phase over all Fields.
 * An instance of this class is passed to the constructor of an instance of GSon
 * for processing as a JSON string.
 * 
 * @author Alan Shiers
 * @version 1.5.0
 */
public class FieldErrorsOutput
{
	public ArrayList<LinkedHashMap<String,String>> fieldErrors = new ArrayList<LinkedHashMap<String,String>>();	
}


