package service;

//import dataaccess.exceptions.BadRequestException;

//import dataaccess.DataAccessException;
import dataaccess.exceptions.BadRequestException;

public class Service
{
    void checkForBadRequest(Object... requestFields) throws BadRequestException
    {
        for (Object requestField: requestFields)
        {
            if (requestField == null)
            {
                throw new BadRequestException("A field was missing");
            }
        }
    }
}