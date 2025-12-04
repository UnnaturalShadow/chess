package service;
import dataaccess.DaoCollection;
import dataaccess.DataAccessException;
import dataaccess.exceptions.BadRequestException;
import dataaccess.exceptions.UserNotValidatedException;

import java.util.UUID;

public class AuthService extends Service
{
    public DaoCollection daos;
    public AuthService(DaoCollection daos)
    {
        this.daos = daos;
    }

    public String generateNewToken(String username) throws DataAccessException
    {
        if (username == null)
        { throw new BadRequestException("No username supplied");}
        String newToken = UUID.randomUUID().toString();

        this.daos.authDao.addAuthToken(username, newToken);
        return newToken;
    }

    public String getUsernameFromToken(String token) throws DataAccessException
    {
        return daos.authDao.authenticateToken(token);
    }

    public void logout(String token) throws DataAccessException, UserNotValidatedException
    {
        if (daos.authDao.authenticateToken(token) == null)
        {throw new UserNotValidatedException("Not validated");}
        daos.authDao.remove(token);
    }
}