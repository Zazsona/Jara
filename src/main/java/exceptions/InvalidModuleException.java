package exceptions;

/**
 * Exception thrown when a module configuration is invalid
 */
public class InvalidModuleException extends RuntimeException
{
    public InvalidModuleException(String message)
    {
        super(message);
    }
}
