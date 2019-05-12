package exceptions;

public class InvalidModuleException extends RuntimeException
{
    public InvalidModuleException(String message)
    {
        super(message);
    }
}
