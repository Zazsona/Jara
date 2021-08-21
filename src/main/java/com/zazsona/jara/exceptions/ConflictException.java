package com.zazsona.jara.exceptions;

/**
 * An exception indicating two values within different objects match when they cannot.
 */
public class ConflictException extends Exception
{
    public ConflictException(String message)
    {
        super(message);
    }
}
