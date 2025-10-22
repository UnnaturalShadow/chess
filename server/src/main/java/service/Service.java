package service;

//import dataaccess.exceptions.BadRequestException;

import dataaccess.DataAccessException;

public class Service
{
    void checkForBadRequest(Object... requestFields) throws DataAccessException
    {
        for (Object requestField: requestFields)
        {
            if (requestField == null)
            {
                throw new DataAccessException("A field was missing");
            }
        }
    }
}