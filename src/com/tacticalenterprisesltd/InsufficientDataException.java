package com.tacticalenterprisesltd;

/**
 * An exception thrown by some methods when there is insufficient data provided.
 * @author Alan Shiers
 * @version 1.5.0
 */
public class InsufficientDataException extends Exception
{
	private static final long serialVersionUID = -5888581636765079050L;

	public InsufficientDataException(String str)
	{
		super(str);
	}
}
