package exception;

public class ResponseException extends Exception
{

    public enum Code
    {
        ServerError,
        ClientError,
    }

    final private Code code;

    public ResponseException(Code code, String message)
    {
        super(message);
        this.code = code;
    }

    public static Code fromHttpStatusCode(int httpStatusCode)
    {
        return switch (httpStatusCode)
        {
            case 500 -> Code.ServerError;
            case 400 -> Code.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }
}