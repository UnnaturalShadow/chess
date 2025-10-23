package dataaccess.exceptions;

public class NotAValidColorException extends RuntimeException
{
    public NotAValidColorException(String message)
    {
        super(message);
    }
}