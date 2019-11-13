package exceptions;

public class QuitException extends Exception
{

    public QuitException()
    {
        super("Operation quit.");
    }

    public QuitException(String message)
    {
        super(message);
    }
}
